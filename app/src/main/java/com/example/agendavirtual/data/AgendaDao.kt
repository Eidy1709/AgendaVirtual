package com.example.agendavirtual.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {

    // Guardar (Insertar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AgendaEntry): Long

    // Leer los anotados de un día específico para un usuario dado
    @Query("SELECT * FROM agenda_entries WHERE userId = :userId AND date = :date ORDER BY time ASC")
    fun getEntriesForDay(userId: Long, date: String): Flow<List<AgendaEntry>>

    // Editar (Actualizar)
    @Update
    suspend fun updateEntry(entry: AgendaEntry)

    // Borrar
    @Delete
    suspend fun deleteEntry(entry: AgendaEntry)

    // Marcar como hecho (actualizar estado isCompleted)
    @Query("UPDATE agenda_entries SET isCompleted = :isCompleted WHERE id = :entryId")
    suspend fun markAsCompleted(entryId: Long, isCompleted: Boolean)

    // Obtener todas las anotaciones de un usuario (para pruebas o listados generales)
    @Query("SELECT * FROM agenda_entries WHERE userId = :userId ORDER BY date DESC, time ASC")
    suspend fun getAllEntriesForUser(userId: Long): List<AgendaEntry>
}
