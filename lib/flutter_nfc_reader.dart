import 'dart:async';
import 'dart:io' show Platform;

import 'package:flutter/services.dart';

enum NFCStatus {
  none,
  reading,
  read,
  stopped,
  error,
}

class NfcData {
  final String id;
  final String content;
  final String error;
  final String statusMapper;

  NFCStatus status;

  NfcData({
    this.id,
    this.content,
    this.error,
    this.statusMapper,
  });

  factory NfcData.fromMap(Map data) {
    NfcData result = NfcData(
      id: data['nfcId'],
      content: data['nfcContent'],
      error: data['nfcError'],
      statusMapper: data['nfcStatus'],
    );
    switch (result.statusMapper) {
      case 'none':
        result.status = NFCStatus.none;
        break;
      case 'reading':
        result.status = NFCStatus.reading;
        break;
      case 'stopped':
        result.status = NFCStatus.stopped;
        break;
      case 'error':
        result.status = NFCStatus.error;
        break;
      default:
        result.status = NFCStatus.none;
    }
    return result;
  }
}

class FlutterNfcReader {
  static const MethodChannel _channel =
      const MethodChannel('flutter_nfc_reader');
  static const stream =
      const EventChannel('it.matteocrippa.flutternfcreader.flutter_nfc_reader');

  static Future<NfcData> enableReaderMode() async {
    final Map data = await _channel.invokeMethod('NfcEnableReaderMode');
    final NfcData result = NfcData.fromMap(data);

    return result;
  }

  static Future<NfcData> disableReaderMode() async {
    final Map data = await _channel.invokeMethod('NfcDisableReaderMode');
    final NfcData result = NfcData.fromMap(data);

    return result;
  }

  static Future<NfcData> stop() async {
    final Map data = await _channel.invokeMethod('NfcStop');
    final NfcData result = NfcData.fromMap(data);

    return result;
  }

  static Future<NfcData> read({String instruction}) async {
    final Map data = await _callRead(instruction: instruction);
    final NfcData result = NfcData.fromMap(data);
    return result;
  }

  static Stream<NfcData> onTagDiscovered({String instruction}) {
    if (Platform.isIOS) {
      _callRead(instruction: instruction);
    }
    return stream.receiveBroadcastStream().map((rawNfcData) {
      return NfcData.fromMap(rawNfcData);
    });
  }

  static Future<Map> _callRead({instruction: String}) async {
      return await _channel.invokeMethod('NfcRead', <String, dynamic> {
        "instruction": instruction
      });
  }

  static Future<NfcData> write(String path, String label) async {
    final Map data = await _channel.invokeMethod(
        'NfcWrite', <String, dynamic>{'label': label, 'path': path});

    final NfcData result = NfcData.fromMap(data);

    return result;
  }
  static Future<NFCAvailability> checkNFCAvailability() async {
    var availability = "NFCAvailability.${await _channel.invokeMethod<String>("NfcAvailable")}";
    return NFCAvailability.values.firstWhere((item) => item.toString() == availability);
  }
}

enum NFCAvailability {
  available, disabled, not_supported
}
