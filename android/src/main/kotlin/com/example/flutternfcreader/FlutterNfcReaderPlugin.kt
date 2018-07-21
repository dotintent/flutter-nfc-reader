package it.matteocrippa.flutternfcreader

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.util.Log
import com.fondesa.kpermissions.extension.onAccepted
import com.fondesa.kpermissions.extension.onDenied
import com.fondesa.kpermissions.extension.onPermanentlyDenied
import com.fondesa.kpermissions.extension.onShouldShowRationale
import com.fondesa.kpermissions.extension.permissionsBuilder
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterNfcReaderPlugin(private val ctx: Context, private val activity: Activity) : MethodCallHandler, PluginRegistry.ActivityResultListener{

    private var resulter: Result? = null
    private var isActive = false
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar): Unit {
            val channel = MethodChannel(registrar.messenger(), "flutter_nfc_reader")
            channel.setMethodCallHandler(FlutterNfcReaderPlugin(registrar.context(), registrar.activity()))
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result): Unit {

        val request = activity.permissionsBuilder(Manifest.permission.NFC).build()

        request.onAccepted { permissions ->
            Log.w("permissions", permissions.toString())
        }.onDenied { permissions ->
                    Log.w("permissions", permissions.toString())
                }.onPermanentlyDenied { permissions ->
                    Log.w("permissions", permissions.toString())
                }.onShouldShowRationale { permissions, nonce ->
                    Log.w("permissions", permissions.toString())
                    Log.w("permissions", nonce.toString())
                }.send()

        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "NfcRead" -> {

                isActive = initializeNFC()

                if (isActive) {
                    resulter = result
                } else {
                    result.error("Flutter", "NFC Hardware not found", "")
                }

            }
            "NfcStop" -> {
                stopNFC()
                result.success(isActive)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun initializeNFC(): Boolean {

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        if (nfcAdapter == null) {
            return false
        }

        if (!nfcAdapter!!.isEnabled) {
            return false
        }

        pendingIntent = PendingIntent.getActivity(ctx, 0, Intent(activity, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        return true
    }

    private fun stopNFC() {
        resulter = null
        isActive = false
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    override fun onActivityResult(p0: Int, p1: Int, intent: Intent?): Boolean {
        if (intent == null) {
            return false
        }

        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                rawMessages?.map { it as NdefMessage }?.forEach {

                    // Log.d("nfc", it.toString())

                    resulter?.success(it)
                    stopNFC()

                    return true
                }

            }
            else -> {
                return false
            }
        }

        return false
    }

}
