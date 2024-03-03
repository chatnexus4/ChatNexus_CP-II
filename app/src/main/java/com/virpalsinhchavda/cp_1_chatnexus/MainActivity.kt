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
import com.google.firebase.auth.FirebaseAuth
import com.virpalsinhchavda.cp_1_chatnexus.databinding.ActivityMainBinding
import com.virpalsinhchavda.cp_1_chatnexus.databinding.ActivityVerificationBinding

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            startphone()
        }
        setContentView(view)
        button = findViewById(R.id.logout)
        auth = FirebaseAuth.getInstance()

        button.setOnClickListener {
                auth.signOut()
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




