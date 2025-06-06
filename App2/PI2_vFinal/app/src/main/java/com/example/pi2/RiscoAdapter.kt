package com.example.pi2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RiscoAdapter(private val listaRiscos: MutableList<Risco>) :
    RecyclerView.Adapter<RiscoAdapter.RiscoViewHolder>() {

    class RiscoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dataTextView: TextView = itemView.findViewById(R.id.data_relatorio)
        val tipoRiscoTextView: TextView = itemView.findViewById(R.id.tipo_risco)
        val descricaoTextView: TextView = itemView.findViewById(R.id.descricao_risco)
        val latitudeTextView: TextView = itemView.findViewById(R.id.latitude_risco)
        val longitudeTextView: TextView = itemView.findViewById(R.id.longitude_risco)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiscoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_card_relatorio, parent, false)
        return RiscoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiscoViewHolder, position: Int) {
        val risco = listaRiscos[position]
        holder.tipoRiscoTextView.text = risco.tipoRisco
        holder.descricaoTextView.text = risco.descricao
        holder.latitudeTextView.text = risco.latitude.toString()
        holder.longitudeTextView.text = risco.longitude.toString()
        holder.dataTextView.text = risco.data
    }

    override fun getItemCount(): Int {
        return listaRiscos.size
    }

    fun updateData(novaLista: List<Risco>) {
        listaRiscos.clear()
        listaRiscos.addAll(novaLista)
        notifyDataSetChanged()
    }
}
