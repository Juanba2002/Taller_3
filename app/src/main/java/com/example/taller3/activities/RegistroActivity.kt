package com.example.taller3.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.taller3.R
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegistroActivity : AppCompatActivity() {
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var auth: FirebaseAuth

    private var latitud = 0.0
    private var longitud = 0.0
    private lateinit var lastLocation: Location

    private val permisosUbicacionRequestCode = 123
    private val TAG = "GREETING_APP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallBack()

        auth = Firebase.auth

        startLocationUpdates()

        setupRegisterButton()

        findViewById<ImageButton>(R.id.backButtonR).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRegisterButton() {
        findViewById<Button>(R.id.botonRegistrarseR).setOnClickListener {
            if (validateForm()) {
                guardarUsuario()
            } else {
                val toast = Toast.makeText(this, "Información Inválida", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }


    private fun guardarUsuario() {
        val correo = findViewById<EditText>(R.id.inputCorreoR).text.toString()
        val contrasena = findViewById<EditText>(R.id.inputContraseñaR).text.toString()
        auth.createUserWithEmailAndPassword(correo, contrasena).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val user = auth.currentUser
                    // Continue with the rest of the logic here
                    // ...

                    // Upload image to Firebase Storage (Add this logic)

                    val intent = Intent(this, MapActivity::class.java)
                    startActivity(intent)
                } else {
                    // If registration fails, handle the error
                    Log.w(TAG, "Error en el registro", task.exception)
                    Toast.makeText(baseContext, "Error en el registro", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateForm(): Boolean {
        val nombre = findViewById<EditText>(R.id.inputNombreR).text.toString()
        val apellido = findViewById<EditText>(R.id.inputApellidoR).text.toString()
        val correo = findViewById<EditText>(R.id.inputCorreoR).text.toString()
        val numIdentificacion = findViewById<EditText>(R.id.inputIdentificacionR).text.toString()
        val contrasena = findViewById<EditText>(R.id.inputContraseñaR).text.toString()
        val confirmarContrasena = findViewById<EditText>(R.id.inputContraseñaConfirmaRr).text.toString()

        val todasLasCasillasTienenTexto =
            nombre.isNotEmpty() && apellido.isNotEmpty() && correo.isNotEmpty() && numIdentificacion.isNotEmpty() && contrasena.isNotEmpty() && confirmarContrasena.isNotEmpty()

        val emailValido = isValidEmail(correo)
        val contrasenasCoinciden = contrasena == confirmarContrasena

        return todasLasCasillasTienenTexto && contrasenasCoinciden && emailValido
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()
        return email.matches(emailRegex)
    }

    fun createLocationRequest(): LocationRequest {
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(1000).build()
        return locationRequest
    }

    fun createLocationCallBack(): LocationCallback {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (result != null) {
                    lastLocation = result.lastLocation!!
                    latitud = lastLocation.latitude
                    longitud = lastLocation.longitude
                }
            }
        }
        return locationCallback
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permisosUbicacionRequestCode
        )
    }
}
