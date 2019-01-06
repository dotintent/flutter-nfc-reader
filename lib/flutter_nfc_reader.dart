import 'dart:async';
import 'package:flutter/services.dart';

class NfcData {
  final String id;
  final String content;
  final String error;
  final bool isReading;

  NfcData(this.id, this.content, this.error, this.isReading);
}

class FlutterNfcReader {
  static const MethodChannel _channel =
      const MethodChannel('flutter_nfc_reader');

  static const kId = "id";
  static const kError = "error";
  static const kContent = "content";
  static const kReading = "reading";

  static Future<NfcData> get read async {
    final Map data = await _channel.invokeMethod('NfcRead');

    final NfcData result = NfcData(
      data[kId],
      data[kContent],
      data[kError],
      false,
    );

    return result;
  }

  static Future<NfcData> get stop async {
    final bool stopped = await _channel.invokeMethod('NfcStop');

    return NfcData(
      null,
      null,
      null,
      stopped,
    );
  }
}
