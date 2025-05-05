import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_sms_scheduler/flutter_sms_scheduler.dart';
import 'package:flutter_sms_scheduler/flutter_sms_scheduler_platform_interface.dart';
import 'package:flutter_sms_scheduler/flutter_sms_scheduler_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterSmsSchedulerPlatform
    with MockPlatformInterfaceMixin
    implements FlutterSmsSchedulerPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterSmsSchedulerPlatform initialPlatform = FlutterSmsSchedulerPlatform.instance;

  test('$MethodChannelFlutterSmsScheduler is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterSmsScheduler>());
  });

  test('getPlatformVersion', () async {
    FlutterSmsScheduler flutterSmsSchedulerPlugin = FlutterSmsScheduler();
    MockFlutterSmsSchedulerPlatform fakePlatform = MockFlutterSmsSchedulerPlatform();
    FlutterSmsSchedulerPlatform.instance = fakePlatform;

    expect(await flutterSmsSchedulerPlugin.getPlatformVersion(), '42');
  });
}
