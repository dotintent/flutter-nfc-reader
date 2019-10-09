import 'dart:async';

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

  static Future<NfcData> stop() async {
    final Map data = await _channel.invokeMethod('NfcStop');
    final NfcData result = NfcData.fromMap(data);

    return result;
  }

  static Future<NfcData> read() async {
    final Map data = await _channel.invokeMethod('NfcRead');
    final NfcData result = NfcData.fromMap(data);

    return result;
  }

  static Stream<NfcData> onTagDiscovered() {
    return stream.receiveBroadcastStream().map((rawNfcData) {
      return NfcData.fromMap(rawNfcData);
    });
  }

  static Future<NfcData> write(String path, String label) async {
    final Map data = await _channel.invokeMethod(
        'NfcWrite', <String, dynamic>{'label': label, 'path': path});

    final NfcData result = NfcData.fromMap(data);

    return result;
  }
}
