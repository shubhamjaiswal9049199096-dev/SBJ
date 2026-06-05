package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ElevatorViewModel(private val repository: ElevatorRepository) : ViewModel() {

    // --- Data Streams ---
    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tickets: StateFlow<List<Ticket>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val technicians: StateFlow<List<Technician>> = repository.allTechnicians
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search & Details State ---
    private val _ticketSearchId = MutableStateFlow<String>("")
    val ticketSearchId = _ticketSearchId.asStateFlow()

    val searchedTicket: StateFlow<Ticket?> = _ticketSearchId
        .debounce(300)
        .flatMapLatest { idStr ->
            val id = idStr.toIntOrNull()
            if (id != null) {
                repository.getTicketById(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateTicketSearchId(idStr: String) {
        _ticketSearchId.value = idStr
    }

    // --- Calculated Dashboard Metrics ---
    // Let's assume today's date is "2026-06-05" for mock-data consistency, or use calendar date
    private val todayString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val totalAppointmentsToday: StateFlow<Int> = appointments.map { list ->
        list.count { it.appointmentDate == "2026-06-05" || it.appointmentDate == todayString }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val openTicketsCount: StateFlow<Int> = tickets.map { list ->
        list.count { it.status != "Resolved" && it.status != "Closed" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingAMCRenewalsCount: StateFlow<Int> = customers.map { list ->
        list.count { it.amcStatus == "Renewal Due" || it.amcStatus == "Expired" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val revenueThisMonth: StateFlow<Double> = customers.map { list ->
        // Comprehensive is ₹25,000, Basic is ₹12,000. Let's calculate based on paid customers
        list.filter { it.amcPaymentStatus == "Paid" }.sumOf {
            if (it.amcType == "Comprehensive") 25000.0 else 12500.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Operations (Inserts/Updates) ---

    fun bookAppointment(
        customerId: Int,
        customerName: String,
        customerPhone: String,
        address: String,
        elevatorType: String,
        serviceType: String,
        date: String,
        time: String,
        notes: String,
        technicianId: Int?,
        technicianName: String?
    ) {
        viewModelScope.launch {
            val appointment = Appointment(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                address = address,
                elevatorType = elevatorType,
                serviceType = serviceType,
                appointmentDate = date,
                appointmentTime = time,
                technicianId = technicianId,
                technicianName = technicianName,
                status = "Scheduled",
                notes = notes
            )
            repository.insertAppointment(appointment)
        }
    }

    fun updateAppointmentStatus(appointmentId: Int, status: String) {
        viewModelScope.launch {
            // Fetch appointment first or update direct in db
            val appt = appointments.value.find { it.id == appointmentId } ?: return@launch
            val updated = appt.copy(status = status)
            repository.updateAppointment(updated)
            
            // If completed, update customer "lastServiceDate"
            if (status == "Completed") {
                val customer = customers.value.find { it.id == appt.customerId }
                if (customer != null) {
                    val nextDate = calculateNextDueDate(appt.appointmentDate)
                    val updatedCust = customer.copy(
                        lastServiceDate = appt.appointmentDate,
                        nextDueDate = nextDate
                    )
                    repository.updateCustomer(updatedCust)
                }
            }
        }
    }

    private fun calculateNextDueDate(currentDateStr: String): String {
        // Service due in 30 days
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(currentDateStr) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, 30)
            sdf.format(cal.time)
        } catch (e: Exception) {
            "2026-07-05" // fallback
        }
    }

    fun raiseTicket(
        customerId: Int,
        customerName: String,
        customerPhone: String,
        title: String,
        priority: String,
        description: String
    ) {
        viewModelScope.launch {
            val ticket = Ticket(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                title = title,
                priority = priority,
                status = "Open",
                technicianId = null,
                technicianName = null,
                description = description,
                internalNotes = "",
                createdAt = "2026-06-05" // today’s date string
            )
            repository.insertTicket(ticket)
        }
    }

    fun assignAndUpdateTicket(
        ticketId: Int,
        status: String,
        technicianId: Int?,
        technicianName: String?,
        internalNotes: String
    ) {
        viewModelScope.launch {
            val tkt = tickets.value.find { it.id == ticketId } ?: return@launch
            val updated = tkt.copy(
                status = status,
                technicianId = technicianId,
                technicianName = technicianName,
                internalNotes = internalNotes
            )
            repository.updateTicket(updated)

            // If a technician is assigned, update their status to "On Job"
            if (technicianId != null && status == "Assigned") {
                updateTechnicianAvailability(technicianId, "On Job")
            }
        }
    }

    fun addCustomer(
        name: String,
        phone: String,
        email: String,
        buildingName: String,
        address: String,
        elevatorModel: String,
        installDate: String,
        amcType: String,
        amcStatus: String = "Active",
        amcExpiryDate: String,
        amcPaymentStatus: String = "Paid"
    ) {
        viewModelScope.launch {
            val customer = Customer(
                name = name,
                phone = phone,
                email = email,
                buildingName = buildingName,
                address = address,
                elevatorModel = elevatorModel,
                installDate = installDate,
                lastServiceDate = "Never",
                nextDueDate = installDate,
                amcStatus = amcStatus,
                amcType = amcType,
                amcExpiryDate = amcExpiryDate,
                amcPaymentStatus = amcPaymentStatus
            )
            repository.insertCustomer(customer)
        }
    }

    fun renewAMC(customerId: Int, amcType: String, newExpiryDate: String, paymentStatus: String) {
        viewModelScope.launch {
            val customer = customers.value.find { it.id == customerId } ?: return@launch
            val updated = customer.copy(
                amcStatus = "Active",
                amcType = amcType,
                amcExpiryDate = newExpiryDate,
                amcPaymentStatus = paymentStatus
            )
            repository.updateCustomer(updated)
        }
    }

    fun addTechnician(name: String, phone: String, skillSet: String) {
        viewModelScope.launch {
            val tech = Technician(
                name = name,
                phone = phone,
                skillSet = skillSet,
                availability = "Available"
            )
            repository.insertTechnician(tech)
        }
    }

    fun updateTechnicianAvailability(techId: Int, availability: String) {
        viewModelScope.launch {
            val tech = technicians.value.find { it.id == techId } ?: return@launch
            val updated = tech.copy(availability = availability)
            repository.updateTechnician(updated)
        }
    }
}

// Factory for direct injection inside MainActivity
class ElevatorViewModelFactory(private val repository: ElevatorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElevatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ElevatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
