package com.example.flutter_sms_scheduler

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object SmsUtil {
    private const val PERMISSION_REQUEST_SEND_SMS = 123
    // Remove hardcoded values, they will be passed as parameters
    // private const val SMS_DESTINATION = "3030"
    // private const val SMS_MESSAGE = "1"
    // private const val ALARM_REQUEST_CODE = 234 // Use unique request codes
    private const val TAG = "SmsUtil"
    const val EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER"
    const val EXTRA_MESSAGE = "EXTRA_MESSAGE"

    /**
     * Check if the app has SMS permission and request it if not granted
     */
    fun checkSmsPermission(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST_SEND_SMS
            )
            return false
        }
        return true
    }

    /**
     * Send SMS to the specified number with the given message
     */
    fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            Log.d(TAG, "Attempting to send SMS to $phoneNumber with message: '$message'")
            // Use the appropriate SmsManager depending on Android version
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val sentIntent = PendingIntent.getBroadcast(
                context, 0, Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )
            
            smsManager.sendTextMessage(
                phoneNumber,
                null, // originating address
                message,
                sentIntent, // sentIntent
                null // deliveryIntent
            )
            
            Log.d(TAG, "SMS sent successfully to $phoneNumber")
            // Consider removing this Toast or making it optional, as it runs in the background
            // Toast.makeText(context, "SMS sent to $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to send SMS due to permission issue: ${e.message}", e)
            // Toast.makeText(context, "Failed to send SMS: Permission denied", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS: ${e.message}", e)
            // Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * Schedule SMS to be sent at a specific time using AlarmManager
     */
    fun scheduleSms(context: Context, phoneNumber: String, message: String, scheduledTimeMillis: Long) {
        try {
            Log.d(TAG, "Attempting to schedule SMS to $phoneNumber at $scheduledTimeMillis")

            // Create an intent for the AlarmReceiver
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "SEND_SCHEDULED_SMS" // Use a specific action
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(EXTRA_MESSAGE, message)
                // Ensure uniqueness for multiple alarms: Use timestamp in data URI
                data = android.net.Uri.parse("smsScheduler://$scheduledTimeMillis/$phoneNumber")
            }
            
            Log.d(TAG, "Created intent with action: ${intent.action}, data: ${intent.data}")

            // Generate a unique request code based on timestamp to avoid collisions
            val requestCode = (scheduledTimeMillis % Int.MAX_VALUE).toInt()

            // Create a PendingIntent
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode, // Use unique request code
                intent,
                flags
            )
            
            Log.d(TAG, "Created pending intent: $pendingIntent with request code: $requestCode")
            
            // Get the AlarmManager service
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Check for exact alarm permission on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e(TAG, "Cannot schedule exact alarms - permission not granted")
                    // Optionally: Request permission or guide user to settings
                    // For now, just log and potentially inform Flutter side
                    Toast.makeText(context, "Exact alarm permission needed", Toast.LENGTH_LONG).show()
                    // Consider throwing an exception or returning a status to Flutter
                    return // Stop scheduling if permission is missing
                }
            }

            // Schedule the exact alarm using RTC_WAKEUP for absolute time
            Log.d(TAG, "Setting exact alarm using RTC_WAKEUP for time: $scheduledTimeMillis")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTimeMillis,
                    pendingIntent
                )
            } else {
                 alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTimeMillis,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "SMS alarm scheduled successfully for $phoneNumber at $scheduledTimeMillis")
            // Toast is likely not useful here as it's called from background
            // Toast.makeText(context, "SMS scheduled for $phoneNumber", Toast.LENGTH_SHORT).show()

        } catch (e: SecurityException) {
             Log.e(TAG, "Failed to schedule SMS due to security issue: ${e.message}", e)
             Toast.makeText(context, "Failed to schedule SMS: Security issue", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule SMS: ${e.message}", e)
            Toast.makeText(context, "Failed to schedule SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * Handle permission request result (if called from an Activity context)
     */
    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray, activity: Activity) {
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "SMS permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        // Add handling for SCHEDULE_EXACT_ALARM if requested directly
    }
}