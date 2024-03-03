package com.virpalsinhchavda.cp_1_chatnexus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.virpalsinhchavda.cp_1_chatnexus.databinding.ActivityVerificationBinding
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class verification : AppCompatActivity() {
    private lateinit var otpFields: Array<EditText>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityVerificationBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var OTP :String
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)

        otpFields = arrayOf(
            findViewById(R.id.OTP1),
            findViewById(R.id.OTP2),
            findViewById(R.id.OTP3),
            findViewById(R.id.OTP4),
            findViewById(R.id.OTP5),
            findViewById(R.id.OTP6)
        )
        setListeners()

        auth = FirebaseAuth.getInstance()

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber")!!.toString()
        binding.OTPverification.setOnClickListener {
            //login()
            val typedOTP = otpFields[0].text.toString() + otpFields[1].text.toString() + otpFields[2].text.toString() +
                            otpFields[3].text.toString() + otpFields[4].text.toString()+ otpFields[5].text.toString()
            if (typedOTP.isNotEmpty()){
                if (typedOTP.length == 6){
                        val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                            OTP,typedOTP
                        )
                    signInWithPhoneAuthCredential(credential)
                }else{
                    Toast.makeText(this,"Please Enter Correct OTP",Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this,"Please Fill All the OTP Fields",Toast.LENGTH_LONG).show()
            }


        }

        binding.ResendOTP.setOnClickListener{
            resendVerificationCode()
        }
    }
    private fun resendVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("OnVerificationFailed", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("OnVerificationFailed", "TooManyRequestException: ${e.toString()}")
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            // Save verification ID and resending token so we can use them later\
            OTP = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this,"Authentication Successfull",Toast.LENGTH_LONG).show()
                    login()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("singinwithphoneauthcredential", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
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