package com.example.pi_riskguard_v1

import android.content.ComponentCallbacks
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pi_riskguard_v1.databinding.TelaCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.snackbar.Snackbar

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: TelaCadastroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_cadastro)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Usuario")

        val editEmail = findViewById<EditText>(R.id.inputEmail)
        val editNome = findViewById<EditText>(R.id.inputNome)
        val editCpf = findViewById<EditText>(R.id.inputCPF)
        val editSenha = findViewById<EditText>(R.id.inputSenha)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val nome = editNome.text.toString().trim()
            val cpf = editCpf.text.toString().trim()
            val senha = editSenha.text.toString()

            val mensagemValidacaoCampos = validacoes(nome, email, senha, cpf)

            if(mensagemValidacaoCampos != null)
            {
                Log.e("Cadastro", "Validação falhou: $mensagemValidacaoCampos")
                Toast.makeText(this, mensagemValidacaoCampos, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

               cpfExiste(cpf) { erroCpfExistente ->
                   if (erroCpfExistente != null) {
                       Log.e("Cadastro", "CPF já cadastrado: $erroCpfExistente")
                       Toast.makeText(this, erroCpfExistente, Toast.LENGTH_SHORT).show()
                   } else {
                       Log.d("Cadastro", "CPF não cadastrado. Prosseguindo com cadastro.")
                       cadastrarUsuario(email, senha, nome, cpf)
                   }
               }
        }
        voltarParaLogin()
    }

    private fun cadastrarUsuario(email: String, senha: String, nome: String, cpf: String)
    {
        Log.d("Cadastro", "Iniciando cadastro no Firebase Auth...")
        auth.createUserWithEmailAndPassword(email,senha ).addOnCompleteListener {cadastro ->
            if(cadastro.isSuccessful){
                val userId = auth.currentUser?.uid
                val usuario = Usuario(nome, cpf, email, senha)

                userId?.let{
                    Log.d("Cadastro", "Usuário autenticado. Salvando no Realtime Database...")
                    database.child(it).setValue(usuario)
                        .addOnCompleteListener { dbCadastro ->
                            if(dbCadastro.isSuccessful){
                                Log.d("Cadastro", "Usuário salvo no Firebase")
                                Toast.makeText(this, "Sucesso ao cadastrar usuario.", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            }else{
                                Log.e("Cadastro", "Erro ao salvar usuário: ${dbCadastro.exception?.message}")
                                Toast.makeText(this, "Erro ao cadastrar usuário.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            else{
                val e = cadastro.exception
                if(e is FirebaseAuthUserCollisionException)
                {
                    Toast.makeText(this@CadastroActivity, "Erro: Este e-mail já existe!", Toast.LENGTH_LONG).show()
                    Log.e("Cadastro", "E-mail já cadastrado: ${e.message}")
                }else{
                    Log.e("Cadastro", "Erro ao cadastrar: ${cadastro.exception?.message}")
                    Toast.makeText(this, "Erro ao cadastrar: \${exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun voltarParaLogin(){
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)

        btnVoltar.setOnClickListener {
            Log.d("Cadastro", "Voltando para tela de login.")
            val intent2 = Intent(this@CadastroActivity, LoginActivity::class.java)
            startActivity(intent2)
        }
    }

    private fun validacoes(nome: String, email: String, senha: String, cpf: String): String?{
        // Validação do email
        if (!email.contains("@"))
            return "Email inválido. Insira um email com @"

        // Validação do nome (apenas letras e espaços)
        if (!nome.matches(Regex("^[A-Za-zÀ-ÿ\\s]+\$")))
            return "Nome inválido. Use apenas letras e espaços."

        // Validação de tamanho de senha, mínimo do firebase
        if(senha.length < 6)
            return "Senha muito curta. Use no mínimo 6 caracteres."

        if(cpf.length != 11)
            return "CPF inválido! Deve ter 11 dígitos."

        return null
    }

    private fun cpfExiste(cpf: String, callback: (String?) -> Unit){
        Log.d("Cadastro", "Consultando CPF no Firebase...")
        database.orderByChild("cpf").equalTo(cpf)
            .get()
            .addOnSuccessListener { registro ->
                if(registro.exists())
                {
                    Log.e("Cadastro", "CPF já existe no banco.")
                    callback("Email já cadastrado para esse CPF.")
                }
                else
                {
                    Log.d("Cadastro", "CPF ainda não registrado.")
                    callback(null)
                }
            }
            .addOnFailureListener{ e->
                Log.e("Cadastro", "Erro ao verificar CPF: ${e.message}")
                callback("Erro ao verificar CPF.")
            }
    }
}
