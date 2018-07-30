package it.matteocrippa.flutternfcreader

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar


class FlutterNfcReaderPlugin(private val activity: Activity) : MethodCallHandler, PluginRegistry.NewIntentListener {

    private val nfcAdapter: NfcAdapter by lazy { NfcAdapter.getDefaultAdapter(activity) }
    private var isReading = false
    private var resulter: Result? = null

    private val pendingIntent by lazy {
        val intent = Intent(activity, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        PendingIntent.getActivity(activity, 0, intent, 0)
    }

    private val intentFilters by lazy {
        val filter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply { addDataType("*/*") }
        arrayOf(filter)
    }

    companion object {
        val NFC_TYPES = arrayOf(NfcF::class.java.name)

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

                if (isReading) {
                } else {
                    result.error("Flutter", "NFC Hardware not found", "")
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

    private fun startNFC() {
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, arrayOf(NFC_TYPES))
        isReading = true
    }

    private fun stopNFC() {
        nfcAdapter.disableForegroundDispatch(activity)
        isReading = false
    }

    override fun onNewIntent(p0: Intent?): Boolean {

        p0?.let { intent ->
            when (intent.action) {
                NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                    val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    rawMessages?.map { it as NdefMessage }?.forEach {
                        resulter?.success(it)
                    }
                    stopNFC()
                }
                else -> {
                    stopNFC()
                }
            }

        }

        return true
    }
}
