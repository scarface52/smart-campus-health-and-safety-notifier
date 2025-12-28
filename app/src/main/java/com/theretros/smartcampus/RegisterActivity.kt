package com.theretros.smartcampus

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.theretros.smartcampus.data.insertUser
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameField: TextInputLayout
    private lateinit var lastNameField: TextInputLayout
    private lateinit var emailField: TextInputLayout
    private lateinit var passwordField: TextInputLayout
    private lateinit var facultyField: TextInputLayout
    private lateinit var signUpButton: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nameField = findViewById(R.id.nameField)
        lastNameField = findViewById(R.id.lastNameField)
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        facultyField = findViewById(R.id.facultyField)
        signUpButton = findViewById(R.id.signUpButton)
        signUpButton.setOnClickListener { validateFields() }

    }

    fun validateFields() {
        val name = nameField.editText?.text.toString().trim()
        val lastName = lastNameField.editText?.text.toString().trim()
        val email = emailField.editText?.text.toString().trim()
        val password = passwordField.editText?.text.toString().trim()
        val faculty = facultyField.editText?.text.toString().trim()

        if (name.isEmpty()) {
            clearErrors()
            nameField.error = "Name is required"
        }
        else if (lastName.isEmpty()) {
            clearErrors()
            lastNameField.error = "Last name is required"
        }
        else if (email.isEmpty()) {
            clearErrors()
            emailField.error = "Email is required"
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            clearErrors()
            emailField.error = "Invalid email address"
        }
        else if (password.isEmpty()) {
            clearErrors()
            passwordField.error = "Password is required"
        }
        else if (faculty.isEmpty()) {
            clearErrors()
            facultyField.error = "Faculty is required"
        }
        else {
            signUp(name, lastName, email, password, faculty)
        }
    }

    fun signUp(name: String, lastName: String, email: String, password: String, faculty: String) {
        val role = "Student"
        val jurisdiction = ""
        lifecycleScope.launch {
            var userId: Int
            try {
                userId = insertUser(name, lastName, email, password, faculty, role, jurisdiction)
            } catch (e: Exception) {
                println("Error inserting user: ${e.message}")
                clearErrors()
                emailField.error = "Email already exists"
                return@launch
            }

            println("User id: $userId")
            if (userId != 0) {
                navigateToProfile(userId)
            }
        }
    }

    fun navigateToProfile(userId: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }

    fun clearErrors() {
        nameField.error = null
        lastNameField.error = null
        emailField.error = null
        passwordField.error = null
        facultyField.error = null
    }
}