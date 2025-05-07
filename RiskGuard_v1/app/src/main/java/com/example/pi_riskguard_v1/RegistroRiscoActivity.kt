package com.example.pi_riskguard_v1

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import java.util.*
import android.os.Looper
import android.widget.Spinner
import android.widget.Toast
import com.google.android.gms.location.*

class RegistroRiscoActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 100
    private val LOCATION_PERMISSION_REQUEST_CODE = 200
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_registro_risco)

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
            obterLocalizacao() // Se a permissão já foi dada, obtém a localização
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
                obterLocalizacao() // Obtém localização se a permissão for concedida
            }
        }

        // CALENDÁRIO
        val dataContainer: RelativeLayout = findViewById(R.id.data_container)
        val textData: TextView             = findViewById(R.id.text_data)
        dataContainer.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this,
                { _, y, m, d ->
                    textData.text = "%02d/%02d/%04d".format(d, m+1, y)
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // GALERIA DE IMAGENS
        val imageContainer: RelativeLayout = findViewById(R.id.container_imagem)
        val imageView: ImageView           = findViewById(R.id.icon_imagem)
        imageContainer.setOnClickListener { openGallery() }
    }

    private fun obterLocalizacao() {
        val spinnerCoordenadas = findViewById<Spinner>(R.id.spinner_coordenadas)

        // Verificando permissões
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

        // Obtendo a última localização
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val texto = "Latitude: $latitude\nLongitude: $longitude"

                // Preenche o Spinner com as coordenadas
                val coordenadas = listOf("Latitude: $latitude\nLongitude: $longitude")
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, coordenadas)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCoordenadas.adapter = adapter

                // Exibindo um Toast com as coordenadas
                Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
                Log.d("GeoLocalizacao", texto)
            } else {
                // Caso não consiga obter a localização
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
                            val latitude = novaLocalizacao.latitude
                            val longitude = novaLocalizacao.longitude
                            val texto = "Latitude: $latitude\nLongitude: $longitude"

                            // Preenche o Spinner com as novas coordenadas
                            val coordenadas = listOf("Latitude: $latitude\nLongitude: $longitude")
                            val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, coordenadas)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinnerCoordenadas.adapter = adapter

                            // Exibindo as coordenadas no Toast
                            Toast.makeText(applicationContext, texto, Toast.LENGTH_LONG).show()
                            Log.d("GeoLocalizacao", texto)
                        } else {
                            Toast.makeText(applicationContext, "Localização ainda indisponível", Toast.LENGTH_SHORT).show()
                        }

                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }

                // Solicitando a localização com a alta precisão
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
                try {
                    val bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    findViewById<ImageView>(R.id.icon_imagem).setImageBitmap(bmp)
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
                }
            }
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

        // Ao clicar em Registro de Riscos, abrimos a tela de registro
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
            finish()
        }

        // ABRE TELA RISCOS AO CLICAR NO MENU
        iconTipo.setOnClickListener {
            startActivity(Intent(this, DescricaoRiscosActivity::class.java))
            dialog.dismiss()
        }

        dialog.show()
    }

    // Trata o resultado da permissão
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