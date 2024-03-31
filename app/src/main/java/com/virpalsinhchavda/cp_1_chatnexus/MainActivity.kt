package com.virpalsinhchavda.cp_1_chatnexus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var signOutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val otpCompleted = sharedPreferences.getBoolean("otp_completed", false)

        if (!otpCompleted) {
            // Redirect user back to OTPActivity
            val intent = Intent(this, VerificationActivity::class.java)
            startActivity(intent)
            finish() // Finish MainActivity to prevent user from returning with back button
            return
        }

        // Continue with MainActivity initialization
        setContentView(R.layout.activity_main)


        auth = FirebaseAuth.getInstance()
        signOutBtn = findViewById(R.id.logout)

        signOutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, PhonenumberVerification::class.java))
        }
    }
}