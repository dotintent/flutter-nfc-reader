#import "FlutterNfcReaderPlugin.h"
#import <flutter_nfc_reader/flutter_nfc_reader-Swift.h>

@implementation FlutterNfcReaderPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    if (@available(iOS 11.0, *)) {
        [SwiftFlutterNfcReaderPlugin registerWithRegistrar:registrar];
    } else {
        // Fallback on earlier versions
    }
}
@end
