package com.theretros.smartcampus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.theretros.smartcampus.data.checkLoginInfo
import com.theretros.smartcampus.data.updateUserPassword
import kotlinx.coroutines.launch

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var changeButton: MaterialButton
    private lateinit var passwordChangeField: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        changeButton = findViewById(R.id.changeButton)
        changeButton.setText("asdfasfasdfasdfas")
        passwordChangeField = findViewById(R.id.passwordChangeField)
        changeButton.setOnClickListener {
            println(245345555534)
            lifecycleScope.launch {
                println(24534534)
                val email = intent.getStringExtra("email")
                println(email)
                val credential = checkLoginInfo(email!!, null)
                val id = credential.user_id
                println(id)
                val password = passwordChangeField.text.toString()
                updateUserPassword(id.toString(), password)
                val intent = Intent(this@NewPasswordActivity, LoginActivity::class.java)
                startActivity(intent)
            }


        }
    }
}