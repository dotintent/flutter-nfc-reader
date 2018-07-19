#import "FlutterNfcReaderPlugin.h"
#import <flutter_nfc_reader/flutter_nfc_reader-Swift.h>

@implementation FlutterNfcReaderPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterNfcReaderPlugin registerWithRegistrar:registrar];
}
@end
