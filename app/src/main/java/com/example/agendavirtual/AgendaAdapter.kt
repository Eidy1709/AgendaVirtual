package com.example.agendavirtual

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agendavirtual.data.AgendaEntry

class AgendaAdapter(
    private var entries: List<AgendaEntry>,
    private val onCheckedChange: (AgendaEntry, Boolean) -> Unit,
    private val onEditClick: (AgendaEntry) -> Unit,
    private val onDeleteClick: (AgendaEntry) -> Unit
) : RecyclerView.Adapter<AgendaAdapter.AgendaViewHolder>() {

    class AgendaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbCompleted: CheckBox = view.findViewById(R.id.cbCompleted)
        val tvItemTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvItemDescription: TextView = view.findViewById(R.id.tvItemDescription)
        val tvItemTime: TextView = view.findViewById(R.id.tvItemTime)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgendaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda_entry, parent, false)
        return AgendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgendaViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvItemTitle.text = entry.title
        holder.tvItemDescription.text = entry.description
        holder.tvItemTime.text = if (entry.time.isNotEmpty()) entry.time else "Todo el día"

        holder.cbCompleted.setOnCheckedChangeListener(null)
        holder.cbCompleted.isChecked = entry.isCompleted

        // Feedback visual y bloqueo del botón de edición si está completado
        if (entry.isCompleted) {
            holder.tvItemTitle.paintFlags = holder.tvItemTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvItemDescription.paintFlags = holder.tvItemDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.6f
            holder.btnEdit.isEnabled = false
            holder.btnEdit.alpha = 0.3f
        } else {
            holder.tvItemTitle.paintFlags = holder.tvItemTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvItemDescription.paintFlags = holder.tvItemDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.alpha = 1.0f
            holder.btnEdit.isEnabled = true
            holder.btnEdit.alpha = 1.0f
        }

        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(entry, isChecked)
        }

        holder.btnEdit.setOnClickListener { onEditClick(entry) }
        holder.btnDelete.setOnClickListener { onDeleteClick(entry) }
    }

    override fun getItemCount(): Int = entries.size

    fun updateData(newEntries: List<AgendaEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
