import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_sms_scheduler_method_channel.dart';

abstract class FlutterSmsSchedulerPlatform extends PlatformInterface {
  /// Constructs a FlutterSmsSchedulerPlatform.
  FlutterSmsSchedulerPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterSmsSchedulerPlatform _instance = MethodChannelFlutterSmsScheduler();

  /// The default instance of [FlutterSmsSchedulerPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterSmsScheduler].
  static FlutterSmsSchedulerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterSmsSchedulerPlatform] when
  /// they register themselves.
  static set instance(FlutterSmsSchedulerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
