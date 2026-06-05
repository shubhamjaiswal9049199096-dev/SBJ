package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val buildingName: String,
    val address: String,
    val elevatorModel: String,
    val installDate: String,
    val lastServiceDate: String,
    val nextDueDate: String,
    val amcStatus: String, // "Active", "Expired", "Renewal Due"
    val amcType: String, // "Basic", "Comprehensive"
    val amcExpiryDate: String,
    val amcPaymentStatus: String // "Paid", "Pending"
) : Serializable

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val elevatorType: String,
    val serviceType: String, // "Installation", "AMC Service", "Repair", "Inspection"
    val appointmentDate: String, // YYYY-MM-DD
    val appointmentTime: String, // HH:MM AM/PM
    val technicianId: Int?,
    val technicianName: String?,
    val status: String, // "Scheduled", "In Progress", "Completed"
    val notes: String
) : Serializable

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String,
    val customerPhone: String,
    val title: String, // "Elevator not working", "Noise issue", etc.
    val priority: String, // "Low", "Medium", "High", "Emergency"
    val status: String, // "Open", "Assigned", "In Progress", "Resolved", "Closed"
    val technicianId: Int?,
    val technicianName: String?,
    val description: String,
    val internalNotes: String,
    val createdAt: String // YYYY-MM-DD
) : Serializable

@Entity(tableName = "technicians")
data class Technician(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val skillSet: String, // "Installation Expert", "Maintenance Spl.", "Electrical Faults", etc.
    val availability: String // "Available", "On Job", "Off Duty"
) : Serializable
