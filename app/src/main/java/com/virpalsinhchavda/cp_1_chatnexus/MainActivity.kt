package com.virpalsinhchavda.cp_1_chatnexus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // User is already logged in, continue with MainActivity
            // Add your MainActivity code here
        } else {
            // User is not logged in, redirect to PhoneNumberVerificationActivity
            startActivity(Intent(this, PhonenumberVerification::class.java))
            finish() // Finish MainActivity to prevent user from returning with back button
        }

        /*val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val otpCompleted = sharedPreferences.getBoolean("otp_completed", false)

        if (otpCompleted) {
            // Redirect user to MainActivity directly
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Finish MainActivity to prevent user from returning with back button
        } else {
            // Redirect user to PhoneNumberActivity
            startActivity(Intent(this, PhonenumberVerification::class.java))
            finish() // Finish MainActivity to prevent user from returning with back button
        }*/
    }
}