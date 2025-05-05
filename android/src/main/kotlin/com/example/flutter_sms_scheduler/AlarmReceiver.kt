package com.example.flutter_sms_scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm triggered with action: ${intent.action}, data: ${intent.data}")
        if (intent.action == "SEND_SCHEDULED_SMS") {
            val phoneNumber = intent.getStringExtra(SmsUtil.EXTRA_PHONE_NUMBER)
            val message = intent.getStringExtra(SmsUtil.EXTRA_MESSAGE)

            if (phoneNumber != null && message != null) {
                try {
                    Log.d(TAG, "Attempting to send SMS to $phoneNumber from AlarmReceiver")
                    SmsUtil.sendSms(context, phoneNumber, message)
                    Log.d(TAG, "SMS send attempt successful from AlarmReceiver for $phoneNumber")
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending SMS from AlarmReceiver: ${e.message}", e)
                }
            } else {
                Log.e(TAG, "Missing phone number or message in intent extras")
            }
        } else {
             Log.w(TAG, "Received unexpected intent action: ${intent.action}")
        }
    }
}