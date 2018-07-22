import Flutter
import UIKit
import VYNFCKit

@available(iOS 11.0, *)
public class SwiftFlutterNfcReaderPlugin: NSObject, FlutterPlugin {
    
    fileprivate var nfcSession: NFCNDEFReaderSession? = nil
    fileprivate var instruction: String? = nil
    fileprivate var resulter: FlutterResult? = nil
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_nfc_reader", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterNfcReaderPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        switch(call.method) {
        case "NfcRead":
            resulter = result
            activateNFC(instruction)
        case "NfcStop":
            result(disableNFC())
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
        nfcSession?.invalidate()
        resulter?(false)
        resulter = nil
    }

}

// MARK: - NFCDelegate
@available(iOS 11.0, *)
extension SwiftFlutterNfcReaderPlugin : NFCNDEFReaderSessionDelegate {
    
    public func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        
        for message in messages {
            for payload in message.records {
                guard let parsedPayload = VYNFCNDEFPayloadParser.parse(payload) else {
                    continue
                }
                var text = ""
                guard let result = resulter else { return  }
                
                if let parsedPayload = parsedPayload as? VYNFCNDEFURIPayload {
                    text = "[URI payload]\n"
                    text = String(format: "%@%@", text, parsedPayload.uriString)
                    let urlString = parsedPayload.uriString
                    result(urlString)
                    disableNFC()
                } else {
                    text = "N/A"
                    result(text)
                    disableNFC()
                }
            }
        }
    }
    
    public func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        print(error.localizedDescription)
    }
}
