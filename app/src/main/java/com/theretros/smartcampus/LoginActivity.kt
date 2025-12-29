package com.theretros.smartcampus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.theretros.smartcampus.data.checkLoginInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var emailFieldLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordFieldLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordButton: Button
    private lateinit var noAccountText: TextView
    private lateinit var signUpButton: Button
    private lateinit var elements: Array<View>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        logoImageView = findViewById(R.id.logo)
        emailFieldLayout = findViewById(R.id.emailFieldLayout)
        emailEditText = findViewById(R.id.emailField)
        passwordEditText = findViewById(R.id.passwordField)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordButton = findViewById(R.id.forgotPassword)
        noAccountText = findViewById(R.id.noAccountText)
        signUpButton = findViewById(R.id.signupButton)
        passwordFieldLayout = findViewById(R.id.passwordFieldLayout)
        // add all elements into an array
        elements = arrayOf(logoImageView, emailFieldLayout, emailEditText, passwordEditText, loginButton, forgotPasswordButton, noAccountText, signUpButton, passwordFieldLayout)
        setupListeners()
    }
    
    fun setupListeners(){
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            lifecycleScope.launch {

                val userSessionCredentials  = checkLoginInfo(email, password)

                println("userId: ${userSessionCredentials.user_id}")
                println("role: ${userSessionCredentials.role}")
                if (userSessionCredentials.user_id != 0) {

                    val sessionManager = SessionManager(this@LoginActivity)
                    sessionManager.saveSession(userSessionCredentials.user_id.toString(), userSessionCredentials.role == "Admin")

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)

                    // Optional: Finish current activity so user can't "Go Back" to login
                    finish()
                } else {
                    lifecycleScope.launch {
                        setVisibilityOfAllElements(View.INVISIBLE)
                        delay(100)
                        setVisibilityOfAllElements(View.VISIBLE)
                    }
                    passwordEditText.setText("")
                    passwordFieldLayout.error = "Invalid email or password"
                }
            }
        }
        signUpButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    fun setVisibilityOfAllElements(visibility: Int) {
        elements.forEach {
            it.visibility = visibility
        }
    }




}