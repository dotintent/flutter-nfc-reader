package it.matteocrippa.flutternfcreader

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Build
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterNfcReaderPlugin(val registrar: Registrar) : MethodCallHandler {
    private var isReading = false
    private var stopOnFirstDiscovered = false
    private var nfcAdapter: NfcAdapter? = null
    private var nfcManager: NfcManager? = null

    private var resulter: Result? = null

    private var READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

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
                nfcAdapter?.enableReaderMode(registrar.activity(), {
                    resulter?.success(it.id)
                },READER_FLAGS, null )
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

}