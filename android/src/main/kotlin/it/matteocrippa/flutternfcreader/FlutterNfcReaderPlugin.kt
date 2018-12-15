package it.matteocrippa.flutternfcreader

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.nio.charset.Charset

class FlutterNfcReaderPlugin(val registrar: Registrar) : MethodCallHandler,  NfcAdapter.ReaderCallback {
    private var isReading = false
    private var stopOnFirstDiscovered = false
    private var nfcAdapter: NfcAdapter? = null
    private var nfcManager: NfcManager? = null

    private var resulter: Result? = null

    private var READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar): Unit {
            val channel = MethodChannel(registrar.messenger(), "flutter_nfc_reader")
            channel.setMethodCallHandler(FlutterNfcReaderPlugin(registrar))
        }
    }

    init {
        nfcManager = registrar.activity().getSystemService(Context.NFC_SERVICE) as? NfcManager
        nfcAdapter = nfcManager?.defaultAdapter
    }

    override fun onMethodCall(call: MethodCall, result: Result): Unit {

        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "NfcRead" -> {
                startNFC()
                resulter = result

                if (!isReading) {
                    result.success("NFC Hardware not found")
                    resulter = null
                }

            }
            "NfcStop" -> {
                stopNFC()
                result.success(isReading)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun startNFC(): Boolean {
        isReading = if (nfcAdapter?.isEnabled == true) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                nfcAdapter?.enableReaderMode(registrar.activity(), this, READER_FLAGS, null )
            }

            true
        } else {
            false
        }
        return isReading
    }

    private fun stopNFC() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter?.disableReaderMode(registrar.activity())
        }
        resulter = null
        isReading = false
    }

    // handle discovered NDEF Tags
    override fun onTagDiscovered(tag: Tag?) {
        // convert tag to NDEF tag
        val ndef = Ndef.get(tag)
        // ndef will be null if the discovered tag is not a NDEF tag
        // read NDEF message
        ndef?.connect()
        val message = ndef?.getNdefMessage()
                          ?.toByteArray()
                          ?.toString(Charset.forName("UTF-8"))
        ndef?.close()
        if (message != null) {
            resulter?.success(message)
        }
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