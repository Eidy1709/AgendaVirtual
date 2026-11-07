package com.example.agendavirtual

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.agendavirtual.data.AppDatabase
import com.example.agendavirtual.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getDatabase(this)

        val etRegUsername = findViewById<EditText>(R.id.etRegUsername)
        val etRegPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val etLoginUsername = findViewById<EditText>(R.id.etLoginUsername)
        val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnRegister.setOnClickListener {
            val username = etRegUsername.text.toString().trim()
            val password = etRegPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos de registro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val existing = db.userDao().getUserByUsername(username)
                    if (existing != null) {
                        withContext(Dispatchers.Main) {
                            tvStatus.text = "Error: El usuario ya existe"
                            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                        }
                    } else {
                        db.userDao().insertUser(User(username = username, email = "$username@test.com", password = password))
                        withContext(Dispatchers.Main) {
                            tvStatus.text = "¡Usuario registrado con éxito!"
                            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                            etRegUsername.setText("")
                            etRegPassword.setText("")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "Error al registrar: ${e.message}"
                        tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                    }
                }
            }
        }

        btnLogin.setOnClickListener {
            val username = etLoginUsername.text.toString().trim()
            val password = etLoginPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos de login", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val user = db.userDao().login(username, password)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        tvStatus.text = "¡Login exitoso! Bienvenido, ${user.username}"
                        tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                        etLoginUsername.setText("")
                        etLoginPassword.setText("")
                    } else {
                        tvStatus.text = "Error: Usuario o contraseña incorrectos"
                        tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                    }
                }
            }
        }
    }
}
