package com.example.taller3.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.taller3.databinding.ActivityRegistroBinding
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding

    var usuario = ""
    var nombre = ""
    var contrasena = ""
    var confirmarContrasena = ""
    var apellido = ""
    var numIdentificacion = ""

    // latitud y longitud de la ubicación actual
    var latitud = 0.0
    var longitud = 0.0
    lateinit var lastLocation: Location // Última ubicación conocida
    private val permisosUbicacionRequestCode = 123 // Identificador único para la solicitud de permisos

    lateinit var locationClient: FusedLocationProviderClient // Cliente de ubicación
    lateinit var locationRequest: LocationRequest // Solicitud de ubicación
    lateinit var locationCallback: LocationCallback // Callback de ubicación

    val TAG = "GREETING_APP"
    private var isImageSelected = false

    private lateinit var uriUpload: Uri

    val getContentGallery = registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
        loadImage(it!!)
    })

    lateinit var storageRef: StorageReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallBack()

        auth = Firebase.auth // Inicializar Firebase Authentication

        // Verificar si ya se tienen permisos. si se tienen, se inicia la actualizacion de ubicacion.
        startLocationUpdates()

        setupRegisterButton()

        binding.imageView12.setOnClickListener {
            getContentGallery.launch("image/*")
        }

        binding.backButtonR.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRegisterButton() {
        binding.botonRegistrarseR.setOnClickListener {
            if (validateForm()) {
                guardarUsuario()
            } else {
                val toast = Toast.makeText(this, "Información Inválida", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    private fun guardarUsuario() {
        val correo = binding.inputCorreoR.text.toString()
        val contrasena = binding.inputContraseAR.text.toString()

        // Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(correo, contrasena).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Registro exitoso
                val user = auth.currentUser
                // Continuar con el resto de la lógica aquí
                // ...

                // Subir imagen a Firebase Storage
                uploadFirebaseImage(uriUpload)

                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
            } else {
                // Si falla el registro, manejar el error
                Log.w(TAG, "Error en el registro", task.exception)
                Toast.makeText(baseContext, "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(): Boolean {
        val nombre = binding.inputNombreR.text.toString()
        val apellido = binding.inputApellidoR.text.toString()
        val correo = binding.inputCorreoR.text.toString()
        val numIdentificacion = binding.inputIdentificacionR.text.toString()
        val contrasena = binding.inputContraseAR.text.toString()
        val confirmarContrasena = binding.inputContraseAConfirmaRr.text.toString()

        val todasLasCasillasTienenTexto =
            nombre.isNotEmpty() && apellido.isNotEmpty() && correo.isNotEmpty() && numIdentificacion.isNotEmpty() && contrasena.isNotEmpty() && confirmarContrasena.isNotEmpty()

        val emailValido = isValidEmail(correo)

        val contrasenasCoinciden = contrasena == confirmarContrasena

        return todasLasCasillasTienenTexto && contrasenasCoinciden && emailValido && isImageSelected
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()
        return email.matches(emailRegex)
    }

    fun loadImage(uri: Uri) {
        val imageStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        binding.imageView12.setImageBitmap(bitmap)
        isImageSelected = true
        uriUpload = uri
    }

    fun uploadFirebaseImage(uriUpload: Uri) {
        // Obtén una referencia al lugar donde las fotos serán guardadas
        val currentUser = auth.currentUser
        val objectId = currentUser?.uid
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("images/${objectId}.png")

        // Inicia la carga del archivo
        storageRef.putFile(uriUpload).addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
            // La carga fue exitosa, aquí puedes obtener, por ejemplo, la URL de la imagen
            val downloadUrl = taskSnapshot.metadata?.reference?.downloadUrl
            downloadUrl?.addOnSuccessListener { uri ->
                println("Imagen cargada con éxito. URL: $uri")
            }
        }.addOnFailureListener { exception: Exception ->
            // La carga falló, maneja el error
            println("Error al cargar la imagen: ${exception.message}")
        }
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

    // Función para solicitar permisos de ubicacion al usuario
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permisosUbicacionRequestCode
        )
    }
}
