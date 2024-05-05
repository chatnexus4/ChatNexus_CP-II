package com.virpalsinhchavda.cp_1_chatnexus

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class VerificationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var verifyBtn: Button
    private lateinit var resendTV: TextView
    private lateinit var inputOTP1: EditText
    private lateinit var inputOTP2: EditText
    private lateinit var inputOTP3: EditText
    private lateinit var inputOTP4: EditText
    private lateinit var inputOTP5: EditText
    private lateinit var inputOTP6: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var OTP: String
    private lateinit var phoneNumber: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var countDown: Int = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        auth = FirebaseAuth.getInstance()

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber")!!

        init()
        progressBar.visibility = View.INVISIBLE
        addTextChangeListener()
        resendOTPTvVisibility()

        resendTV.setOnClickListener {
            resendVerificationCode()
            resendOTPTvVisibility()
        }
        resendTV.text = getString(R.string.resend_otp_countdown, countDown)

        verifyBtn.setOnClickListener {
            val typedOTP = (inputOTP1.text.toString() + inputOTP2.text.toString() +
                    inputOTP3.text.toString() + inputOTP4.text.toString() +
                    inputOTP5.text.toString() + inputOTP6.text.toString())

            if (typedOTP.isNotEmpty()) {
                if (typedOTP.length == 6) {
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(OTP, typedOTP)
                    progressBar.visibility = View.VISIBLE
                    signInWithPhoneAuthCredential(credential)
                } else {
                    Toast.makeText(this, "Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }

        inputOTP1.requestFocus()
    }

    private fun resendOTPTvVisibility() {
        inputOTP1.setText("")
        inputOTP2.setText("")
        inputOTP3.setText("")
        inputOTP4.setText("")
        inputOTP5.setText("")
        inputOTP6.setText("")
        resendTV.visibility = View.INVISIBLE
        resendTV.isEnabled = false

        // Start the resend countdown
        startResendCountdown()
    }

    private fun startResendCountdown() {
        val handler = Handler(Looper.myLooper()!!)
        val runnable = object : Runnable {
            override fun run() {
                if (countDown > 0) {
                    resendTV.text = getString(R.string.resend_otp_countdown, countDown)
                    countDown--
                    handler.postDelayed(this, 1000) // Run every second
                } else {
                    resendTV.visibility = View.VISIBLE
                    resendTV.isEnabled = true
                    resendTV.text = "Resend OTP"
                }
            }
        }
        handler.post(runnable)
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification.
            // 2 - Auto-retrieval.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            OTP = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        progressBar.visibility = View.VISIBLE
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    val uid = user?.uid

                    // Store phone number in Firebase Realtime Database under 'phonenumber' branch
                    uid?.let { userId ->
                        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(phoneNumber)
                        userRef.child("phonenumber").setValue(phoneNumber) // Store phone number
                    }

                    // Redirect to the Username activity
                    sendToUsername()
                } else {
                    // Sign in failed
                    Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE // Hide progress bar on sign-in failure
                }
            }
    }

    private fun sendToUsername() {
        startActivity(Intent(this, MessageActivity::class.java))
    }

    private fun addTextChangeListener() {
        inputOTP1.addTextChangedListener(EditTextWatcher(inputOTP1))
        inputOTP2.addTextChangedListener(EditTextWatcher(inputOTP2))
        inputOTP3.addTextChangedListener(EditTextWatcher(inputOTP3))
        inputOTP4.addTextChangedListener(EditTextWatcher(inputOTP4))
        inputOTP5.addTextChangedListener(EditTextWatcher(inputOTP5))
        inputOTP6.addTextChangedListener(EditTextWatcher(inputOTP6))
    }

    private fun init() {
        verifyBtn = findViewById(R.id.verifyOTPBtn)
        resendTV = findViewById(R.id.resendTextView)
        progressBar = findViewById(R.id.otpProgressBar)
        inputOTP1 = findViewById(R.id.otpEditText1)
        inputOTP2 = findViewById(R.id.otpEditText2)
        inputOTP3 = findViewById(R.id.otpEditText3)
        inputOTP4 = findViewById(R.id.otpEditText4)
        inputOTP5 = findViewById(R.id.otpEditText5)
        inputOTP6 = findViewById(R.id.otpEditText6)
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            val text = p0.toString()
            when (view.id) {
                R.id.otpEditText1 -> {
                    if (text.length == 1) {
                        inputOTP2.requestFocus()
                    }
                }
                R.id.otpEditText2 -> {
                    if (text.length == 1) {
                        inputOTP3.requestFocus()
                    } else if (text.isEmpty()) {
                        inputOTP1.requestFocus()
                    }
                }
                R.id.otpEditText3 -> {
                    if (text.length == 1) {
                        inputOTP4.requestFocus()
                    } else if (text.isEmpty()) {
                        inputOTP2.requestFocus()
                    }
                }
                R.id.otpEditText4 -> {
                    if (text.length == 1) {
                        inputOTP5.requestFocus()
                    } else if (text.isEmpty()) {
                        inputOTP3.requestFocus()
                    }
                }
                R.id.otpEditText5 -> {
                    if (text.length == 1) {
                        inputOTP6.requestFocus()
                    } else if (text.isEmpty()) {
                        inputOTP4.requestFocus()
                    }
                }
                R.id.otpEditText6 -> {
                    if (text.isEmpty() && p0.isNullOrEmpty()) {
                        inputOTP5.requestFocus()
                    } else if (text.length == 1) {
                        inputOTP6.clearFocus()
                    }
                }
            }

            // Transfer input to previous EditText on backspace press
            if (text.isEmpty() && !p0.isNullOrEmpty()) {
                when (view.id) {
                    R.id.otpEditText2 -> inputOTP1.requestFocus()
                    R.id.otpEditText3 -> inputOTP2.requestFocus()
                    R.id.otpEditText4 -> inputOTP3.requestFocus()
                    R.id.otpEditText5 -> inputOTP4.requestFocus()
                    R.id.otpEditText6 -> inputOTP5.requestFocus()
                }
            }
        }
    }
}
