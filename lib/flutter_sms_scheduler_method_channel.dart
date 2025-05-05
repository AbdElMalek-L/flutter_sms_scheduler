import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_sms_scheduler_platform_interface.dart';

/// An implementation of [FlutterSmsSchedulerPlatform] that uses method channels.
class MethodChannelFlutterSmsScheduler extends FlutterSmsSchedulerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_sms_scheduler');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
