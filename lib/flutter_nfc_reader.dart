import 'dart:async';
import 'package:flutter/services.dart';

class NfcData {
  final String id;
  final String content;
  final String error;

  NfcData(this.id, this.content, this.error);
}

class FlutterNfcReader {
  static const MethodChannel _channel =
      const MethodChannel('flutter_nfc_reader');

  static const kId = "id";
  static const kError = "error";
  static const kContent = "content";

  static Future<NfcData> get read async {
    final Map data = await _channel.invokeMethod('NfcRead');

    final NfcData result = NfcData(data[kId], data[kContent], data[kError]);
    return result;
  }

  static Future<bool> get stop async {
    final bool stopped = await _channel.invokeMethod('NfcStop');
    return stopped;
  }
}
