package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ElevatorRepository(private val elevatorDao: ElevatorDao) {

    val allCustomers: Flow<List<Customer>> = elevatorDao.getAllCustomers()
    val allAppointments: Flow<List<Appointment>> = elevatorDao.getAllAppointments()
    val allTickets: Flow<List<Ticket>> = elevatorDao.getAllTickets()
    val allTechnicians: Flow<List<Technician>> = elevatorDao.getAllTechnicians()

    fun getTicketById(id: Int): Flow<Ticket?> = elevatorDao.getTicketByIdFlow(id)

    suspend fun insertCustomer(customer: Customer) = elevatorDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = elevatorDao.updateCustomer(customer)
    suspend fun deleteCustomerById(id: Int) = elevatorDao.deleteCustomerById(id)

    suspend fun insertAppointment(appointment: Appointment) = elevatorDao.insertAppointment(appointment)
    suspend fun updateAppointment(appointment: Appointment) = elevatorDao.updateAppointment(appointment)
    suspend fun deleteAppointmentById(id: Int) = elevatorDao.deleteAppointmentById(id)

    suspend fun getDirectTicket(id: Int) = elevatorDao.getTicketById(id)
    suspend fun insertTicket(ticket: Ticket) = elevatorDao.insertTicket(ticket)
    suspend fun updateTicket(ticket: Ticket) = elevatorDao.updateTicket(ticket)
    suspend fun deleteTicketById(id: Int) = elevatorDao.deleteTicketById(id)

    suspend fun insertTechnician(technician: Technician) = elevatorDao.insertTechnician(technician)
    suspend fun updateTechnician(technician: Technician) = elevatorDao.updateTechnician(technician)
    suspend fun deleteTechnicianById(id: Int) = elevatorDao.deleteTechnicianById(id)

    /**
     * Pre-populates the database with realistic business data if empty.
     */
    suspend fun prePopulateIfEmpty() {
        val currentCustomers = allCustomers.first()
        if (currentCustomers.isNotEmpty()) return

        // 1. Insert Technicians
        val t1 = Technician(name = "Amit Sharma", phone = "+91 91122 33445", skillSet = "Emergency & Electrical Faults", availability = "Available")
        val t2 = Technician(name = "Vijay Chawla", phone = "+91 92233 44556", skillSet = "Routine Maintenance & AMC Service", availability = "On Job")
        val t3 = Technician(name = "Sanjay Patil", phone = "+91 93344 55667", skillSet = "Installation & Safety Auditing", availability = "Available")
        val t4 = Technician(name = "Rahul Mhatre", phone = "+91 94455 66778", skillSet = "Hydraulic & MRL Elevator Expert", availability = "Off Duty")

        val idT1 = elevatorDao.insertTechnician(t1).toInt()
        val idT2 = elevatorDao.insertTechnician(t2).toInt()
        val idT3 = elevatorDao.insertTechnician(t3).toInt()
        val idT4 = elevatorDao.insertTechnician(t4).toInt()

        // 2. Insert Customers
        val c1 = Customer(
            name = "Ramesh Patel",
            phone = "+91 98200 12345",
            email = "ramesh@pateltex.com",
            buildingName = "Kalyan Heights",
            address = "Kalyan Road, near Shivaji Chowk, Bhiwandi",
            elevatorModel = "8-Passenger Gearless MRL Lift",
            installDate = "2023-04-10",
            lastServiceDate = "2026-05-15",
            nextDueDate = "2026-06-15",
            amcStatus = "Active",
            amcType = "Comprehensive",
            amcExpiryDate = "2027-04-10",
            amcPaymentStatus = "Paid"
        )
        val c2 = Customer(
            name = "Sai Developers (Mohan Lal)",
            phone = "+91 90041 55667",
            email = "info@saidevelopers.in",
            buildingName = "Sai Paradise",
            address = "Kalyan Road, Behind Octroi Naka, Bhiwandi",
            elevatorModel = "15-Passenger Stretcher Elev. (Hydraulic)",
            installDate = "2024-01-20",
            lastServiceDate = "2026-05-01",
            nextDueDate = "2026-06-01",
            amcStatus = "Renewal Due",
            amcType = "Basic",
            amcExpiryDate = "2026-06-20",
            amcPaymentStatus = "Pending"
        )
        val c3 = Customer(
            name = "Vardhaman Synthetics Ltd.",
            phone = "+91 93222 99881",
            email = "vardhaman@textilebhiwandi.com",
            buildingName = "Vardhaman Warehouse",
            address = "Anjur Phata, Mankoli Road, Bhiwandi",
            elevatorModel = "2-Ton Heavy Duty Industrial Freight lift",
            installDate = "2022-09-05",
            lastServiceDate = "2026-04-20",
            nextDueDate = "2026-05-20",
            amcStatus = "Expired",
            amcType = "Comprehensive",
            amcExpiryDate = "2026-05-05",
            amcPaymentStatus = "Pending"
        )
        val c4 = Customer(
            name = "Shanti Sadan Society",
            phone = "+91 88888 77665",
            email = "shantisadan@outlook.com",
            buildingName = "Shanti Sadan CHS Ltd.",
            address = "Temghar, Pipeline Road, Bhiwandi",
            elevatorModel = "6-Passenger Passenger Cabin Elevator",
            installDate = "2025-06-15",
            lastServiceDate = "2026-05-28",
            nextDueDate = "2026-06-12",
            amcStatus = "Active",
            amcType = "Basic",
            amcExpiryDate = "2026-06-15", // Expires in 10 days
            amcPaymentStatus = "Paid"
        )

        val idC1 = elevatorDao.insertCustomer(c1).toInt()
        val idC2 = elevatorDao.insertCustomer(c2).toInt()
        val idC3 = elevatorDao.insertCustomer(c3).toInt()
        val idC4 = elevatorDao.insertCustomer(c4).toInt()

        // 3. Insert Appointments
        val a1 = Appointment(
            customerId = idC1,
            customerName = "Ramesh Patel",
            customerPhone = "+91 98200 12345",
            address = "Kalyan Road, near Shivaji Chowk, Bhiwandi",
            elevatorType = "8-Passenger Gearless MRL",
            serviceType = "AMC Service",
            appointmentDate = "2026-06-05", // Today
            appointmentTime = "11:00 AM",
            technicianId = idT2,
            technicianName = "Vijay Chawla",
            status = "In Progress",
            notes = "Standard monthly grease and control check."
        )
        val a2 = Appointment(
            customerId = idC2,
            customerName = "Sai Developers (Mohan Lal)",
            customerPhone = "+91 90041 55667",
            address = "Kalyan Road, Behind Octroi Naka, Bhiwandi",
            elevatorType = "15-Passenger Stretcher (Hydraulic)",
            serviceType = "Inspection",
            appointmentDate = "2026-06-05", // Today
            appointmentTime = "03:30 PM",
            technicianId = idT3,
            technicianName = "Sanjay Patil",
            status = "Scheduled",
            notes = "Post-monsoon water protection check in lift pit."
        )
        val a3 = Appointment(
            customerId = idC4,
            customerName = "Shanti Sadan Society",
            customerPhone = "+91 88888 77665",
            address = "Temghar, Pipeline Road, Bhiwandi",
            elevatorType = "6-Passenger Passenger Cabin",
            serviceType = "Repair",
            appointmentDate = "2026-06-06", // Tomorrow
            appointmentTime = "10:00 AM",
            technicianId = idT1,
            technicianName = "Amit Sharma",
            status = "Scheduled",
            notes = "Door sensor is triggering intermittently. Reset and test."
        )

        elevatorDao.insertAppointment(a1)
        elevatorDao.insertAppointment(a2)
        elevatorDao.insertAppointment(a3)

        // 4. Insert Tickets
        val tk1 = Ticket(
            customerId = idC3,
            customerName = "Vardhaman Synthetics Ltd.",
            customerPhone = "+91 93222 99881",
            title = "Elevator not working",
            priority = "Emergency",
            status = "Open",
            technicianId = null,
            technicianName = null,
            description = "Elevator is completely unresponsive and stuck on the 2nd floor. Goods loaded inside.",
            internalNotes = "Technician must bring voltage tester and safety locks.",
            createdAt = "2026-06-05"
        )
        val tk2 = Ticket(
            customerId = idC1,
            customerName = "Ramesh Patel",
            customerPhone = "+91 98200 12345",
            title = "Noise issue",
            priority = "Medium",
            status = "Resolved",
            technicianId = idT2,
            technicianName = "Vijay Chawla",
            description = "High-pitched squeal from hoist rope during ascent.",
            internalNotes = "Cleaned the ropes and lubricated guide shoes.",
            createdAt = "2026-06-03"
        )
        val tk3 = Ticket(
            customerId = idC2,
            customerName = "Sai Developers (Mohan Lal)",
            customerPhone = "+91 90041 55667",
            title = "Door problem",
            priority = "High",
            status = "Assigned",
            technicianId = idT1,
            technicianName = "Amit Sharma",
            description = "Door safety edge is false-triggering on 4th floor.",
            internalNotes = "Amit Sharma dispatched with spare photo-cell sensor unit.",
            createdAt = "2026-06-04"
        )

        elevatorDao.insertTicket(tk1)
        elevatorDao.insertTicket(tk2)
        elevatorDao.insertTicket(tk3)
    }
}
