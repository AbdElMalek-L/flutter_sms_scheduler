package com.example.flutter_sms_scheduler

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterSmsSchedulerPlugin */
class FlutterSmsSchedulerPlugin : FlutterPlugin, MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_sms_scheduler")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }

      "scheduleSms" -> {
        val phoneNumber = call.argument<String>("phoneNumber")
        val message = call.argument<String>("message")
        val timestamp = call.argument<Long>("scheduledTime")

        if (phoneNumber != null && message != null && timestamp != null) {
          SmsUtil.scheduleSms(context, phoneNumber, message, timestamp)
          result.success(null)
        } else {
          result.error("INVALID_ARGUMENTS", "Missing phoneNumber, message, or timestamp", null)
        }
      }

      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
