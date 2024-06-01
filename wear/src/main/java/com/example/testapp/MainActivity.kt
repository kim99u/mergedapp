package com.example.testapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.testapp.databinding.ActivityMainBinding
import com.google.android.gms.wearable.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.button1.setOnClickListener {
            sendMessage("Button 1")
        }
        binding.button2.setOnClickListener {
            sendMessage("Button 2")
        }
        binding.button3.setOnClickListener {
            sendMessage("Button 3")
        }

        createNotificationChannel()

        //버튼 누르면 알림 보내기
        val buttonSendNotification: Button = findViewById(R.id.button_send_notification)
        buttonSendNotification.setOnClickListener(){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED){
                // 권한이 허용된 경우 알림을 보냅니다.
                sendNotification("Hello Wear OS", "This is a test notification")
            }
        }
    }

    private fun sendMessage(text: String) {
        val putDataMapRequest = PutDataMapRequest.create("/button_text").apply {
            dataMap.putString("button_text", text)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(putDataRequest)
    }

    private val channelId = "wear_os_channel_id"
    private val channelName = "Wear OS Channel"
    private val channelDescription = "Channel for Wear OS notifications"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
            sendNotification("Hello Wear OS", "This is a test notification")
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }

    private fun createNotificationChannel() {
        // 알림 채널은 Android 8.0 (API 26) 이상에서만 필요합니다.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }
        // 알림 채널을 시스템에 등록합니다.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendNotification(title: String, text: String) {
        val intent = Intent(this, ButtonActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "wear_os_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)
        //권한을 확인하고 알림을 보냄
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManager.notify(1, notificationBuilder.build())
            println("성공")
        }
        else{
            println("실패")
        }
    }
}