package com.theretros.smartcampus.data

import com.theretros.smartcampus.client
import com.theretros.smartcampus.data.dataclasses.User
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



val client = createSupabaseClient(
    supabaseUrl = DATABASE_URL,
    supabaseKey = anon
) {
    // Install the necessary modules
    install(Postgrest)
    install(Storage)
}

// Insert User Function
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
            35,
            "yusuf",
            "karaca",
            "ykaraca@hotmail.com",
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