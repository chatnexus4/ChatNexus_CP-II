package com.virpalsinhchavda.cp_1_chatnexus

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.virpalsinhchavda.cp_1_chatnexus.R
import com.virpalsinhchavda.cp_1_chatnexus.User
import com.virpalsinhchavda.cp_1_chatnexus.MainActivity
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID // Import UUID to generate random IDs

@Suppress("DEPRECATION")
class Username : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var circularImageView: ImageView
    private lateinit var submitButton: Button
    private lateinit var mDatabase: DatabaseReference
    private lateinit var phoneNumber: String

    private var selectedImage: Bitmap? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    val circularBitmap = getCircularBitmap(bitmap)
                    circularImageView.setImageBitmap(circularBitmap)
                    selectedImage = bitmap
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_username)

        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        usernameInput = findViewById(R.id.username_input)
        circularImageView = findViewById(R.id.circularImageView)
        submitButton = findViewById(R.id.buttonDone)

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().reference

        circularImageView.setOnClickListener {
            openGallery()
        }

        submitButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()

            if (username.isNotEmpty() && selectedImage != null) {
                // Upload profile photo to Firebase Storage
                uploadProfilePhoto(selectedImage!!) { profilePhotoUrl ->
                    if (profilePhotoUrl != null) {
                        // Profile photo uploaded successfully, store user data in Realtime Database
                        saveUserDataInDatabase(username, profilePhotoUrl, phoneNumber) { success ->
                            if (success) {
                                // Show a toast message indicating successful data submission
                                Toast.makeText(
                                    this,
                                    "User data submitted successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Redirect to the MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                                finish() // Finish the current activity to prevent going back to it when pressing back button
                            } else {
                                // Failed to store user data in Firebase
                                Toast.makeText(
                                    this,
                                    "Failed to store user data.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        // Failed to upload profile photo
                        Toast.makeText(
                            this,
                            "Failed to upload profile photo.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // Show an error message if either username or image is empty
                Toast.makeText(
                    this,
                    "Please enter your username and select an image.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveUserDataInDatabase(username: String, photoUrl: String, phoneNumber: String, callback: (Boolean) -> Unit) {
        if (username.isNotEmpty() && photoUrl.isNotEmpty() && phoneNumber.isNotEmpty()) {
            // Generate a unique user ID
            val userId = UUID.randomUUID().toString()

            // Store the received data (username and photoUrl) along with the phone number in the Realtime Database
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            val userData = hashMapOf(
                "username" to username,
                "photoUrl" to photoUrl,
                "phoneNumber" to phoneNumber
            )
            userRef.setValue(userData)
                .addOnSuccessListener {
                    callback(true) // Data stored successfully
                }
                .addOnFailureListener { exception ->
                    callback(false) // Failed to store data
                }

            // Redirect or perform the necessary action after storing user data
        } else {
            // Show an error message if any of the required fields are empty
            Toast.makeText(this, "Please provide username, photo URL, and phone number.", Toast.LENGTH_SHORT).show()
            callback(false) // Data not stored due to missing fields
        }
    }



    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(galleryIntent)
    }

    private fun uploadProfilePhoto(bitmap: Bitmap, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val profilePhotoRef = storageRef.child("profile_photos/${UUID.randomUUID()}.jpg")

        // Convert Bitmap to byte array
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Upload profile photo to Firebase Storage
        profilePhotoRef.putBytes(imageData)
            .addOnSuccessListener { taskSnapshot ->
                // Profile photo uploaded successfully, get the download URL
                profilePhotoRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }.addOnFailureListener {
                    // Failed to get profile photo download URL
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                // Profile photo upload failed
                callback(null)
            }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, bitmap.width / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }
}

