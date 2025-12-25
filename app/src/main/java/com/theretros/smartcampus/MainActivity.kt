package com.theretros.smartcampus

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import androidx.lifecycle.viewModelScope
import com.theretros.smartcampus.data.DATABASE_URL
import com.theretros.smartcampus.data.anon
import com.theretros.smartcampus.data.dataclasses.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val client = createSupabaseClient(
    supabaseUrl = DATABASE_URL,
    supabaseKey = anon
) {
    // Install the necessary modules
    install(Postgrest)
    install(Storage)
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    suspend fun InsertUser(user: User) {
        try {
            client.from("users")
                .insert(user)

            println("Data successfully inserted!")
        } catch (e: Exception) {
            println("Error inserting data: ${e.message}")
            throw e
        }
    }

    // how to use suspend functions
    fun insert(){
        // suspend functions must be inside a coroutine scope
        CoroutineScope(Dispatchers.IO).launch {

            // Data insertion
            val user = User(
                101,
                "yusuf",
                "karaca",
                "ykaraca101@hotmail.com",
                "abi57levelım",
                "engineering",
                "user",
                ""
            )
            InsertUser(user)
        }
    }

// get all users

    fun GetAllUsers() {
        val users = mutableListOf<User>()
        CoroutineScope(Dispatchers.IO).launch {
            val result = client.from("users").select().decodeList<User>()
            users.addAll(result)
            for (user in users) {
                println(user)
            }
        }
    }
}