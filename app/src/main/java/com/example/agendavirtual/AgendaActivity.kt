package com.example.agendavirtual

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agendavirtual.data.AppDatabase
import com.example.agendavirtual.data.AgendaEntry
import com.example.agendavirtual.data.AgendaRepository
import com.example.agendavirtual.data.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgendaActivity : AppCompatActivity() {

    private lateinit var repository: AgendaRepository
    private lateinit var adapter: AgendaAdapter
    private var currentUserId: Long = 1L
    private var editingEntryId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        val database = AppDatabase.getDatabase(this)
        repository = AgendaRepository(database.agendaDao())

        val etAgendaTitle = findViewById<EditText>(R.id.etAgendaTitle)
        val etAgendaDescription = findViewById<EditText>(R.id.etAgendaDescription)
        val etAgendaDate = findViewById<EditText>(R.id.etAgendaDate)
        val etAgendaTime = findViewById<EditText>(R.id.etAgendaTime)
        val btnSaveEntry = findViewById<Button>(R.id.btnSaveEntry)
        val tvFormTitle = findViewById<TextView>(R.id.tvFormTitle)

        val etFilterDate = findViewById<EditText>(R.id.etFilterDate)
        val btnFilterDay = findViewById<Button>(R.id.btnFilterDay)
        val rvAgendaEntries = findViewById<RecyclerView>(R.id.rvAgendaEntries)

        // Fecha actual por defecto en formato YYYY-MM-DD
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        etAgendaDate.setText(today)
        etFilterDate.setText(today)

        rvAgendaEntries.layoutManager = LinearLayoutManager(this)
        adapter = AgendaAdapter(
            emptyList(),
            onCheckedChange = { entry, isChecked ->
                lifecycleScope.launch {
                    repository.markAsCompleted(entry.id, isChecked)
                }
            },
            onEditClick = { entry ->
                if (entry.isCompleted) {
                    Toast.makeText(this@AgendaActivity, "No se puede editar una tarea completada", Toast.LENGTH_SHORT).show()
                } else {
                    editingEntryId = entry.id
                    tvFormTitle.text = "Editar Anotación"
                    etAgendaTitle.setText(entry.title)
                    etAgendaDescription.setText(entry.description)
                    etAgendaDate.setText(entry.date)
                    etAgendaTime.setText(entry.time)
                    btnSaveEntry.text = "Actualizar Anotación"
                }
            },
            onDeleteClick = { entry ->
                lifecycleScope.launch {
                    repository.deleteEntry(entry)
                    Toast.makeText(this@AgendaActivity, "Anotación borrada", Toast.LENGTH_SHORT).show()
                }
            }
        )
        rvAgendaEntries.adapter = adapter

        // Asegurar un usuario por defecto en la BD para cumplir con la Foreign Key
        lifecycleScope.launch {
            val userDao = database.userDao()
            val existingUser = userDao.getUserByUsername("testuser")
            if (existingUser == null) {
                currentUserId = userDao.insertUser(User(username = "testuser", email = "test@agenda.com", password = "123"))
            } else {
                currentUserId = existingUser.id
            }
            loadEntriesForDate(today)
        }

        btnSaveEntry.setOnClickListener {
            val title = etAgendaTitle.text.toString().trim()
            val description = etAgendaDescription.text.toString().trim()
            val date = etAgendaDate.text.toString().trim()
            val time = etAgendaTime.text.toString().trim()

            if (title.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "El título y la fecha son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (editingEntryId == null) {
                    val newEntry = AgendaEntry(
                        userId = currentUserId,
                        title = title,
                        description = description,
                        date = date,
                        time = time
                    )
                    repository.saveEntry(newEntry)
                    Toast.makeText(this@AgendaActivity, "Anotación guardada", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedEntry = AgendaEntry(
                        id = editingEntryId!!,
                        userId = currentUserId,
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        isCompleted = false
                    )
                    repository.updateEntry(updatedEntry)
                    Toast.makeText(this@AgendaActivity, "Anotación actualizada", Toast.LENGTH_SHORT).show()
                    editingEntryId = null
                    tvFormTitle.text = "Nueva Anotación"
                    btnSaveEntry.text = "Guardar Anotación"
                }

                etAgendaTitle.setText("")
                etAgendaDescription.setText("")
                etAgendaTime.setText("")
                loadEntriesForDate(date)
                etFilterDate.setText(date)
            }
        }

        btnFilterDay.setOnClickListener {
            val filterDate = etFilterDate.text.toString().trim()
            if (filterDate.isNotEmpty()) {
                loadEntriesForDate(filterDate)
            } else {
                Toast.makeText(this, "Ingresa una fecha válida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadEntriesForDate(date: String) {
        lifecycleScope.launch {
            repository.getEntriesForDay(currentUserId, date).collectLatest { entries ->
                adapter.updateData(entries)
            }
        }
    }
}
