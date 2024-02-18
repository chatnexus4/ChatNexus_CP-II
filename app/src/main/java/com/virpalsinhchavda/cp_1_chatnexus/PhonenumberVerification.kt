package com.virpalsinhchavda.cp_1_chatnexus

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import com.virpalsinhchavda.cp_1_chatnexus.databinding.ActivityPhonenumberVerificationBinding

class PhonenumberVerification : AppCompatActivity() {
    private lateinit var binding : ActivityPhonenumberVerificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phonenumber_verification)
        val numberEditText: EditText = findViewById(R.id.editTextnumber)
        val myButton: Button = findViewById(R.id.buttonContinue)

        // Set the initial background color
        myButton.setBackgroundColor(Color.parseColor("#FFDDCF51"))

        // Initialize PrefixTextWatcher
        val defaultPrefix = "+91" // Replace this with your default prefix
        numberEditText.addTextChangedListener(PrefixTextWatcher(numberEditText, defaultPrefix))

        // Set a click listener for the button
        myButton.setOnClickListener {
            val inputText = numberEditText.text.toString()

            if (inputText.length == 13) {
                // If the input length is equal to 10, navigate to the next activity
                navigateToVerificationActivity()
            } else {
                // If the input length is less than 10, show an error message
                numberEditText.error = "Minimum length of Number is 10 characters"
            }
        }
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