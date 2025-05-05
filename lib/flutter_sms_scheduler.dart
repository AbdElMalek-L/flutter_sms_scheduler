import 'package:flutter/services.dart';
import 'flutter_sms_scheduler_platform_interface.dart';

class FlutterSmsScheduler {
  static const MethodChannel _channel = MethodChannel('flutter_sms_scheduler');

  /// Example method to get platform version (default template)
  Future<String?> getPlatformVersion() {
    return FlutterSmsSchedulerPlatform.instance.getPlatformVersion();
  }

  /// Schedule an SMS at a specific time
  static Future<void> scheduleSms({
    required String phoneNumber,
    required String message,
    required DateTime scheduledTime,
  }) async {
    try {
      await _channel.invokeMethod('scheduleSms', {
        'phoneNumber': phoneNumber,
        'message': message,
        'scheduledTime': scheduledTime.millisecondsSinceEpoch,
      });
    } on PlatformException catch (e) {
      throw 'Failed to schedule SMS: ${e.message}';
    }
  }
}
