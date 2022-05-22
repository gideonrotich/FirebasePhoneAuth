package com.kanyideveloper.firebasephoneauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.kanyideveloper.firebasephoneauth.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storedVerificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btngenerateOTP.setOnClickListener {
            if (TextUtils.isEmpty(binding.phone.editText?.text.toString().trim())) {
                binding.phone.error = "Please enter a phone number"
                return@setOnClickListener
            }
            binding.progressBar.isVisible = true
            sendVerificationCode(binding.phone.editText?.text.toString().trim().drop(0))
        }

        binding.btnverifyOTP.setOnClickListener {
            if (TextUtils.isEmpty(binding.otp.text.toString().trim())) {
                binding.otp.error = "Please enter an OTP sent to your phone"
                return@setOnClickListener
            }
            verifyCode(binding.otp.text.toString().trim())
        }

    }

    private fun verifyCode(code: String) {
        val credentials = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInByCredentials(credentials)
    }

    private fun signInByCredentials(credentials: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credentials)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber("+254$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            val code = credential.smsCode

            if (code != null) {
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
            binding.btnverifyOTP.isEnabled = false
            binding.progressBar.isVisible = false
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            storedVerificationId = verificationId
            binding.btnverifyOTP.isEnabled = true
            binding.progressBar.isVisible = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null){
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}