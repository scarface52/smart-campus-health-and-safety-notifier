package com.theretros.smartcampus

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var emailField: TextInputEditText
    private lateinit var sendCodeButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailField = findViewById(R.id.emailField)
        sendCodeButton = findViewById(R.id.sendCodeButton)
        setupListeners()
    }

    fun setupListeners() {
        sendCodeButton.setOnClickListener {
            val email = emailField.text.toString()
            val intent = Intent(this, EnterCodeActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }
}