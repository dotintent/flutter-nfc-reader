import Flutter
import Foundation
import CoreNFC
import VYNFCKit

@available(iOS 11.0, *)
public class SwiftFlutterNfcReaderPlugin: NSObject, FlutterPlugin {
    
    fileprivate var nfcSession: NFCNDEFReaderSession? = nil
    fileprivate var instruction: String? = nil
    fileprivate var resulter: FlutterResult? = nil
    fileprivate var readResult: FlutterResult? = nil
    
    private var eventSink: FlutterEventSink?
    
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
            print("read")
            activateNFC(instruction)
        case "NfcStop":
            resulter = result
            disableNFC()
        case "NfcWrite":
            var alertController = UIAlertController(title: nil, message: "IOS does not support NFC tag writing", preferredStyle: .alert)
            alertController.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            UIApplication.shared.keyWindow?.rootViewController?.present(alertController, animated: true)
        case "NfcAvailable":
            var nfcAvailable = NFCNDEFReaderSession.readingAvailable
            result(nfcAvailable ? "available" : "not_supported")
        default:
            result("iOS " + UIDevice.current.systemVersion)
        }
    }
}

// MARK: - NFC Actions
@available(iOS 11.0, *)
extension SwiftFlutterNfcReaderPlugin {
    func activateNFC(_ instruction: String?) {
        print("activate")
        
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
    
    func sendNfcEvent(data: [String: String]){
        guard let eventSink = eventSink else {
            return
        }
        eventSink(data)
    }
}

// MARK: - NFCDelegate
@available(iOS 11.0, *)
extension SwiftFlutterNfcReaderPlugin : NFCNDEFReaderSessionDelegate {
    
    public func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        print(messages)
        guard let message = messages.first else { return }
        guard let payload = message.records.first else { return }
        
        print(payload.identifier as NSData)
        
        // Start Package
        
        let parsedPayload = VYNFCNDEFPayloadParser.parse(payload)
        var text = ""
        var urlString = ""
        
        if let parsedPayload = parsedPayload as? VYNFCNDEFTextPayload {
            // text = "[Text payload]\n"
            text = String(format: "%@%@", text, parsedPayload.text)
        } else if let parsedPayload = parsedPayload as? VYNFCNDEFURIPayload {
            // text = "[URI payload]\n"
            text = String(format: "%@%@", text, parsedPayload.uriString)
            urlString = parsedPayload.uriString
        } else if let parsedPayload = parsedPayload as? VYNFCNDEFTextXVCardPayload {
            // text = "[TextXVCard payload]\n"
            text = String(format: "%@%@", text, parsedPayload.text)
        } else if let sp = parsedPayload as? VYNFCNDEFSmartPosterPayload {
            // text = "[SmartPoster payload]\n"
            for textPayload in sp.payloadTexts {
                if let textPayload = textPayload as? VYNFCNDEFTextPayload {
                    text = String(format: "%@%@\n", text, textPayload.text)
                }
            }
            text = String(format: "%@%@", text, sp.payloadURI.uriString)
            urlString = sp.payloadURI.uriString
        } else if let wifi = parsedPayload as? VYNFCNDEFWifiSimpleConfigPayload {
            for case let credential as VYNFCNDEFWifiSimpleConfigCredential in wifi.credentials {
                text = String(format: "%@SSID: %@\nPassword: %@\nMac Address: %@\nAuth Type: %@\nEncrypt Type: %@",
                              text, credential.ssid, credential.networkKey, credential.macAddress,
                              VYNFCNDEFWifiSimpleConfigCredential.authTypeString(credential.authType),
                              VYNFCNDEFWifiSimpleConfigCredential.encryptTypeString(credential.encryptType)
                )
            }
            if let version2 = wifi.version2 {
                text = String(format: "%@\nVersion2: %@", text, version2.version)
            }
        } else {
            text = "";
        }
        print(text)
        
        // end package
        
        let data = [kId: "", kContent: text, kError: "", kStatus: "reading"]
        sendNfcEvent(data: data);
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
        eventSink = nil
        return nil
    }
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.eventSink = events
        return nil
    }
}
