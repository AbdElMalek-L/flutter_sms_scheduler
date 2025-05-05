package alarm.manager.demo

import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import alarm.manager.demo.ui.theme.AlarmManagerDemoTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SmsSenderScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSendSmsClicked = {
                            if (SmsUtil.checkSmsPermission(this)) {
                                SmsUtil.sendSms(this)
                            }
                        },
                        onScheduleSmsClicked = { delaySeconds ->
                            if (SmsUtil.checkSmsPermission(this)) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    checkAndRequestExactAlarmPermission(delaySeconds)
                                } else {
                                    SmsUtil.scheduleSmsWithDelay(this, delaySeconds)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    
    private fun checkAndRequestExactAlarmPermission(delaySeconds: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Requesting exact alarm permission")
                Toast.makeText(
                    this,
                    "Please enable exact alarms permission for this app",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            } else {
                Log.d(TAG, "Exact alarm permission already granted")
                SmsUtil.scheduleSmsWithDelay(this, delaySeconds)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Check if we returned from alarm permission settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Exact alarm permission granted after returning to the app")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "SMS permission granted")
                SmsUtil.sendSms(this)
            } else {
                Log.d(TAG, "SMS permission denied")
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SmsSenderScreen(
    modifier: Modifier = Modifier, 
    onSendSmsClicked: () -> Unit,
    onScheduleSmsClicked: (Int) -> Unit
) {
    var schedulingTime by remember { mutableStateOf("10") }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onSendSmsClicked
        ) {
            Text(text = "Send SMS to 3030")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = schedulingTime,
            onValueChange = { 
                // Only allow numeric input
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    schedulingTime = it
                }
            },
            label = { Text("Delay in seconds") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                val seconds = schedulingTime.toIntOrNull() ?: 10
                onScheduleSmsClicked(seconds)
            }
        ) {
            Text(text = "Schedule SMS")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmsSenderScreenPreview() {
    AlarmManagerDemoTheme {
        SmsSenderScreen(
            onSendSmsClicked = {},
            onScheduleSmsClicked = {}
        )
    }
}