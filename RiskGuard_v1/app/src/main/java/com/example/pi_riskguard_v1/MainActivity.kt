package com.example.pi_riskguard_v1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CadastroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_cadastro)

        val editEmail = findViewById<EditText>(R.id.inputEmail)
        val editNome = findViewById<EditText>(R.id.inputNome)
        val editCpf = findViewById<EditText>(R.id.inputCPF)
        val editSenha = findViewById<EditText>(R.id.inputSenha)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val nome = editNome.text.toString().trim()
            val cpf = editCpf.text.toString().trim()
            val senha = editSenha.text.toString() // aqui pode adicionar regra depois

            // Validação do email
            if (!email.contains("@")) {
                Toast.makeText(this, "Email inválido. Insira um email com @", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.length > 50) {
                Toast.makeText(this, "Email muito longo. Máximo de 50 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação do nome (apenas letras e espaços)
            if (!nome.matches(Regex("^[A-Za-zÀ-ÿ\\s]+\$"))) {
                Toast.makeText(this, "Nome inválido. Use apenas letras e espaços.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nome.length > 100) {
                Toast.makeText(this, "Nome muito longo. Máximo de 100 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação do CPF (11 dígitos numéricos)
            if (!cpf.matches(Regex("^\\d{11}\$"))) {
                Toast.makeText(this, "CPF inválido. Digite exatamente 11 números.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação da senha (máximo 8 caracteres)
            if (senha.length > 8) {
                Toast.makeText(this, "Senha muito longa. Use no máximo 8 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Se tudo estiver certo
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)

        btnVoltar.setOnClickListener {
            val intent2 = Intent(this@CadastroActivity, LoginActivity::class.java)
            startActivity(intent2)
        }
    }
    
}
