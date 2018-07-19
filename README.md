# flutter_nfc_reader

A new flutter plugin project.

## How to use

### Android setup

Add those two lines to your `AndroidManifest.xml`

```xml
    <uses-permission android:name="android.permission.NFC" />

    <uses-feature
        android:name="android.hardware.nfc"
        android:required="true" />
```

### Start NFC
```dart
 Future<Null> startNFC() async {
    String response;
    try {
      final String result = await FlutterNfcReader.startNFC;
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

  Future<Null> stopNFC() async {
    bool response;
    try {
      final bool result = await FlutterNfcReader.stopNFC;
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

## Getting Started

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

For help on editing plugin code, view the [documentation](https://flutter.io/developing-packages/#edit-plugin-package).
