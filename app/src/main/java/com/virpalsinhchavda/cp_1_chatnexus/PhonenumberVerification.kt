package com.virpalsinhchavda.cp_1_chatnexus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.virpalsinhchavda.cp_1_chatnexus.databinding.ActivityPhonenumberVerificationBinding
import java.util.concurrent.TimeUnit

class PhonenumberVerification : AppCompatActivity() {
    private lateinit var binding : ActivityPhonenumberVerificationBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var number: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhonenumberVerificationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val numberEditText: EditText = findViewById(R.id.editTextnumber)
        val myButton: Button = findViewById(R.id.buttonContinue)
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)
        // Set the initial background color
        myButton.setBackgroundColor(Color.parseColor("#FFDDCF51"))

        // Initialize PrefixTextWatcher
        val defaultPrefix = "+91" // Replace this with your default prefix
        numberEditText.addTextChangedListener(PrefixTextWatcher(numberEditText, defaultPrefix))

        number = findViewById<EditText>(R.id.editTextnumber).text.toString()
        binding.buttonContinue.setOnClickListener {

            if (number.isNotEmpty()){
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(number) // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this) // Activity (for callback binding)
                    .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
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
            val intent = Intent(this@PhonenumberVerification,verification::class.java)
            intent.putExtra("OTP",verificationId)
            intent.putExtra("resendToken",token)
            intent.putExtra("phoneNumber",number)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", true)
            editor.apply()

            // Go to main activity
            startActivity(MainActivity.getIntent(this))
            finish()
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
    // navigate to the Verification activity if valid
    private fun navigateToVerificationActivity() {
        val intent = Intent(this, verification::class.java)
        startActivity(intent)

    }
}
class PrefixTextWatcher(private val editText: EditText, private val defaultPrefix: String) :
    TextWatcher {
    private var isEditing = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No action needed
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // No action needed
    }

    override fun afterTextChanged(editable: Editable?) {
        if (isEditing) return
        isEditing = true

        val text = editable.toString()
        if (!text.startsWith(defaultPrefix)) {
            editText.setText(defaultPrefix)
            editText.setSelection(defaultPrefix.length) // Place cursor at the end
        }

        isEditing = false
    }

}