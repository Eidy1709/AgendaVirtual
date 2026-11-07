package com.example.agendavirtual.data

import kotlinx.coroutines.flow.Flow

class AgendaRepository(private val agendaDao: AgendaDao) {

    // Guardar anotación
    suspend fun saveEntry(entry: AgendaEntry): Long {
        return agendaDao.insertEntry(entry)
    }

    // Leer anotaciones de un día específico
    fun getEntriesForDay(userId: Long, date: String): Flow<List<AgendaEntry>> {
        return agendaDao.getEntriesForDay(userId, date)
    }

    // Editar anotación
    suspend fun updateEntry(entry: AgendaEntry) {
        agendaDao.updateEntry(entry)
    }

    // Borrar anotación
    suspend fun deleteEntry(entry: AgendaEntry) {
        agendaDao.deleteEntry(entry)
    }

    // Marcar como hecho
    suspend fun markAsCompleted(entryId: Long, isCompleted: Boolean) {
        agendaDao.markAsCompleted(entryId, isCompleted)
    }
}
