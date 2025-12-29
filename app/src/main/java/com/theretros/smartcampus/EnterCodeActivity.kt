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
import com.theretros.smartcampus.data.checkLoginInfo
import kotlinx.coroutines.launch

class EnterCodeActivity : AppCompatActivity() {

    private lateinit var sendButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_enter_code)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sendButton = findViewById(R.id.sendCodeButton)
        sendButton.setOnClickListener {
            val email = intent.getStringExtra("email")
            val intent = Intent(this@EnterCodeActivity, NewPasswordActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)

        }
    }
}