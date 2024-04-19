import Flutter
import UIKit
import CoreNFC

@available(iOS 11.0, *)
public class SwiftFlutterNfcReaderPlugin: NSObject, FlutterPlugin {
    
    private var nfcSession: NFCNDEFReaderSession?
    private var readResult: FlutterResult?
    private var writeResult: FlutterResult?
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_nfc_reader", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterNfcReaderPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "NfcRead":
            guard let instruction = (call.arguments as? [String: Any])?["instruction"] as? String else {
                result(FlutterError(code: "invalid_arguments", message: "Instruction is missing", details: nil))
                return
            }
            readResult = result
            activateNFC(with: instruction)
        case "NfcWrite":
            guard let data = (call.arguments as? [String: Any])?["data"] as? String else {
                result(FlutterError(code: "invalid_arguments", message: "Data is missing", details: nil))
                return
            }
            writeResult = result
            writeNFC(with: data)
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func activateNFC(with instruction: String) {
        nfcSession = NFCNDEFReaderSession(delegate: self, queue: DispatchQueue.main, invalidateAfterFirstRead: true)
        nfcSession?.alertMessage = instruction
        nfcSession?.begin()
    }
    
    private func writeNFC(with data: String) {
        // Not supported on iOS, return error
        writeResult?(FlutterError(code: "write_not_supported", message: "Writing NFC tags is not supported on iOS", details: nil))
    }
}

@available(iOS 11.0, *)
extension SwiftFlutterNfcReaderPlugin: NFCNDEFReaderSessionDelegate {
    public func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        // Handle NFC detection
        guard let payload = messages.first?.records.first else { return }
        let payloadData = payload.payload
        
        // Convert payload data to string
        let payloadString = String(data: payloadData, encoding: .utf8) ?? ""
        
        // Send result to Flutter
        readResult?(payloadString)
    }
    
    public func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        // Handle NFC session invalidation
        readResult?(FlutterError(code: "nfc_error", message: error.localizedDescription, details: nil))
    }
}
