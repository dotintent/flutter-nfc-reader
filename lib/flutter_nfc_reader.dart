import 'dart:async';

import 'package:flutter/services.dart';

class FlutterNfcReader {
  static const MethodChannel _channel =
      const MethodChannel('flutter_nfc_reader');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> get startNFC async {
    String result;
    await _channel.invokeMethod('startNFC').then((value) {
      result = value;
    });
    return result;
  }

  static Future<bool> get stopNFC async {
    final bool stopped = await _channel.invokeMethod('stopNFC');
    return stopped;
  }
}
