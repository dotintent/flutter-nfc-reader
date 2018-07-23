# Flutter NFC Reader

A new flutter plugin to help developers looking to use internal hardware inside iOS or Android devices for reading NFC tags.

The system activate a pooling reading session that stops automatically once a tag has been recognised.
You can also trigger the stop event manually using a dedicated function.

## How to use

### Android setup

Add those two lines to your `AndroidManifest.xml`

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
In order to stop a reading session you need to use `stop` function.

```dart
 Future<Null> NfcRead() async {
    String response;
    try {
      final String result = await FlutterNfcReader.read();
      if (result != null) {
        response = '';
      } else {
        response = result;
      }
    } on PlatformException {
      response = '';
    }
    setState(() {
      _nfcActive = true;
      _nfcData = response;
    });
  }
```

### Stop NFC
```dart

  Future<Null> NfcStop() async {
    bool response;
    try {
      final bool result = await FlutterNfcReader.stop;
      response = !result;
    } on PlatformException {
      response = false;
    }
    setState(() {
      _nfcActive = response;
    });
  }
```

For better details look at the demo app.

## Extra

`FlutterNfcReader.read()` has an optional parameter, only for **iOS**, called `instruction`.
You can pass a _String_ that contains information to be shown in the modal screen.

## Getting Started

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

For help on editing plugin code, view the [documentation](https://flutter.io/developing-packages/#edit-plugin-package).
