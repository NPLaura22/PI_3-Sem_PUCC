package com.example.pi_riskguard_v1
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_login)

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextSenha = findViewById<EditText>(R.id.editTextSenha)
        val btnEntrar = findViewById<Button>(R.id.btnEntrarLogin)

        val cadastro = findViewById<TextView>(R.id.textCadastro)
        cadastro.setOnClickListener {
            val intent = Intent(this@LoginActivity, CadastroActivity::class.java)
            startActivity(intent)
        }

        btnEntrar.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val senha = editTextSenha.text.toString().trim()

            val emailCorreto = "grupo11@teste.com"
            val senhaCorreta = "123456"
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha.isEmpty()) {
                Toast.makeText(this, "Por favor, digite sua senha.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //fazer a comparação com o banco de dados - pendente de criação
            if (email == emailCorreto && senha == senhaCorreta) {
                val intent = Intent(this, RegistroRiscoActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                if(email != emailCorreto)
                {
                    Toast.makeText(this, "E-mail incorreto.", Toast.LENGTH_SHORT).show()
                }
                if(senha != senhaCorreta)
                {
                    Toast.makeText(this, "Senha incorreta.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

