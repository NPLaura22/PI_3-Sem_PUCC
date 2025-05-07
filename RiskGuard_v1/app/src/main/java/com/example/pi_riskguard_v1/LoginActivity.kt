package com.example.pi_riskguard_v1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    sealed class ResultadoLogin {
        object Sucesso : ResultadoLogin()
        object SenhaIncorreta : ResultadoLogin()
        object EmailNaoEncontrado : ResultadoLogin()
        data class Erro(val mensagem: String) : ResultadoLogin()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_login)

        Log.d("login", "onCreate chamado")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Usuario")

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextSenha = findViewById<EditText>(R.id.editTextSenha)
        val btnEntrar = findViewById<Button>(R.id.btnEntrarLogin)
        val cadastro = findViewById<TextView>(R.id.textCadastro)

        cadastro.setOnClickListener {
            Log.d("login", "Botão de cadastro clicado")
            val intent = Intent(this@LoginActivity, CadastroActivity::class.java)
            startActivity(intent)
        }

        btnEntrar.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val senha = editTextSenha.text.toString().trim()

            Log.d("login", "Botão entrar clicado com email: $email")

            val mensagemValidacaoCampos = validacoes(email, senha)

            if (mensagemValidacaoCampos != null) {
                Log.d("login", "Validação falhou: $mensagemValidacaoCampos")
                Toast.makeText(this, mensagemValidacaoCampos, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("login", "Validação OK, iniciando verificação de credenciais")

            validarEmailESenha(email, senha) { resultadoLogin ->
                when (resultadoLogin) {
                    is ResultadoLogin.Sucesso -> {
                        Log.d("login", "ResultadoLogin: Sucesso")
                        Toast.makeText(this, "Login válido!", Toast.LENGTH_SHORT).show()
                        auth.signInWithEmailAndPassword(email, senha)
                            .addOnCompleteListener { login ->
                                if (login.isSuccessful) {
                                    Log.d("login", "Auth Firebase: Login bem-sucedido")
                                    val intent = Intent(this@LoginActivity, RegistroRiscoActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Log.e("login", "Auth Firebase: Falha ao logar", login.exception)
                                    Toast.makeText(this, "Erro ao fazer login", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                    is ResultadoLogin.SenhaIncorreta -> {
                        Log.d("login", "ResultadoLogin: Senha incorreta")
                        Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                    }
                    is ResultadoLogin.EmailNaoEncontrado -> {
                        Log.d("login", "ResultadoLogin: Email não encontrado")
                        Toast.makeText(this, "Email não encontrado!", Toast.LENGTH_SHORT).show()
                    }
                    is ResultadoLogin.Erro -> {
                        Log.e("login", "ResultadoLogin: Erro - ${resultadoLogin.mensagem}")
                        Toast.makeText(this, "Erro: ${resultadoLogin.mensagem}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validacoes(email: String, senha: String): String? {
        Log.d("login", "Validando email e senha")
        if (!email.contains("@")) {
            return "Insira um e-mail válido."
        }
        if (senha.isEmpty()) {
            return "Por favor, digite sua senha."
        }
        return null
    }

    fun formatarEmailParaPath(email: String): String {
        val formatado = email.replace(".", "_").replace("@", "_at_")
        Log.d("login", "Email formatado: $formatado")
        return formatado
    }

    private fun validarEmailESenha(email: String, senha: String, callback: (ResultadoLogin) -> Unit) {
        val emailFormatado = formatarEmailParaPath(email)
        Log.d("login", "Iniciando consulta no banco com email formatado: $emailFormatado")

        try {
            database.orderByChild("email").equalTo(email).get()
                .addOnSuccessListener { record ->
                    Log.d("login", "Consulta realizada. Existe: ${record.exists()}, filhos: ${record.childrenCount}")

                    if (record.exists()) {
                        for (userRecord in record.children) {
                            val userSenha = userRecord.child("senha").getValue(String::class.java)
                            Log.d("login", "Senha encontrada: $userSenha")

                            if (userSenha == senha) {
                                callback(ResultadoLogin.Sucesso)
                                return@addOnSuccessListener
                            }
                        }
                        callback(ResultadoLogin.SenhaIncorreta)
                    } else {
                        callback(ResultadoLogin.EmailNaoEncontrado)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("login", "Erro ao buscar email no Firebase", exception)
                    callback(ResultadoLogin.Erro(exception.message ?: "Erro desconhecido"))
                }
        } catch (e: Exception) {
            Log.e("login", "Erro inesperado durante a validação do email e senha", e)
            callback(ResultadoLogin.Erro("Erro inesperado: ${e.message}"))
        }
    }
}
