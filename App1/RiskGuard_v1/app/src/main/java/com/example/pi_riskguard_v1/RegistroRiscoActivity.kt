package com.example.pi_riskguard_v1

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.*
import android.os.Looper
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import java.io.IOException

class RegistroRiscoActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 100
    private val LOCATION_PERMISSION_REQUEST_CODE = 200
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Firebase
    private lateinit var database: DatabaseReference

    // Dados temporários
    private var selectedImageUri: Uri? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_registro_risco)

        // Firebase Database
        database = FirebaseDatabase.getInstance().getReference("Riscos")

        // Spinner de tipo de risco
        val spinnerTipo: Spinner = findViewById(R.id.spinner_tipo)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipos_de_risco,
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerTipo.adapter = adapter
        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                Log.d("TipoRiscoSelecionado", "Selecionado: ${parent.getItemAtPosition(pos)}")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // "Clique aqui" leva à descrição
        findViewById<TextView>(R.id.clique_aqui).setOnClickListener {
            startActivity(Intent(this, DescricaoRiscosActivity::class.java))
        }

        // Ícone de menu
        findViewById<ImageView>(R.id.feather_icon).setOnClickListener { showCustomMenu() }

        // BOTÃO ENVIAR
        val btn_enviar: Button = findViewById(R.id.btn_enviar)

        // Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifica permissão e solicita caso necessário
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            obterLocalizacao()
        }

        btn_enviar.setOnClickListener {
            // Solicita permissão se necessário
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                obterLocalizacao()
            }
            // Envia os dados para o Firebase
            enviarRiscoParaFirebase()
        }

        // CALENDÁRIO
        val dataContainer: RelativeLayout = findViewById(R.id.data_container)
        val textData: TextView = findViewById(R.id.text_data)

        // Obtém a data de hoje
        val c = Calendar.getInstance()
        val dia = c.get(Calendar.DAY_OF_MONTH)
        val mes = c.get(Calendar.MONTH) + 1 // Janeiro = 0, por isso soma 1
        val ano = c.get(Calendar.YEAR)

        // Define a data de hoje no TextView
        textData.text = "%02d/%02d/%04d".format(dia, mes, ano)

        // Clique para abrir a galeria
        val imageContainer: RelativeLayout = findViewById(R.id.container_imagem)
        val imageView: ImageView = findViewById(R.id.icon_imagem)
        imageContainer.setOnClickListener { openGallery() }
    }

    private fun obterLocalizacao() {
        val spinnerCoordenadas = findViewById<Spinner>(R.id.spinner_coordenadas)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                val texto = "Latitude: $latitude\nLongitude: $longitude"
                val coordenadas = listOf(texto)
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, coordenadas)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCoordenadas.adapter = adapter
            } else {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    2000L
                )
                    .setMaxUpdates(1)
                    .build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val novaLocalizacao = locationResult.lastLocation
                        if (novaLocalizacao != null) {
                            latitude = novaLocalizacao.latitude
                            longitude = novaLocalizacao.longitude
                            val texto = "Latitude: $latitude\nLongitude: $longitude"
                            val coordenadas = listOf(texto)
                            val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, coordenadas)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinnerCoordenadas.adapter = adapter
                        } else {
                            Toast.makeText(applicationContext, "Localização ainda indisponível", Toast.LENGTH_SHORT).show()
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    // Dispara a galeria para selecionar uma imagem
    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            it.type = "image/*"
            startActivityForResult(it, PICK_IMAGE_REQUEST)
        }
    }

    // Recebe o resultado da galeria
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                try {
                    val bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    findViewById<ImageView>(R.id.icon_imagem).setImageBitmap(bmp)
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun encodeImageToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Redimensiona a imagem antes de converter
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)

            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream) // 20% de qualidade

            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP) // tira quebras de linha
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }



    // ENVIO PARA O FIREBASE
    private fun enviarRiscoParaFirebase() {
        val spinnerTipo: Spinner = findViewById(R.id.spinner_tipo)
        val tipoRisco = spinnerTipo.selectedItem?.toString() ?: ""
        val descricao = findViewById<EditText>(R.id.descricao_input).text.toString()
        val data = findViewById<TextView>(R.id.text_data).text.toString()
        val lat = latitude
        val lon = longitude

        if (tipoRisco.isBlank() || descricao.isBlank() || data.isBlank() || lat == null || lon == null) {
            Toast.makeText(this, "Preencha todos os campos e aguarde a localização.", Toast.LENGTH_SHORT).show()
            return
        }

        // Se a imagem foi selecionada, converte para Base64 e envia pro Firebase
        if (selectedImageUri != null) {
            val imageUrl = encodeImageToBase64(selectedImageUri!!)
            if (imageUrl != null) {
                salvarRiscoNoDatabase(tipoRisco, descricao, data, lat, lon, imageUrl)
            } else {
                Toast.makeText(this, "Erro ao converter imagem", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Se não tiver imagem, salva sem imagem
            salvarRiscoNoDatabase(tipoRisco, descricao, data, lat, lon, null)
        }
    }
    private fun salvarRiscoNoDatabase(
        tipoRisco: String,
        descricao: String,
        data: String,
        latitude: Double,
        longitude: Double,
        imageUrl: String?
    ) {
        val riscoId = database.push().key ?: UUID.randomUUID().toString()
        val risco = mapOf(
            "tipoRisco" to tipoRisco,
            "descricao" to descricao,
            "data" to data,
            "latitude" to latitude,
            "longitude" to longitude,
            "imagemUrl" to (imageUrl ?: "")
        )
        database.child(riscoId).setValue(risco)
            .addOnSuccessListener {
                Toast.makeText(this, "Risco registrado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, TelaTipoRiscosActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao registrar risco.", Toast.LENGTH_SHORT).show()
            }
    }

    // Menu customizado com clique no 'icon_tipo' para abrir TelaTipoRiscosActivity
    private fun showCustomMenu() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.menu)

        val registro = dialog.findViewById<LinearLayout>(R.id.layout_registro)
        val tipo     = dialog.findViewById<LinearLayout>(R.id.layout_tipo)
        val sair     = dialog.findViewById<LinearLayout>(R.id.layout_sair)
        val iconTipo = dialog.findViewById<ImageView>(R.id.icon_tipo)

        registro.setOnClickListener {
            startActivity(Intent(this, RegistroRiscoActivity::class.java))
            dialog.dismiss()
        }
        tipo.setOnClickListener {
            startActivity(Intent(this, TelaTipoRiscosActivity::class.java))
            dialog.dismiss()
        }
        sair.setOnClickListener {
            Toast.makeText(this, "Saindo...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        iconTipo.setOnClickListener {
            startActivity(Intent(this, DescricaoRiscosActivity::class.java))
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                obterLocalizacao()
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class DescricaoRiscoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_descricao_riscos)
    }
}