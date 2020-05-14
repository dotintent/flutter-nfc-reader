package it.matteocrippa.flutternfcreader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import android.os.Handler
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.nio.charset.Charset
import android.os.Looper
import java.io.IOException

const val PERMISSION_NFC = 1007

class FlutterNfcReaderPlugin(registrar: Registrar) : MethodCallHandler, EventChannel.StreamHandler, NfcAdapter.ReaderCallback {

    private val activity = registrar.activity()

    private var nfcAdapter: NfcAdapter? = null
    private var nfcManager: NfcManager? = null


    private var kId = "nfcId"
    private var kContent = "nfcContent"
    private var kError = "nfcError"
    private var kStatus = "nfcStatus"
    private var kWrite = ""
    private var kPath = ""
    private var readResult: Result? = null
    private var writeResult: Result? = null
    private var tag: Tag? = null
    private var eventChannel: EventChannel.EventSink? = null

    private var nfcFlags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_BARCODE or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val messenger = registrar.messenger()
            val channel = MethodChannel(messenger, "flutter_nfc_reader")
            val eventChannel = EventChannel(messenger, "it.matteocrippa.flutternfcreader.flutter_nfc_reader")
            val plugin = FlutterNfcReaderPlugin(registrar)
            channel.setMethodCallHandler(plugin)
            eventChannel.setStreamHandler(plugin)
        }
    }

    init {
        if (activity != null) {
            nfcManager = activity.getSystemService(Context.NFC_SERVICE) as? NfcManager
            nfcAdapter = nfcManager?.defaultAdapter

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                        arrayOf(Manifest.permission.NFC),
                        PERMISSION_NFC
                )
            }

            startNFCReader()
        }
    }

    private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {

        try {
            val nDefTag = Ndef.get(tag)

            nDefTag?.let {
                it.connect()
                if (it.maxSize < nfcMessage.toByteArray().size) {
                    //Message to large to write to NFC tag
                    return false
                }
                return if (it.isWritable) {
                    it.writeNdefMessage(nfcMessage)
                    it.close()
                    //Message is written to tag
                    true
                } else {
                    //NFC tag is read-only
                    false
                }
            }

            val nDefFormatableTag = NdefFormatable.get(tag)

            nDefFormatableTag?.let {
                return try {
                    it.connect()
                    it.format(nfcMessage)
                    it.close()
                    //The data is written to the tag
                    true
                } catch (e: IOException) {
                    //Failed to format tag
                    false
                }
            }
            //NDEF is not supported
            return false

        } catch (e: Exception) {
            //Write operation has failed
        }
        return false
    }

    fun createNFCMessage(payload: String?, intent: Intent?): Boolean {

        val pathPrefix = "it.matteocrippa.flutternfcreader"
        val nfcRecord = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, pathPrefix.toByteArray(), ByteArray(0), (payload as String).toByteArray())
        val nfcMessage = NdefMessage(arrayOf(nfcRecord))
        intent?.let {
            val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            return writeMessageToTag(nfcMessage, tag)
        }
        return false
    }


    override fun onMethodCall(call: MethodCall, result: Result) {

        if (call.method == "NfcEnableReaderMode") {
            startNFCReader()
        } else if (call.method == "NfcDisableReaderMode") {
            stopNFCReader()
        }

        if (nfcAdapter?.isEnabled != true && call.method != "NfcAvailable") {
            result.error("404", "NFC Hardware not found", null)
            return
        }

        when (call.method) {
            "NfcStop" -> {
                readResult = null
                writeResult = null
            }

            "NfcRead" -> {
                readResult = result
            }

            "NfcWrite" -> {
                writeResult = result
                kWrite = call.argument("label")!!
                kPath = call.argument("path")!!
                if (this.tag != null) {
                    writeTag()
                }
            }
            "NfcAvailable" -> {
                when {
                    nfcAdapter == null -> result.success("not_supported")
                    nfcAdapter!!.isEnabled -> result.success("available")
                    else -> result.success("disabled")
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    // EventChannel.StreamHandler methods
    override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
        eventChannel = events
    }

    override fun onCancel(arguments: Any?) {
        eventChannel = null
    }


    private fun startNFCReader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter?.enableReaderMode(activity, this, nfcFlags, null)
        }
    }


    private fun stopNFCReader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter?.disableReaderMode(activity)
        }
    }


    private fun writeTag() {
        if (writeResult != null) {
            val nfcRecord = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, kPath.toByteArray(), ByteArray(0), kWrite.toByteArray())
            val nfcMessage = NdefMessage(arrayOf(nfcRecord))
            writeMessageToTag(nfcMessage, tag)
            val data = mapOf(kId to "", kContent to kWrite, kError to "", kStatus to "write")
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                writeResult?.success(data)
                writeResult = null
            }
        }
    }


    private fun readTag() {
        if (readResult != null) {
            // convert tag to NDEF tag
            val ndef = Ndef.get(tag)
            ndef?.connect()
            val ndefMessage = ndef?.ndefMessage ?: ndef?.cachedNdefMessage
            val message = ndefMessage?.toByteArray()
                    ?.toString(Charset.forName("UTF-8")) ?: ""
            //val id = tag?.id?.toString(Charset.forName("ISO-8859-1")) ?: ""
            val id = bytesToHexString(tag?.id) ?: ""
            ndef?.close()
            val data = mapOf(kId to id, kContent to message, kError to "", kStatus to "reading")
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                readResult?.success(data)
                readResult = null
            }
        } else {
            // convert tag to NDEF tag
            val ndef = Ndef.get(tag)
            ndef?.connect()
            val ndefMessage = ndef?.ndefMessage ?: ndef?.cachedNdefMessage
            val message = ndefMessage?.toByteArray()
                    ?.toString(Charset.forName("UTF-8")) ?: ""
            //val id = tag?.id?.toString(Charset.forName("ISO-8859-1")) ?: ""
            val id = bytesToHexString(tag?.id) ?: ""
            ndef?.close()
            val data = mapOf(kId to id, kContent to message, kError to "", kStatus to "reading")
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                eventChannel?.success(data)
            }
        }
    }

    // handle discovered NDEF Tags
    override fun onTagDiscovered(tag: Tag?) {
        this.tag = tag
        writeTag()
        readTag()
        Handler(Looper.getMainLooper()).postDelayed({
            this.tag = null
        }, 2000)
    }

    private fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("0x")
        if (src == null || src.isEmpty()) {
            return null
        }

        val buffer = CharArray(2)
        for (i in src.indices) {
            buffer[0] = Character.forDigit(src[i].toInt().ushr(4).and(0x0F), 16)
            buffer[1] = Character.forDigit(src[i].toInt().and(0x0F), 16)
            stringBuilder.append(buffer)
        }

        return stringBuilder.toString()
    }
}
