package com.example.taller3.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance();

        auth = Firebase.auth

        // Autenticación automática con Firebase Authentication
        loginFirebase()

        // Escucha el evento del botón de login
        // Autenticación con usuario y contraseña, con Firebase Authentication
        setupLoginButton()

        // Escucha el evento del botón de registro
        setupRegisterButton()
    }

    private fun loginFirebase() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("sessionToken", null)

        if (sessionToken != null) {
            // Iniciar sesión automáticamente con el token de sesión
            auth.signInWithCustomToken(sessionToken).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registro exitoso
                        Log.i("FIREBASE", "Inicio de sesión automático exitoso")
                        val intent = Intent(this, MapActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Si falla el inicio de sesión, manejar el error
                        Log.w("FIREBASE", "Error en el inicio de sesión automático", task.exception)
                        eliminarTokenDeSesion()
                    }
                }
        } else {
            // Iniciar sesión anónima con Firebase Authentication
            auth.signInAnonymously().addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Inicio de sesión anónimo exitoso
                        Log.i("FIREBASE", "Inicio de sesión anónimo exitoso")
                    } else {
                        // Si falla el inicio de sesión anónimo, manejar el error
                        Log.w("FIREBASE", "Error en el inicio de sesión anónimo", task.exception)
                    }
                }
        }
    }

    private fun eliminarTokenDeSesion() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("sessionToken")
        editor.apply()

        // Iniciar sesión anónima con Firebase Authentication
        auth.signInAnonymously().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión anónimo exitoso
                    Log.i("FIREBASE", "Inicio de sesión anónimo exitoso")
                } else {
                    // Si falla el inicio de sesión anónimo, manejar el error
                    Log.w("FIREBASE", "Error en el inicio de sesión anónimo", task.exception)
                }
            }
    }

    private fun validateForm(): Boolean {
        val usuario = binding.inputUsuario.text.toString()
        val contrasena = binding.inputContrasena.text.toString()

        val todasLasCasillasTienenTexto = usuario.isNotEmpty() && contrasena.isNotEmpty()

        return todasLasCasillasTienenTexto
    }

    private fun setupLoginButton() {
        binding.botonIniciarSesion.setOnClickListener {
            if (validateForm()) {
                val usuario = binding.inputUsuario.text.toString()
                val contrasena = binding.inputContrasena.text.toString()

                // Autenticación con Firebase Authentication
                auth.signInWithEmailAndPassword(usuario, contrasena).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Inicio de sesión exitoso
                            Log.i("FIREBASE", "Inicio de sesión exitoso")
                            val user = auth.currentUser
                            val intent = Intent(this, MapActivity::class.java)
                            startActivity(intent)
                        } else {
                            // Si falla el inicio de sesión, manejar el error
                            Log.w("FIREBASE", "Error en el inicio de sesión", task.exception)
                            val toast = Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT)
                            toast.show()
                        }
                    }
            } else {
                val toast = Toast.makeText(this, "Campos incompletos", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    private fun setupRegisterButton() {
        binding.botonRegistrarse.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }
}
