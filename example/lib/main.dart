import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_nfc_reader/flutter_nfc_reader.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _nfcData = '';
  bool _nfcActive = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;

    try {
      platformVersion = await FlutterNfcReader.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> startNFC() async {
    String response;
    bool state = false;

    try {
      final Map<String, dynamic> result = await FlutterNfcReader.read;

      if (result["error"]) {
        state = false;
      } else {
        state = true;
        response = result["data"];
      }
    } on PlatformException {
      response = '';
      state = false;
    }
    setState(() {
      _nfcActive = state;
      _nfcData = response;
    });
  }

  Future<void> stopNFC() async {
    bool response;
    try {
      final bool result = await FlutterNfcReader.stop;
      response = result;
    } on PlatformException {
      response = false;
    }
    setState(() {
      _nfcActive = response;
    });
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
          appBar: new AppBar(
            title: const Text('Plugin example app'),
          ),
          body: new SafeArea(
            top: true,
            bottom: true,
            child: new Center(
              child: Column(
                children: <Widget>[
                  new Text('Running on: $_platformVersion\n'),
                  new Text('NFC Status: $_nfcActive\n'),
                  new Text('NFC Data: $_nfcData\n'),
                  new FlatButton(
                    child: Text('Start NFC'),
                    onPressed: () {
                      startNFC();
                    },
                  ),
                  new FlatButton(
                    child: Text('Stop NFC'),
                    onPressed: () {
                      stopNFC();
                    },
                  ),
                ],
              ),
            ),
          )),
    );
  }
}
