package it.matteocrippa.flutternfcreader

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar


class FlutterNfcReaderPlugin(private val activity: Activity) : MethodCallHandler, PluginRegistry.NewIntentListener {

    private var isReading = false
    private var stopOnFirstDiscovered = true

    private var resulter: Result? = null

    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(activity) }
    private val pendingIntent by lazy {
        PendingIntent.getActivity(activity, 0,
            Intent(activity, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
    }
    private val intentFilters by lazy {
        val filter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        arrayOf(filter)
    }
    private val technologies by lazy {
        arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar): Unit {
            val channel = MethodChannel(registrar.messenger(), "flutter_nfc_reader")
            channel.setMethodCallHandler(FlutterNfcReaderPlugin(registrar.activity()))
        }
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
        isReading = if (nfcAdapter == null) {
            false
        } else {
            nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, intentFilters, technologies)
            true
        }
        return isReading
    }

    private fun stopNFC() {
        nfcAdapter?.disableForegroundDispatch(activity)
        resulter = null
        isReading = false
    }

    override fun onNewIntent(p0: Intent?): Boolean {

        p0?.let { intent ->
            when(intent.action) {
                NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                    val messages = getNDEF(intent)
                    messages.firstOrNull()?.let { message ->
                        message.records?.let {
                            it.forEach {
                                it?.payload.let {
                                    it?.let {
                                        resulter?.success(it)
                                        if(stopOnFirstDiscovered) {
                                            stopNFC()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    resulter?.success("No NFC Discovered")
                }
            }

        }

        return true
    }

    private fun getNDEF(intent: Intent): Array<NdefMessage> {

        val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        rawMessage?.let {
            return rawMessage.map {
                it as NdefMessage
            }.toTypedArray()
        }
        // Unknown tag type
        val empty = byteArrayOf()
        val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
        val msg = NdefMessage(arrayOf(record))
        return arrayOf(msg)
    }

}
