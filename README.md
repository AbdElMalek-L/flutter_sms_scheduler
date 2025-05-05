# flutter_sms_scheduler

A Flutter plugin to schedule SMS messages to be sent at a specific time on Android.

## Features

*   Schedule SMS messages to be sent at a future date and time.
*   Leverages Android's `AlarmManager` for scheduling and `SmsManager` for sending.

## Getting Started

This plugin currently supports Android only.

## Installation

1.  Add `flutter_sms_scheduler` as a dependency in your `pubspec.yaml` file:

    ```yaml
    dependencies:
      flutter: 
        sdk: flutter
      flutter_sms_scheduler:
        path: ../ # Or the appropriate path/version
    ```

2.  Run `flutter pub get`.

## Android Setup

Add the following permissions to your `AndroidManifest.xml` file located at `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android" ...>
    <!-- Required to send SMS -->
    <uses-permission android:name="android.permission.SEND_SMS" />
    <!-- Required to schedule exact alarms (essential for scheduling) -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <!-- Optional: Required if you want to reschedule alarms after device reboot -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application ...>
        <activity ...>
            ...
        </activity>

        <!-- Add the receiver for handling the scheduled alarm -->
        <receiver android:name="com.example.flutter_sms_scheduler.AlarmReceiver"
                  android:exported="true">
            <intent-filter>
                <action android:name="SEND_SCHEDULED_SMS" />
            </intent-filter>
        </receiver>

        <!-- Optional: Add receiver for boot completed to reschedule alarms -->
        <!-- 
        <receiver android:name=".BootReceiver" android:enabled="true" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"></action>
            </intent-filter>
        </receiver>
        -->

    </application>
</manifest>
```

**Note:**

*   Requesting `SEND_SMS` permission at runtime is crucial before attempting to schedule or send SMS.
*   Handling the `SCHEDULE_EXACT_ALARM` permission might be necessary for Android 12 (API 31) and above. Check Android documentation for details.
*   Implementing the `BootReceiver` (commented out above) is necessary if you need scheduled messages to persist after the device restarts.

## Usage

Import the package:

```dart
import 'package:flutter_sms_scheduler/flutter_sms_scheduler.dart';
```

Schedule an SMS:

```dart
import 'package:flutter/material.dart';
import 'package:flutter_sms_scheduler/flutter_sms_scheduler.dart';
import 'package:permission_handler/permission_handler.dart'; // Example using permission_handler

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('SMS Scheduler Example'),
        ),
        body: Center(
          child: ElevatedButton(
            onPressed: _scheduleSms,
            child: Text('Schedule SMS in 5 minutes'),
          ),
        ),
      ),
    );
  }

  Future<void> _scheduleSms() async {
    // 1. Request Permissions (Example using permission_handler)
    if (await Permission.sms.request().isGranted && 
        await Permission.scheduleExactAlarm.request().isGranted) { // Check/Request necessary permissions

      final String phoneNumber = '+1234567890'; // Replace with target phone number
      final String message = 'This is a scheduled SMS!';
      // Schedule 5 minutes from now
      final DateTime scheduledTime = DateTime.now().add(const Duration(minutes: 5)); 

      try {
        await FlutterSmsScheduler.scheduleSms(
          phoneNumber: phoneNumber,
          message: message,
          scheduledTimeMillis: scheduledTime.millisecondsSinceEpoch,
        );
        print('SMS scheduled successfully for $scheduledTime');
        // Show confirmation to the user
      } catch (e) {
        print('Error scheduling SMS: $e');
        // Show error message to the user
      }
    } else {
      print('Required permissions not granted.');
      // Inform user that permissions are needed
    }
  }
}

```

## Important Notes

*   **Android Only:** This plugin currently only supports the Android platform.
*   **Permissions:** Ensure you have requested and been granted the necessary `SEND_SMS` and `SCHEDULE_EXACT_ALARM` permissions before calling `scheduleSms`.
*   **Background Execution:** SMS sending relies on Android's `AlarmManager`. Delivery depends on the device's state and battery optimization settings. Some manufacturers have aggressive background restrictions that might affect exact timing.
*   **Reboot Persistence:** By default, scheduled alarms are cleared when the device reboots. To make them persistent, you need to implement a `BroadcastReceiver` that listens for `BOOT_COMPLETED` and reschedules the alarms (see commented-out section in `AndroidManifest.xml` example).
*   **Error Handling:** Implement proper error handling for permission denials and scheduling failures.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

