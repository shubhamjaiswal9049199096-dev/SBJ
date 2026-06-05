package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ElevatorDao {

    // --- Customer Queries ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Int)

    // --- Appointment Queries ---
    @Query("SELECT * FROM appointments ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Int)

    // --- Ticket Queries ---
    @Query("SELECT * FROM tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: Int): Ticket?

    @Query("SELECT * FROM tickets WHERE id = :id")
    fun getTicketByIdFlow(id: Int): Flow<Ticket?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket): Long

    @Update
    suspend fun updateTicket(ticket: Ticket)

    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun deleteTicketById(id: Int)

    // --- Technician Queries ---
    @Query("SELECT * FROM technicians ORDER BY name ASC")
    fun getAllTechnicians(): Flow<List<Technician>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnician(technician: Technician): Long

    @Update
    suspend fun updateTechnician(technician: Technician)

    @Query("DELETE FROM technicians WHERE id = :id")
    suspend fun deleteTechnicianById(id: Int)
}
