package com.virpalsinhchavda.cp_1_chatnexus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText

class verification : AppCompatActivity() {
    private lateinit var otpFields: Array<EditText>
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var button : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)
        button = findViewById(R.id.OTPverification)
        otpFields = arrayOf(
            findViewById(R.id.OTP1),
            findViewById(R.id.OTP2),
            findViewById(R.id.OTP3),
            findViewById(R.id.OTP4),
            findViewById(R.id.OTP5),
            findViewById(R.id.OTP6)
        )
        setListeners()
        button.setOnClickListener {
            login()
        }

    }
    private fun setListeners() {
        for (i in 0 until otpFields.size) {
            val editText = otpFields[i]
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        // Move focus to the next EditText field
                        otpFields[i + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && i > 0 && editText.text.isEmpty()) {
                    // If backspace is pressed on an empty field, move focus to the previous field
                    otpFields[i - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }
    }
    private fun login() {
        // Perform login authentication

        // Once logged in successfully, set isLoggedIn to true
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.apply()

        // Go to main activity
        startActivity(MainActivity.getIntent(this))
        finish() // Prevents user from going back to LoginActivity
    }
}