package com.example.pi2

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MapaDeRiscosActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapa_de_riscos)

        // Menu lateral
        findViewById<ImageView>(R.id.menu).setOnClickListener {
            showCustomMenu()
        }

        // WebView
        webView = findViewById(R.id.webview_mapa)
        webView.settings.javaScriptEnabled = true

// Firebase
        database = FirebaseDatabase.getInstance().getReference("Riscos")

// Usa o onPageFinished para garantir que o HTML está carregado
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                carregarDadosDoFirebase()
            }
        }

// Carrega o HTML
        webView.loadUrl("file:///android_asset/mapa_calor.html")
    }

    private fun carregarDadosDoFirebase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaRiscos = mutableListOf<String>()

                for (riscoSnapshot in snapshot.children) {
                    val lat = riscoSnapshot.child("latitude").getValue(Double::class.java)
                    val lng = riscoSnapshot.child("longitude").getValue(Double::class.java)
                    val intensidade = riscoSnapshot.child("intensidade").getValue(Double::class.java) ?: 0.5

                    if (lat != null && lng != null) {
                        listaRiscos.add("[$lat, $lng, $intensidade]")
                    }
                }

                val jsonArray = listaRiscos.joinToString(separator = ",", prefix = "[", postfix = "]")

                // Envia para o HTML
                val script = "javascript:carregarHeatmap($jsonArray)"
                webView.evaluateJavascript(script, null)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showCustomMenu() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.menu)

        val layoutMapa = dialog.findViewById<LinearLayout>(R.id.layout_mapa)
        val layoutRelatorio = dialog.findViewById<LinearLayout>(R.id.layout_relatorio)

        layoutMapa?.setOnClickListener {
            startActivity(Intent(this, MapaDeRiscosActivity::class.java))
            dialog.dismiss()
        }

        layoutRelatorio?.setOnClickListener {
            startActivity(Intent(this, RelatorioActivity::class.java))
            dialog.dismiss()
        }

        dialog.show()
    }
}