package com.example.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
    }

    private fun sendMessage(text: String) {
        val putDataMapRequest = PutDataMapRequest.create("/button_text").apply {
            dataMap.putString("button_text", text)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(putDataRequest)
    }
}