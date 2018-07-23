package it.matteocrippa.flutternfcreader

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterNfcReaderPlugin(private val activity: Activity) : MethodCallHandler, PluginRegistry.NewIntentListener {

    private var resulter: Result? = null
    private var isReading = false
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

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
                isReading = initializeNFC()

                if (isReading) {
                    resulter = result
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

    private fun initializeNFC(): Boolean {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        pendingIntent = PendingIntent.getActivity(activity, 0,
                Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        return nfcAdapter != null
    }

    private fun stopNFC() {
        resulter = null
        isReading = false
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    override fun onNewIntent(p0: Intent?): Boolean {

        p0?.let { intent ->
            when (intent.action) {
                NfcAdapter.ACTION_NDEF_DISCOVERED, NfcAdapter.ACTION_TECH_DISCOVERED -> {
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

    /*private fun (record: NdefRecord): String {
        val payload = record.payload
        return String(payload)
    }*/

}
