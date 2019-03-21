# Flutter NFC Reader

![](https://raw.githubusercontent.com/matteocrippa/flutter-nfc-reader/master/.github/nfc-flutter-logo.jpg)

A new flutter plugin to help developers looking to use internal hardware inside iOS or Android devices for reading NFC tags.

The system activate a pooling reading session that stops automatically once a tag has been recognised.
You can also trigger the stop event manually using a dedicated function.

## Supported NFC Format

| Platform | Supported NFC Tags |
| --- | --- |
| Android | NDEF |
| iOS | NDEF |

## Installation

Add to pubspec.yaml:

```yaml
dependencies:
  flutter_nfc_reader:
    git:
      url: git://github.com/matteocrippa/flutter-nfc-reader.git
      ref: master
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
    setState(() {
      _nfcData = NfcData();
      _nfcData.status = NFCStatus.reading;
    });

    print('NFC: Scan started');

    print('NFC: Scan readed NFC tag');
    FlutterNfcReader.read.listen((response) {
      setState(() {
        _nfcData = response;
      });
    });
  }
```

### Stop NFC

```dart
Future<void> stopNFC() async {
    NfcData response;

    try {
      print('NFC: Stop scan by user');
      response = await FlutterNfcReader.stop;
    } on PlatformException {
      print('NFC: Stop scan exception');
      response = NfcData(
        id: '',
        content: '',
        error: 'NFC scan stop exception',
        statusMapper: '',
      );
      response.status = NFCStatus.error;
    }

    setState(() {
      _nfcData = response;
    });
  }
```

For better details look at the demo app.

## Getting Started

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

For help on editing plugin code, view the [documentation](https://flutter.io/developing-packages/#edit-plugin-package).

## Contributing

Please take a quick look at the [contribution guidelines](https://github.com/matteocrippa/flutter-nfc-reader/blob/master/.github/CONTRIBUTING.md) first. If you see a package or project here that is no longer maintained or is not a good fit, please submit a pull request to improve this file. 
Thank you to all [contributors](https://github.com/matteocrippa/flutter-nfc-reader/graphs/contributors)!!
