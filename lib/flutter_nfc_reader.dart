import 'dart:async';
import 'package:flutter/services.dart';

class FlutterNfcReader {
  static const MethodChannel _channel =
      const MethodChannel('flutter_nfc_reader');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<String> read({instruction: String}) async {
    final String result =
        await _channel.invokeMethod('NfcRead', {instruction: instruction});
    return result;
  }

  static Future<bool> get stop async {
    final bool stopped = await _channel.invokeMethod('NfcStop');
    return stopped;
  }
}
