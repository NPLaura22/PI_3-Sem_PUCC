package com.example.pi2

import android.app.Dialog
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import java.util.Calendar
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import java.util.Date
import java.util.Locale

class RelatorioActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RiscoAdapter
    private val listaRiscos = mutableListOf<Risco>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_relatorio)
        filtrarData()
        filtrarDataFim()

        findViewById<ImageView>(R.id.menu).setOnClickListener { showCustomMenu() }

        recyclerView = findViewById(R.id.recyclerRiscos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RiscoAdapter(listaRiscos)
        recyclerView.adapter = adapter

        buscarTodosOsItens()
        exibirTipos()
        val btnFiltrar: Button = findViewById(R.id.btn_filtrar)
        val btnLimpar: Button = findViewById(R.id.btn_limpar_filtro)
        val spinnerTipo: Spinner = findViewById(R.id.spinner_tipo)
        val btnFiltrarData: Button = findViewById(R.id.button_filtrar_data)
        btnFiltrarData.setOnClickListener {
            filtrarPeriodo()
        }


        btnFiltrar.setOnClickListener {
            val tipoSelecionado = spinnerTipo.selectedItem.toString()

            if (tipoSelecionado == "Tipos de risco") {
                buscarTodosOsItens()
                Toast.makeText(this, "Selecione um tipo de risco para aplicar o filtro.", Toast.LENGTH_SHORT).show()
            } else {
                filtrarPorTipo(tipoSelecionado)
            }

        }

        btnLimpar.setOnClickListener{
            buscarTodosOsItens()
            spinnerTipo.setSelection(0)

            // Limpar campos de data
            val txtDataInicio: TextView = findViewById(R.id.filtro_data_inicio_text)
            val txtDataFim: TextView = findViewById(R.id.filtro_data_fim_text)
            txtDataInicio.text = "Início"
            txtDataFim.text = "Fim"
        }


    }

    private fun exibirTipos() {
        val spinnerTipo: Spinner = findViewById(R.id.spinner_tipo)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipos_de_risco,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerTipo.adapter = adapter
    }


    private fun filtrarData(){
        //filtro data inicial
        val txtViewInicio: TextView = findViewById(R.id.filtro_data_inicio_text)
        val imgCalendarInicio: ImageView = findViewById(R.id.icon_calendar_inicio)
        imgCalendarInicio.setOnClickListener {
            val c = Calendar.getInstance()
            val ano = c.get(Calendar.YEAR)
            val mes = c.get(Calendar.MONTH)
            val dia = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val dataSelecionada = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                txtViewInicio.setText(dataSelecionada)
            }, ano, mes, dia)

            datePickerDialog.show()
        }
    }

    private fun preencherDataFinal(){
        // filtro data fim
        val txtViewFim: TextView = findViewById(R.id.filtro_data_fim_text)
        val imgCalendarFim: ImageView = findViewById(R.id.icon_calendar_fim)
        imgCalendarFim.setOnClickListener {
            val c = Calendar.getInstance()
            val ano = c.get(Calendar.YEAR)
            val mes = c.get(Calendar.MONTH)
            val dia = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val dataSelecionada = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                txtViewFim.setText(dataSelecionada)
            }, ano, mes, dia)

            datePickerDialog.show()
        }
    }

    private fun filtrarPeriodo() {
        val txtDataInicio: TextView = findViewById(R.id.filtro_data_inicio_text)
        val txtDataFim: TextView = findViewById(R.id.filtro_data_fim_text)

        val dataInicioStr = txtDataInicio.text.toString()
        val dataFimStr = txtDataFim.text.toString()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Verifica se pelo menos a data de início foi preenchida
        if (dataInicioStr == "Início") {
            Toast.makeText(this, "Por favor, selecione pelo menos a data de início", Toast.LENGTH_SHORT).show()
            return
        }

        val dataInicio: Date = dateFormat.parse(dataInicioStr) ?: return
        val dataFim: Date? = if (dataFimStr != "Fim") dateFormat.parse(dataFimStr) else null

        val databaseRef = FirebaseDatabase.getInstance().getReference("Riscos")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaRiscos.clear()

                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Risco::class.java)

                    if (item != null && item.data != null) {
                        try {
                            val dataItem = dateFormat.parse(item.data)

                            if (dataItem != null) {
                                val dentroDoPeriodo = if (dataFim != null) {
                                    !dataItem.before(dataInicio) && !dataItem.after(dataFim)
                                } else {
                                    dateFormat.format(dataItem) == dateFormat.format(dataInicio)
                                }

                                if (dentroDoPeriodo) {
                                    listaRiscos.add(item)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("FiltroData", "Erro ao converter data: ${item.data}")
                        }
                    }
                }

                adapter.notifyDataSetChanged()

                val tvNenhumRegistro: TextView = findViewById(R.id.tv_nenhum_registro)
                tvNenhumRegistro.visibility = if (listaRiscos.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Erro ao buscar dados: ${error.message}")
            }
        })
    }
    private fun filtrarDataFim() {
        val txtViewFim: TextView = findViewById(R.id.filtro_data_fim_text)
        val imgCalendarFim: ImageView = findViewById(R.id.icon_calendar_fim)

        imgCalendarFim.setOnClickListener {
            val c = Calendar.getInstance()
            val ano = c.get(Calendar.YEAR)
            val mes = c.get(Calendar.MONTH)
            val dia = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val dataSelecionada = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                txtViewFim.text = dataSelecionada
            }, ano, mes, dia)

            datePickerDialog.show()
        }
    }



    private fun filtrarPorTipo(tipoSelecionado: String){
        val databaseRef = FirebaseDatabase.getInstance().getReference("Riscos")
        val query = if (tipoSelecionado.isEmpty()) {
            databaseRef
        } else {
            databaseRef.orderByChild("tipoRisco").equalTo(tipoSelecionado)
        }
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaRiscos.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Risco::class.java)
                    if (item != null) {
                        listaRiscos.add(item)
                    } else {
                        Log.w("MapaDeRiscos", "Item nulo ou mal formatado")
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Erro ao buscar dados: ${error.message}")
            }
        })
    }
    private fun buscarTodosOsItens() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Riscos")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaRiscos.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Risco::class.java)
                    if (item != null) {
                        listaRiscos.add(item)
                    } else {
                        Log.w("MapaDeRiscos", "Item nulo ou mal formatado")
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Erro ao buscar dados: ${error.message}")
            }
        })
    }

    private fun showCustomMenu() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.menu) // Certifique-se de que esse XML existe

        val layoutMapa = dialog.findViewById<LinearLayout>(R.id.layout_mapa)
        val layoutRelatorio = dialog.findViewById<LinearLayout>(R.id.layout_relatorio)

        layoutMapa?.setOnClickListener {
            val intent = Intent(this, MapaDeRiscosActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

          layoutRelatorio?.setOnClickListener {
              val intent = Intent(this, RelatorioActivity::class.java)
              startActivity(intent)
              dialog.dismiss()
          }

        dialog.show()
    }
}