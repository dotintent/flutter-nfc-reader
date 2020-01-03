# Flutter NFC Reader & Writer

![](https://raw.githubusercontent.com/matteocrippa/flutter-nfc-reader/master/.github/nfc-flutter-logo.jpg)

A new flutter plugin to help developers looking to use internal hardware inside iOS or Android devices for reading and writing NFC tags.

The system activate a pooling reading session that stops automatically once a tag has been recognised.
You can also trigger the stop event manually using a dedicated function.

## Supported NFC Format

| Platform | Supported NFC Tags               |
| -------- | -------------------------------- |
| Android  | **NDEF:**  A, B, F, V, BARCODE   |
| iOS      | **NDEF:** NFC TYPE 1, 2, 3, 4, 5 |


## Only Android supports nfc tag writing

## Installation

Add to pubspec.yaml:

```yaml
dependencies:
  flutter_nfc_reader: ^0.1.0
```

or to get the experimental one:

```yaml
dependencies:
  flutter_nfc_reader:
    git:
      url: git://github.com/matteocrippa/flutter-nfc-reader.git
      ref: develop
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

Assign 19 in minSdkVersion in the  `build.gradle (Module: app)`

```gradle
defaultConfig {
...
minSdkVersion 19
...
}
```

### iOS Setup

Atm only `Swift` based Flutter project are supported.

- Enable Capabilities / Near Field Communication Tag Reading.
- Info.plist file, add Privacy - NFC Scan Usage Description with string value NFC Tag.

In your Podfile add this code in the top

```ruby
platform :ios, '8.0'
use_frameworks!
```

### Read NFC

This function will return a promise when a read occurs, till that very moment the reading session is open.
The promise will return a `NfcData` model, this model contains:

`FlutterNfcReader.read()` and `FlutterNfcReader.onTagDiscovered()` have an optional parameter, only for **iOS**, called `instruction`.
You can pass a _String_ that contains information to be shown in the modal screen.

- id > id of the tag
- content > content of the tag
- error > if any error occurs

```dart
FlutterNfcReader.read().then((response) {
    print(response.content);
});
```

### Stream NFC

this function will return a Stream that emits `NfcData` everytime a tag is recognized. On Ios you can use this too but IOS will always show a bottom sheet when it wants to scan a NFC Tag. Therefore you need to explicitly cast `FlutterNfcReader.read()` when you expect a second value. When subscribing to the stream the read function is called a first time for you. View the Example for a sample implementation.

```dart
FlutterNfcReader.onTagDiscovered().listen((onData) {
  print(onData.id);
  print(onData.content);
});
```

### Write NFC (Only Android)

This function will return a promise when a write occurs, till that very moment the reading session is open.
The promise will return a `NfcData` model, this model contains:

- content > writed in the tag

```dart
FlutterNfcReader.write("path_prefix","tag content").then((response) {
print(response.content);
});
```

### Read & Write NFC (Only Android)

You can read and then write in an nfc tag using the above functions as follows

```dart
FlutterNfcReader.read().then((readResponse) {
    FlutterNfcReader.write(" ",readResponse.content).then((writeResponse) {
        print('writed: ${writeResponse.content}');
    });
});
```

### Stop NFC

- status > status of ncf reading or writing stoped

```dart
FlutterNfcReader.stop().then((response) {
    print(response.status.toString());
});
```

For better details look at the demo app.

### Check NFC Availability
In order to check whether the Device supports NFC or not you can call `FlutterNfcReader.checkNFCAvailability()`.
The method returns `NFCAvailability.available` when NFC is supported and enabled, `NFCAvailability.disabled` when NFC is disabled (Android only) and `NFCAvailability.not_supported` when the user's hardware does not support NFC.

### IOS Specifics

IOS behaves a bit different in terms of NFC Scanning and writing.

- Ids of the tags aren't possible in the current implementation
- each scan is visible for the user with a bottom sheet 

## Getting Started

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

For help on editing plugin code, view the [documentation](https://flutter.io/developing-packages/#edit-plugin-package).

## Contributing

Please take a quick look at the [contribution guidelines](https://github.com/matteocrippa/flutter-nfc-reader/blob/master/.github/CONTRIBUTING.md) first. If you see a package or project here that is no longer maintained or is not a good fit, please submit a pull request to improve this file.
Thank you to all [contributors](https://github.com/matteocrippa/flutter-nfc-reader/graphs/contributors)!!

to develop on ios you need to activate the "legacy" build system because of this issue in flutter:

<https://github.com/flutter/flutter/issues/20685>
