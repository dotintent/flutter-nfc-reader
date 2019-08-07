import Flutter
import Foundation
import CoreNFC

@available(iOS 11.0, *)
public class SwiftFlutterNfcReaderPlugin: NSObject, FlutterPlugin {
    
    fileprivate var nfcSession: NFCNDEFReaderSession? = nil
    fileprivate var instruction: String? = nil
    fileprivate var resulter: FlutterResult? = nil
    fileprivate var readResult: FlutterResult? = nil
    
    
    fileprivate let kId = "nfcId"
    fileprivate let kContent = "nfcContent"
    fileprivate let kStatus = "nfcStatus"
    fileprivate let kError = "nfcError"
    
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_nfc_reader", binaryMessenger: registrar.messenger())
        let eventChannel = FlutterEventChannel(name: "it.matteocrippa.flutternfcreader.flutter_nfc_reader", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterNfcReaderPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
        eventChannel.setStreamHandler(instance)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch(call.method) {
        case "NfcRead":
            let map = call.arguments as? Dictionary<String, String>
            instruction = map?["instruction"] ?? ""
            readResult = result
            activateNFC(instruction)
        case "NfcStop":
            resulter = result
            disableNFC()
        case "NfcWrite":
            var alertController = UIAlertController(title: nil, message: "IOS does not support NFC tag writing", preferredStyle: .alert)
            alertController.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            UIApplication.shared.keyWindow?.rootViewController?.present(alertController, animated: true)
        default:
            result("iOS " + UIDevice.current.systemVersion)
        }
    }
}

// MARK: - NFC Actions
@available(iOS 11.0, *)
extension SwiftFlutterNfcReaderPlugin {
    func activateNFC(_ instruction: String?) {
        // setup NFC session
        nfcSession = NFCNDEFReaderSession(delegate: self, queue: DispatchQueue(label: "queueName", attributes: .concurrent), invalidateAfterFirstRead: true)
        
        // then setup a new session
        if let instruction = instruction {
            nfcSession?.alertMessage = instruction
        }
        
        // start
        if let nfcSession = nfcSession {
            nfcSession.begin()
        }
    }
    
    func disableNFC() {
        //nfcSession?.invalidate()
        let data = [kId: "", kContent: "", kError: "", kStatus: "stopped"]
        
        resulter?(data)
        resulter = nil
    }
    
}

// MARK: - NFCDelegate
@available(iOS 11.0, *)
extension SwiftFlutterNfcReaderPlugin : NFCNDEFReaderSessionDelegate {
    
    public func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        guard let message = messages.first else { return }
        guard let payload = message.records.first else { return }
        guard let payloadContent = String(data: payload.payload, encoding: String.Encoding.utf8) else { return }
        
        let data = [kId: "", kContent: payloadContent, kError: "", kStatus: "reading"]
        readResult?(data)
        readResult=nil
        //disableNFC()
    }
    
    public func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        print(error.localizedDescription)
        let data = [kId: "", kContent: "", kError: error.localizedDescription, kStatus: "error"]
        resulter?(data)
        disableNFC()
    }
}

@available(iOS 11.0, *)
extension SwiftFlutterNfcReaderPlugin: FlutterStreamHandler {
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        
        return nil
    }
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        return nil
    }
}
