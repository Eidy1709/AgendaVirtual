package com.example.agendavirtual

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agendavirtual.data.AgendaEntry
import com.example.agendavirtual.data.AgendaRepository
import com.example.agendavirtual.data.AppDatabase
import com.example.agendavirtual.data.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarioActivity : AppCompatActivity() {

    private lateinit var repository: AgendaRepository
    private lateinit var adapter: AgendaAdapter
    private lateinit var tvDiaSeleccionado: TextView
    private lateinit var tvSinAnotaciones: TextView

    private var currentUserId: Long = 1L
    private var fechaSeleccionada: String = ""
    private var cargaJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        val database = AppDatabase.getDatabase(this)
        repository = AgendaRepository(database.agendaDao())

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        tvDiaSeleccionado = findViewById(R.id.tvDiaSeleccionado)
        tvSinAnotaciones = findViewById(R.id.tvSinAnotaciones)
        val rvAnotaciones = findViewById<RecyclerView>(R.id.rvAnotaciones)
        val fabAgregar = findViewById<FloatingActionButton>(R.id.fabAgregar)

        // Al abrir la app, el día seleccionado es hoy (formato AAAA-MM-DD)
        fechaSeleccionada = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        rvAnotaciones.layoutManager = LinearLayoutManager(this)
        adapter = AgendaAdapter(
            emptyList(),
            onCheckedChange = { entry, isChecked ->
                lifecycleScope.launch {
                    repository.markAsCompleted(entry.id, isChecked)
                }
            },
            onEditClick = { entry -> abrirFormulario(entry) },
            onDeleteClick = { entry -> confirmarBorrado(entry) }
        )
        rvAnotaciones.adapter = adapter

        // Cuando el usuario toca un día del calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // month empieza en 0 (enero = 0), por eso se suma 1
            fechaSeleccionada = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            cargarAnotaciones()
        }

        fabAgregar.setOnClickListener { abrirFormulario(null) }

        lifecycleScope.launch {
            currentUserId = obtenerUsuario(database)
            cargarAnotaciones()
        }
    }

    // Busca el usuario de prueba y, si no existe, lo crea
    private suspend fun obtenerUsuario(database: AppDatabase): Long {
        val userDao = database.userDao()
        val existente = userDao.getUserByUsername("testuser")
        return existente?.id ?: userDao.insertUser(
            User(username = "testuser", email = "test@agenda.com", password = "123")
        )
    }

    private fun cargarAnotaciones() {
        tvDiaSeleccionado.text = "Anotaciones del ${formatearFecha(fechaSeleccionada)}"

        // Se cancela la búsqueda anterior para que no se mezclen los días
        cargaJob?.cancel()
        cargaJob = lifecycleScope.launch {
            repository.getEntriesForDay(currentUserId, fechaSeleccionada).collectLatest { lista ->
                adapter.updateData(lista)
                tvSinAnotaciones.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    // Convierte "2026-11-08" en "08/11/2026"
    private fun formatearFecha(fecha: String): String {
        val partes = fecha.split("-")
        return "${partes[2]}/${partes[1]}/${partes[0]}"
    }

    private fun abrirFormulario(entry: AgendaEntry?) {
        val intent = Intent(this, AgendaActivity::class.java)
        intent.putExtra("fecha", entry?.date ?: fechaSeleccionada)
        if (entry != null) {
            intent.putExtra("entryId", entry.id)
            intent.putExtra("titulo", entry.title)
            intent.putExtra("descripcion", entry.description)
            intent.putExtra("hora", entry.time)
        }
        startActivity(intent)
    }

    private fun confirmarBorrado(entry: AgendaEntry) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Borrar anotación")
            .setMessage("¿Quieres borrar \"${entry.title}\"? Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Borrar") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteEntry(entry)
                    Toast.makeText(this@CalendarioActivity, "Anotación borrada", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}