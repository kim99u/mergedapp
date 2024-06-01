package com.example.testapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.testapp.R

class ButtonActivity :  ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button)

        val buttonIds = listOf(
            R.id.button1, R.id.button2, R.id.button3,
//            R.id.button4, R.id.button5,
//            R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button10
        )

        for (id in buttonIds){
            val button : Button = findViewById(id)
            button.setOnClickListener(){
                val buttonText = button.text.toString()
                // 버튼을 눌렀을 때 할 행위 구현

                Toast.makeText(this, buttonText, Toast.LENGTH_SHORT).show()
            }
        }
    }


}