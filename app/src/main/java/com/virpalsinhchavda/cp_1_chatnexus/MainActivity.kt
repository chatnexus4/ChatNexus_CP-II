package com.virpalsinhchavda.cp_1_chatnexus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var logout : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            startphone()
        }
        setContentView(R.layout.activity_main)
        logout = findViewById(R.id.logout)
        logout.setOnClickListener {
                logout()
        }
    }
    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()
        startphone()
    }
    private fun startphone(){
        startActivity(Intent(this, PhonenumberVerification::class.java))
        finish()
    }
    companion object {
        fun getIntent(context: Context): Intent? {
            return Intent(context, MainActivity::class.java)
        }
    }
}




