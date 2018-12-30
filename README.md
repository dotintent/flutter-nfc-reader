# Flutter NFC Reader

A new flutter plugin to help developers looking to use internal hardware inside iOS or Android devices for reading NFC tags.

The system activate a pooling reading session that stops automatically once a tag has been recognised.
You can also trigger the stop event manually using a dedicated function.

## Installation

Add to pubspec.yaml:

```yaml
dependencies:
  flutter_nfc_reader: ^0.0.22
```

and then run the shell

```shell
flutter packages get
```

last step import to the project:

```dart
import 'package:flutter_nfc_reader/flutter_nfc_reader.dart';
```

## How to use

### Android setup

Add those two lines to your `AndroidManifest.xml` on the top

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature
        android:name="android.hardware.nfc"
        android:required="true" />
```

### iOS Setup

Atm only `Swift` based Flutter project are supported.

- Enable Capabilities / Near Field Communication Tag Reading. 
- Info.plist file, add Privacy - NFC Scan Usage Description with string value NFC Tag.


### Read NFC

This function will return a promise when a read occurs, till that very moment the reading session is open.
The promise will return a `NfcData` model, this model contains:

- id > id of the tag
- content > content of the tag
- error > if any error occurs

```dart
Future<void> startNFC() async {
    NfcData response;
    bool reading = true;

    try {
      response = await FlutterNfcReader.read;
    } on PlatformException {
      reading = false;
    }
    setState(() {
      _nfcReading = reading;
      _nfcData = response;
    });
  }
```

### Stop NFC
```dart

  Future<void> stopNFC() async {
    bool response;
    try {
      final bool result = await FlutterNfcReader.stop;
      response = result;
    } on PlatformException {
      response = false;
    }
    setState(() {
      _nfcReading = response;
    });
  }
```

For better details look at the demo app.

## Getting Started

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

For help on editing plugin code, view the [documentation](https://flutter.io/developing-packages/#edit-plugin-package).
