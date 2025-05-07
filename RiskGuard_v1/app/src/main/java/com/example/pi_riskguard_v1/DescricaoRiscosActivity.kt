package com.example.pi_riskguard_v1

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class DescricaoRiscosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_descricao_riscos)


        // Ícone de menu
        findViewById<ImageView>(R.id.feather_icon).setOnClickListener {
            showCustomMenu()
        }

        //DESCRIÇÃO RISCOS
        // FÍSICOS
        val iconFisicos = findViewById<ImageView>(R.id.icon_fisicos)
        val contentFisicos = findViewById<LinearLayout>(R.id.content_fisicos)
        iconFisicos.setOnClickListener {
            if (contentFisicos.visibility == View.GONE) {
                contentFisicos.visibility = View.VISIBLE
                iconFisicos.setImageResource(R.drawable.minus)
            } else {
                contentFisicos.visibility = View.GONE
                iconFisicos.setImageResource(R.drawable.plus)
            }
        }

        // QUÍMICOS
        val iconQuimicos = findViewById<ImageView>(R.id.icon_quimicos)
        val contentQuimicos = findViewById<LinearLayout>(R.id.content_quimicos)
        iconQuimicos.setOnClickListener {
            if (contentQuimicos.visibility == View.GONE) {
                contentQuimicos.visibility = View.VISIBLE
                iconQuimicos.setImageResource(R.drawable.minus)
            } else {
                contentQuimicos.visibility = View.GONE
                iconQuimicos.setImageResource(R.drawable.plus)
            }
        }

        // BIOLÓGICOS
        val iconBiologicos = findViewById<ImageView>(R.id.icon_biologicos)
        val contentBiologicos = findViewById<LinearLayout>(R.id.content_biologicos)
        iconBiologicos.setOnClickListener {
            if (contentBiologicos.visibility == View.GONE) {
                contentBiologicos.visibility = View.VISIBLE
                iconBiologicos.setImageResource(R.drawable.minus)
            } else {
                contentBiologicos.visibility = View.GONE
                iconBiologicos.setImageResource(R.drawable.plus)
            }
        }

        // ERGONÔMICOS
        val iconErgonomicos = findViewById<ImageView>(R.id.icon_ergonomicos)
        val contentErgonomicos = findViewById<LinearLayout>(R.id.content_ergonomicos)
        iconErgonomicos.setOnClickListener {
            if (contentErgonomicos.visibility == View.GONE) {
                contentErgonomicos.visibility = View.VISIBLE
                iconErgonomicos.setImageResource(R.drawable.minus)
            } else {
                contentErgonomicos.visibility = View.GONE
                iconErgonomicos.setImageResource(R.drawable.plus)
            }
        }

        // ACIDENTE
        val iconAcidente = findViewById<ImageView>(R.id.icon_acidente)
        val contentAcidente = findViewById<LinearLayout>(R.id.content_acidente)
        iconAcidente.setOnClickListener {
            if (contentAcidente.visibility == View.GONE) {
                contentAcidente.visibility = View.VISIBLE
                iconAcidente.setImageResource(R.drawable.minus)
            } else {
                contentAcidente.visibility = View.GONE
                iconAcidente.setImageResource(R.drawable.plus)
            }
        }

        val iconVoltar = findViewById<ImageView>(R.id.icon_voltar)

        iconVoltar.setOnClickListener {
            // Volta para a activity anterior
            //finish()
            startActivity(Intent(this, RegistroRiscoActivity::class.java))
        }

    }

    private fun showCustomMenu() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.menu)

        val registro = dialog.findViewById<LinearLayout>(R.id.layout_registro)
        val tipo     = dialog.findViewById<LinearLayout>(R.id.layout_tipo)
        val sair     = dialog.findViewById<LinearLayout>(R.id.layout_sair)

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

        dialog.show()
    }
}