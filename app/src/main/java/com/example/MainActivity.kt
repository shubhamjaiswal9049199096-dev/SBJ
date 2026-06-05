package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ElevatorViewModel
import com.example.ui.viewmodel.ElevatorViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ElevatorRepository(database.elevatorDao())
        val factory = ElevatorViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[ElevatorViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel)
            }
        }
    }
}

// Global Custom Colors matching "Clean Minimalism" & "S Elevators Theme"
object BrandColors {
    val DeepNavy = Color(0xFF0D1B2A)
    val SlateBlue = Color(0xFF1B263B)
    val DarkAccent = Color(0xFF415A77)
    val LightGrayBg = Color(0xFFF1F4F9)
    val AccentOrange = Color(0xFFFF8C00)
    val StatusGreenBg = Color(0xFFECFDF5)
    val StatusGreenText = Color(0xFF059669)
    val StatusRedBg = Color(0xFFFEF2F2)
    val StatusRedText = Color(0xFFDC2626)
    val StatusOrangeBg = Color(0xFFFFF7ED)
    val StatusOrangeText = Color(0xFFEA580C)
    val StatusBlueBg = Color(0xFFEFF6FF)
    val StatusBlueText = Color(0xFF2563EB)
}

enum class NavigationTab {
    DASHBOARD, APPOINTMENTS, TICKETS, CRM, TEAM, REPORT, COMPANY
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppScreen(viewModel: ElevatorViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    // Dialog state controllers
    var showApptDialog by remember { mutableStateOf(false) }
    var showTicketDialog by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showTechDialog by remember { mutableStateOf(false) }

    // Live Database Lists
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    val technicians by viewModel.technicians.collectAsStateWithLifecycle()

    // Simulation Overlay states
    var isSimulatingAction by remember { mutableStateOf(false) }
    var simulationTitle by remember { mutableStateOf("") }
    var simulationText by remember { mutableStateOf("") }

    fun triggerSimulation(title: String, text: String) {
        simulationTitle = title
        simulationText = text
        isSimulatingAction = true
    }

    Scaffold(
        topBar = {
            Column {
                // S Elevators Premium Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandColors.DeepNavy)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BHIWANDI OPERATIONS & SERVICE PLANNER",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "S Elevators",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        // User Badge "JS"
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandColors.AccentOrange)
                                .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JS",
                                color = BrandColors.DeepNavy,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // Bottom Tab Sheet using modern compact style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .shadow(12.dp)
                        .background(Color.White)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = listOf(
                        TabItem("Home", Icons.Filled.Home, NavigationTab.DASHBOARD),
                        TabItem("Appts", Icons.Filled.DateRange, NavigationTab.APPOINTMENTS),
                        TabItem("Tickets", Icons.Filled.ConfirmationNumber, NavigationTab.TICKETS),
                        TabItem("CRM", Icons.Filled.Group, NavigationTab.CRM),
                        TabItem("Team", Icons.Filled.Engineering, NavigationTab.TEAM),
                        TabItem("Company", Icons.Filled.Business, NavigationTab.COMPANY)
                    )

                    items.forEach { item ->
                        val isSelected = currentTab == item.tab
                        val tint = if (isSelected) BrandColors.AccentOrange else Color(0xFF94A3B8)
                        val bg = if (isSelected) Color(0xFFFFF3EB) else Color.Transparent

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { currentTab = item.tab }
                                .background(bg)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("tab_${item.tab.name.lowercase()}")
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = tint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                color = tint,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                // Custom Home indicator visual fill to support bottom design
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFE2E8F0))
                    )
                }
            }
        },
        containerColor = BrandColors.LightGrayBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tabChange"
            ) { target ->
                when (target) {
                    NavigationTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNewAppointment = { showApptDialog = true },
                        onNewTicket = { showTicketDialog = true },
                        onAddCustomer = { showCustomerDialog = true },
                        onViewAppointmentsTab = { currentTab = NavigationTab.APPOINTMENTS },
                        onViewTicketsTab = { currentTab = NavigationTab.TICKETS },
                        onSimulateAction = { ttl, msg -> triggerSimulation(ttl, msg) }
                    )
                    NavigationTab.APPOINTMENTS -> AppointmentSchedulingScreen(
                        viewModel = viewModel,
                        appointments = appointments,
                        customers = customers,
                        technicians = technicians,
                        onAddAppointmentClick = { showApptDialog = true },
                        onSimulateAction = { ttl, msg -> triggerSimulation(ttl, msg) }
                    )
                    NavigationTab.TICKETS -> TechnicalTicketsScreen(
                        viewModel = viewModel,
                        tickets = tickets,
                        customers = customers,
                        technicians = technicians,
                        onRaiseTicketClick = { showTicketDialog = true },
                        onSimulateAction = { ttl, msg -> triggerSimulation(ttl, msg) }
                    )
                    NavigationTab.CRM -> CustomerCrmScreen(
                        viewModel = viewModel,
                        customers = customers,
                        onAddCustomerClick = { showCustomerDialog = true },
                        onSimulateAction = { ttl, msg -> triggerSimulation(ttl, msg) }
                    )
                    NavigationTab.TEAM -> TechnicianManagementScreen(
                        viewModel = viewModel,
                        technicians = technicians,
                        appointments = appointments,
                        tickets = tickets,
                        onAddTechnicianClick = { showTechDialog = true }
                    )
                    NavigationTab.COMPANY -> CompanyAndReportsDashboard(
                        viewModel = viewModel,
                        customers = customers,
                        appointments = appointments,
                        tickets = tickets,
                        technicians = technicians,
                        onSimulateAction = { ttl, msg -> triggerSimulation(ttl, msg) }
                    )
                    else -> DashboardScreen(
                        viewModel = viewModel,
                        onNewAppointment = { showApptDialog = true },
                        onNewTicket = { showTicketDialog = true },
                        onAddCustomer = { showCustomerDialog = true },
                        onViewAppointmentsTab = { currentTab = NavigationTab.APPOINTMENTS },
                        onViewTicketsTab = { currentTab = NavigationTab.TICKETS },
                        onSimulateAction = { ttl, msg -> triggerSimulation(ttl, msg) }
                    )
                }
            }

            // --- POPUP DIALOGS ---

            if (showApptDialog) {
                AddAppointmentDialog(
                    customers = customers,
                    technicians = technicians,
                    onDismiss = { showApptDialog = false },
                    onConfirm = { cid, cname, cphone, addr, elev, serv, d, t, n, tid, tname ->
                        viewModel.bookAppointment(cid, cname, cphone, addr, elev, serv, d, t, n, tid, tname)
                        showApptDialog = false
                        Toast.makeText(context, "Appointment Booked Successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showTicketDialog) {
                RaiseTicketDialog(
                    customers = customers,
                    onDismiss = { showTicketDialog = false },
                    onConfirm = { cid, cname, cphone, title, priority, desc ->
                        viewModel.raiseTicket(cid, cname, cphone, title, priority, desc)
                        showTicketDialog = false
                        Toast.makeText(context, "Support Ticket Raised!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showCustomerDialog) {
                AddCustomerDialog(
                    onDismiss = { showCustomerDialog = false },
                    onConfirm = { name, phone, email, bld, addr, model, inst, amcType, amcStatus, expState, payState ->
                        viewModel.addCustomer(name, phone, email, bld, addr, model, inst, amcType, amcStatus, expState, payState)
                        showCustomerDialog = false
                        Toast.makeText(context, "New Customer Profile Registered!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showTechDialog) {
                AddTechnicianDialog(
                    onDismiss = { showTechDialog = false },
                    onConfirm = { name, phone, skill ->
                        viewModel.addTechnician(name, phone, skill)
                        showTechDialog = false
                        Toast.makeText(context, "Technician Enrolled!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Simulated Communication Overlay Drawer (WhatsApp / SMS Engine)
            if (isSimulatingAction) {
                Dialog(onDismissRequest = { isSimulatingAction = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandColors.SlateBlue)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(BrandColors.AccentOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "Simulated",
                                    tint = BrandColors.AccentOrange,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = simulationTitle,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = simulationText,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { isSimulatingAction = false },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.AccentOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Okey, Done", color = BrandColors.DeepNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector, val tab: NavigationTab)

// --- MODULE 1: DASHBOARD ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: ElevatorViewModel,
    onNewAppointment: () -> Unit,
    onNewTicket: () -> Unit,
    onAddCustomer: () -> Unit,
    onViewAppointmentsTab: () -> Unit,
    onViewTicketsTab: () -> Unit,
    onSimulateAction: (String, String) -> Unit
) {
    val appointmentsToday by viewModel.totalAppointmentsToday.collectAsStateWithLifecycle()
    val openTickets by viewModel.openTicketsCount.collectAsStateWithLifecycle()
    val pendingRenewals by viewModel.pendingAMCRenewalsCount.collectAsStateWithLifecycle()
    val revenue by viewModel.revenueThisMonth.collectAsStateWithLifecycle()

    val recentTickets by viewModel.tickets.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcoming Greeting Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome Back,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Shubham Jaiswal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.DeepNavy
                )
            }
            Text(
                text = "Bhiwandi, IN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandColors.AccentOrange,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF3EB))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Headline title for metrics
        Text(
            text = "LIVE PERFORMANCE METRICS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Metrics Grid (2x2)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card 1: Open Tickets (Navy Blue)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(125.dp)
                    .clickable { onViewTicketsTab() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = BrandColors.DeepNavy)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text("OPEN TICKETS", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$openTickets", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("REQUIRES ACTION", color = Color(0xFFFCA5A5), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Card 2: Today's Appt (White)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(125.dp)
                    .clickable { onViewAppointmentsTab() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text("TODAY'S APPT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$appointmentsToday", color = BrandColors.SlateBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    Text(
                        text = "LIVE LIST",
                        color = BrandColors.StatusGreenText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandColors.StatusGreenBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card 3: AMC Renewals (White)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(125.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text("AMC RENEWALS LENDING", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("$pendingRenewals", color = BrandColors.SlateBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    Text(
                        text = "PAST DUE / RENEWALS",
                        color = Color(0xFFEA580C),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFF7ED))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Card 4: Month Revenue (Orange Accent)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(125.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = BrandColors.AccentOrange)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text("EST MTD REVENUE", color = BrandColors.DeepNavy.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        val formattedRevenue = if (revenue >= 100000) "₹${String.format("%.1f", revenue / 100000.0)}L" else "₹${revenue.toInt()}"
                        Text(formattedRevenue, color = BrandColors.DeepNavy, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(
                        text = "100% INVOICED",
                        color = BrandColors.DeepNavy,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline Quick Actions
        Text(
            text = "QUICK ADMINISTRATIVE ACTIONS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Quick action buttons in Grid Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // New Ticket action
            Button(
                onClick = onNewTicket,
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .testTag("action_new_ticket"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandColors.StatusRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = BrandColors.StatusRedText)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("NEW TICKET", color = BrandColors.DeepNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // New Appointment action
            Button(
                onClick = onNewAppointment,
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .testTag("action_new_appt"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandColors.StatusBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = BrandColors.StatusBlueText, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("NEW APPOINTMENT", color = BrandColors.DeepNavy, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Add Customer action
            Button(
                onClick = onAddCustomer,
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .testTag("action_new_customer"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("ADD CUSTOMER", color = BrandColors.DeepNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Tickets Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "URGENT COMPLAINTS & TICKETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Text(
                text = "VIEW ALL",
                fontSize = 11.sp,
                color = BrandColors.AccentOrange,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onViewTicketsTab() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of Urgent Tickets on the dashboard
        if (recentTickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No recent tickets recorded inside Bhiwandi databases.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            recentTickets.take(3).forEach { ticket ->
                val priorityColor = when (ticket.priority) {
                    "Emergency" -> Color(0xFFEF4444)
                    "High" -> Color(0xFFF97316)
                    "Medium" -> Color(0xFFFBBF24)
                    else -> Color(0xFF3B82F6)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable {
                            onSimulateAction(
                                "Ticket Details: TKT-${ticket.id}",
                                "Customer: ${ticket.customerName}\nIssue: ${ticket.title}\nStatus: ${ticket.status}\nTechnician Assigned: ${ticket.technicianName ?: "None"}"
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored Left boundary edge indicator
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(50.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(priorityColor)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TKT-${1000 + ticket.id}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (ticket.status) {
                                                "Resolved" -> BrandColors.StatusGreenBg
                                                "Open" -> BrandColors.StatusRedBg
                                                else -> BrandColors.StatusOrangeBg
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = ticket.status.uppercase(),
                                        color = when (ticket.status) {
                                            "Resolved" -> BrandColors.StatusGreenText
                                            "Open" -> BrandColors.StatusRedText
                                            else -> BrandColors.StatusOrangeText
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ticket.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.DeepNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ticket.customerName,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(priorityColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = ticket.priority.uppercase(),
                                    color = priorityColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Today",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- MODULE 2: APPOINTMENT SCHEDULING ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppointmentSchedulingScreen(
    viewModel: ElevatorViewModel,
    appointments: List<Appointment>,
    customers: List<Customer>,
    technicians: List<Technician>,
    onAddAppointmentClick: () -> Unit,
    onSimulateAction: (String, String) -> Unit
) {
    var filterDate by remember { mutableStateOf("2026-06-05") } // Default to simulated current date
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "APPOINTMENT DIARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Scheduler & Dispatcher",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.DeepNavy
                )
            }

            Button(
                onClick = onAddAppointmentClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.AccentOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = BrandColors.DeepNavy, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("BOOK", color = BrandColors.DeepNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simple Calendar Grid Day Selector (June 2026)
        Text(
            text = "SELECT DATE TO EXPEDITE VIEW",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val daysList = listOf(
                Pair("05", "Fri"),
                Pair("06", "Sat"),
                Pair("07", "Sun"),
                Pair("08", "Mon"),
                Pair("09", "Tue"),
                Pair("10", "Wed"),
                Pair("11", "Thu")
            )

            daysList.forEach { (day, weekday) ->
                val dateStr = "2026-06-$day"
                val isSelected = filterDate == dateStr

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(55.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { filterDate = dateStr }
                        .background(if (isSelected) BrandColors.AccentOrange else Color.White)
                        .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = weekday.uppercase(),
                        color = if (isSelected) BrandColors.DeepNavy else Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = day,
                        color = if (isSelected) BrandColors.DeepNavy else BrandColors.DeepNavy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredAppointments = appointments.filter { it.appointmentDate == filterDate }

        Text(
            text = "ASSIGNMENTS ON $filterDate (${filteredAppointments.size})",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredAppointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No appointments booked for this date.", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Click 'BOOK' at top to add one instantly.", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredAppointments) { appt ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = null,
                                        tint = BrandColors.AccentOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = appt.appointmentTime,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandColors.DeepNavy,
                                        fontSize = 14.sp
                                    )
                                }

                                // Status Dropdown / Action trigger
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (appt.status) {
                                                "Completed" -> BrandColors.StatusGreenBg
                                                "In Progress" -> BrandColors.StatusBlueBg
                                                else -> Color(0xFFF1F5F9)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable {
                                            val nextStatus = when (appt.status) {
                                                "Scheduled" -> "In Progress"
                                                "In Progress" -> "Completed"
                                                else -> "Scheduled"
                                            }
                                            viewModel.updateAppointmentStatus(appt.id, nextStatus)
                                            Toast
                                                .makeText(context, "Status updated to $nextStatus!", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                ) {
                                    Text(
                                        text = appt.status.uppercase(),
                                        color = when (appt.status) {
                                            "Completed" -> BrandColors.StatusGreenText
                                            "In Progress" -> BrandColors.StatusBlueText
                                            else -> Color(0xFF475569)
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = appt.customerName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.SlateBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = appt.address,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("SERVICE ASSIGNED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                    Text(
                                        text = "${appt.serviceType} (${appt.elevatorType})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandColors.DeepNavy
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("TECHNICIAN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                    Text(
                                        text = appt.technicianName ?: "Waiting Dispatch",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (appt.technicianId != null) BrandColors.StatusGreenText else Color.Red
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons: SMS/WhatsApp + Assign Tech helper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onSimulateAction(
                                            "Simulated SMS Notification Sent!",
                                            "To: ${appt.customerName} (${appt.customerPhone})\nMessage: S Elevators Notice: Your ${appt.serviceType} is booked slot ${appt.appointmentTime} with engineer ${appt.technicianName ?: "TBD"}. Queries: 9049199096."
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.LightGrayBg),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = null, tint = BrandColors.SlateBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simulate SMS", color = BrandColors.SlateBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onSimulateAction(
                                            "WhatsApp Dispatch Summary",
                                            "Opening WhatsApp API Sandbox...\nTriggered WhatsApp Alert to ${appt.customerPhone} for service ${appt.serviceType} success notification."
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7)),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", color = Color(0xFF15803D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- MODULE 3: HELP DESK / TICKET SYSTEM ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TechnicalTicketsScreen(
    viewModel: ElevatorViewModel,
    tickets: List<Ticket>,
    customers: List<Customer>,
    technicians: List<Technician>,
    onRaiseTicketClick: () -> Unit,
    onSimulateAction: (String, String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriorityFilter by remember { mutableStateOf("All") }

    // Search Engine by ticket ID (StateFlow connected in ViewModel)
    val searchedIdInput by viewModel.ticketSearchId.collectAsStateWithLifecycle()
    val searchedTicketResult by viewModel.searchedTicket.collectAsStateWithLifecycle()

    var showAssignSheet by remember { mutableStateOf<Ticket?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BHIWANDI TECHNICAL HELPDESK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Complaints & Faults",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.DeepNavy
                )
            }

            Button(
                onClick = onRaiseTicketClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.DeepNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RAISE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-use search pane: Client ID Search Tracker VS Normal Query
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("CUSTOMER FAULT TRACKER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BrandColors.AccentOrange)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = searchedIdInput,
                        onValueChange = { viewModel.updateTicketSearchId(it) },
                        placeholder = { Text("Track status. Enter 4-Digit Ticket ID eg. 1001", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BrandColors.LightGrayBg,
                            unfocusedContainerColor = BrandColors.LightGrayBg,
                            disabledContainerColor = BrandColors.LightGrayBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    if (searchedIdInput.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateTicketSearchId("") }) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                    }
                }

                // If searched ticket is found, render visual timeline tracker
                searchedTicketResult?.let { ticket ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandColors.LightGrayBg)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("TICKET ID: TKT-${1000 + ticket.id} (${ticket.title})", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrandColors.DeepNavy)
                            Text("Customer: ${ticket.customerName}", fontSize = 10.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Visual Track States: Open -> Assigned -> In Progress -> Resolved -> Closed
                            val steps = listOf("Open", "Assigned", "In Progress", "Resolved", "Closed")
                            val currentStepIndex = steps.indexOf(ticket.status)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                steps.forEachIndexed { index, step ->
                                    val checked = index <= currentStepIndex
                                    val activeColor = if (checked) BrandColors.AccentOrange else Color.LightGray

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(activeColor)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = step,
                                            fontSize = 8.sp,
                                            fontWeight = if (index == currentStepIndex) FontWeight.Bold else FontWeight.Normal,
                                            color = if (index == currentStepIndex) BrandColors.DeepNavy else Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Priority Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val priorities = listOf("All", "Emergency", "High", "Medium", "Low")
            priorities.forEach { pr ->
                val isSel = selectedPriorityFilter == pr
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedPriorityFilter = pr }
                        .background(if (isSel) BrandColors.DeepNavy else Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = pr,
                        color = if (isSel) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main List
        val filteredTickets = tickets.filter {
            selectedPriorityFilter == "All" || it.priority.lowercase() == selectedPriorityFilter.lowercase()
        }

        if (filteredTickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No matching open fault registers found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTickets) { ticket ->
                    val priorityColor = when (ticket.priority) {
                        "Emergency" -> Color(0xFFEF4444)
                        "High" -> Color(0xFFF97316)
                        "Medium" -> Color(0xFFFBBF24)
                        else -> Color(0xFF3B82F6)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TKT-${1000 + ticket.id}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.LightGray
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(priorityColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        ticket.priority.uppercase(),
                                        color = priorityColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ticket.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.DeepNavy
                            )
                            Text(
                                text = "Client: ${ticket.customerName}",
                                fontSize = 12.sp,
                                color = BrandColors.SlateBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ticket.description,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("CURRENT REPAIR ENGINEER", fontSize = 8.sp, color = Color.Gray)
                                    Text(
                                        text = ticket.technicianName ?: "UNASSIGNED",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ticket.technicianId != null) BrandColors.StatusGreenText else Color.Red
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Status update quick-switch chip
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .clickable {
                                                showAssignSheet = ticket
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("ASSIGN ENG", color = BrandColors.SlateBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BrandColors.AccentOrange)
                                            .clickable {
                                                val nextSt = when (ticket.status) {
                                                    "Open" -> "Assigned"
                                                    "Assigned" -> "In Progress"
                                                    "In Progress" -> "Resolved"
                                                    "Resolved" -> "Closed"
                                                    else -> "Open"
                                                }
                                                viewModel.assignAndUpdateTicket(
                                                    ticket.id,
                                                    nextSt,
                                                    ticket.technicianId,
                                                    ticket.technicianName,
                                                    ticket.internalNotes
                                                )
                                                Toast
                                                    .makeText(context, "Ticket updated to $nextSt", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = ticket.status.uppercase(),
                                                color = BrandColors.DeepNavy,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = null,
                                                tint = BrandColors.DeepNavy,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (ticket.internalNotes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFFBEB))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "INTERNAL NOTE: ${ticket.internalNotes}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Assign Technician Dropdown overlay
        showAssignSheet?.let { ticketToAssign ->
            Dialog(onDismissRequest = { showAssignSheet = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Assign Repair Technician",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.DeepNavy
                        )
                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Text("Select an engineer below to dispatch to Bhiwandi site:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        technicians.forEach { tech ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.assignAndUpdateTicket(
                                            ticketToAssign.id,
                                            "Assigned",
                                            tech.id,
                                            tech.name,
                                            "Assigned to ${tech.name}. Outstation job."
                                        )
                                        showAssignSheet = null
                                        Toast
                                            .makeText(context, "${tech.name} dispatched!", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (tech.availability == "Available") Color.Green else Color.Yellow)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tech.name, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
                                }
                                Text(
                                    text = tech.skillSet,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- MODULE 4: CUSTOMER CRM ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerCrmScreen(
    viewModel: ElevatorViewModel,
    customers: List<Customer>,
    onAddCustomerClick: () -> Unit,
    onSimulateAction: (String, String) -> Unit
) {
    var searchTxt by remember { mutableStateOf("") }
    var amcStatusFilter by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BHIWANDI DIRECTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Customer CRM & Lifts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.DeepNavy
                )
            }

            Button(
                onClick = onAddCustomerClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.DeepNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search CRM bar
        TextField(
            value = searchTxt,
            onValueChange = { searchTxt = it },
            placeholder = { Text("Search customers or buildings...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters: Active / Expired / Renewal Due / All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val states = listOf("All", "Active", "Renewal Due", "Expired")
            states.forEach { st ->
                val active = amcStatusFilter == st
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { amcStatusFilter = st }
                        .background(if (active) BrandColors.AccentOrange else Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = st.uppercase(),
                        color = if (active) BrandColors.DeepNavy else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val filteredCustomers = customers.filter { cust ->
            (cust.name.contains(searchTxt, ignoreCase = true) || cust.buildingName.contains(searchTxt, ignoreCase = true)) &&
                    (amcStatusFilter == "All" || cust.amcStatus.uppercase() == amcStatusFilter.uppercase())
        }

        if (filteredCustomers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No customers found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCustomers) { cust ->
                    // Highlighting critical alerts: 30 / 15 / 7 days
                    val isExpired = cust.amcStatus == "Expired"
                    val isRenewalDue = cust.amcStatus == "Renewal Due"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            1.dp,
                            if (isExpired) Color.Red.copy(alpha = 0.5f) else if (isRenewalDue) BrandColors.AccentOrange.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cust.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandColors.DeepNavy
                                    )
                                    Text(
                                        text = cust.buildingName,
                                        fontSize = 12.sp,
                                        color = BrandColors.SlateBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (cust.amcStatus) {
                                                "Active" -> BrandColors.StatusGreenBg
                                                "Expired" -> BrandColors.StatusRedBg
                                                else -> BrandColors.StatusOrangeBg
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cust.amcStatus.uppercase(),
                                        color = when (cust.amcStatus) {
                                            "Active" -> BrandColors.StatusGreenText
                                            "Expired" -> BrandColors.StatusRedText
                                            else -> BrandColors.StatusOrangeText
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PinDrop, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(cust.address, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Elevator specifications details per customer
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Elevator, contentDescription = null, tint = BrandColors.AccentOrange, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("LIFT UNIT:", fontSize = 8.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                                    Text("${cust.elevatorModel} (Inst: ${cust.installDate})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Last / Next Maintenance Schedule
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("LAST SERVICE", fontSize = 8.sp, color = Color.Gray)
                                    Text(cust.lastServiceDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("NEXT SERVICE DUE", fontSize = 8.sp, color = Color.Gray)
                                    Text(cust.nextDueDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandColors.AccentOrange)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // One-click actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onSimulateAction(
                                            "Dialing Phone Client",
                                            "S Elevators Call Logger initiating call:\nCalling ${cust.name} at: ${cust.phone}\nBhiwandi Landline linked standard gateway."
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.LightGrayBg),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = null, tint = BrandColors.DeepNavy, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call ${cust.phone}", color = BrandColors.DeepNavy, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = {
                                        onSimulateAction(
                                            "WhatsApp AMC Contract Manager",
                                            "Contacting ${cust.name} via WhatsApp Business client:\nMessage template: 'Dear ${cust.name}, S Elevators would like to request AMC contract renewal inspection scheduling for ${cust.buildingName}. Please contact +91 9049199096'"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Filled.Message, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp AMC", color = Color(0xFF15803D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- MODULE 5: TECHNICIAN MANAGEMENT ---
@Composable
fun TechnicianManagementScreen(
    viewModel: ElevatorViewModel,
    technicians: List<Technician>,
    appointments: List<Appointment>,
    tickets: List<Ticket>,
    onAddTechnicianClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "S ELEVATORS FIELD FORCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Technician Desk",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.DeepNavy
                )
            }

            Button(
                onClick = onAddTechnicianClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.DeepNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD TECH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (technicians.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No field force members configured inside systems.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(technicians) { tech ->
                    // Calculate assigned metrics dynamically
                    val activeApptsList = appointments.filter { it.technicianId == tech.id && it.status != "Completed" }
                    val activeTktsList = tickets.filter { it.technicianId == tech.id && it.status != "Resolved" && it.status != "Closed" }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = tech.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandColors.DeepNavy
                                    )
                                    Text(
                                        text = tech.skillSet,
                                        fontSize = 11.sp,
                                        color = BrandColors.AccentOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Interactive State Toggler
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (tech.availability) {
                                                "Available" -> BrandColors.StatusGreenBg
                                                "On Job" -> BrandColors.StatusBlueBg
                                                else -> Color(0xFFF1F5F9)
                                            }
                                        )
                                        .clickable {
                                            val nextAv = when (tech.availability) {
                                                "Available" -> "On Job"
                                                "On Job" -> "Off Duty"
                                                else -> "Available"
                                            }
                                            viewModel.updateTechnicianAvailability(tech.id, nextAv)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tech.availability.uppercase(),
                                        color = when (tech.availability) {
                                            "Available" -> BrandColors.StatusGreenText
                                            "On Job" -> BrandColors.StatusBlueText
                                            else -> Color(0xFF475569)
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Contact: ${tech.phone}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Day's assignment records
                            Text("ACTIVE WORKLOAD TODAY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = BrandColors.LightGrayBg),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("${activeApptsList.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandColors.DeepNavy)
                                        Text("Appts Pending", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = BrandColors.LightGrayBg),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("${activeTktsList.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandColors.AccentOrange)
                                        Text("Tickets Active", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- MODULE 6 & 7 & 8: REPORTS, AMC & COMPANY INFO DASHBOARD ---
@Composable
fun CompanyAndReportsDashboard(
    viewModel: ElevatorViewModel,
    customers: List<Customer>,
    appointments: List<Appointment>,
    tickets: List<Ticket>,
    technicians: List<Technician>,
    onSimulateAction: (String, String) -> Unit
) {
    var reportsSubTab by remember { mutableStateOf("AMC") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Module Picker tab row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val subpages = listOf("AMC", "Analytics", "About S Elevators")
            subpages.forEach { pg ->
                val chosen = reportsSubTab == pg
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { reportsSubTab = pg }
                        .background(if (chosen) BrandColors.DeepNavy else Color.Transparent)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pg.uppercase(),
                        color = if (chosen) Color.White else Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (reportsSubTab) {
            "AMC" -> AmcContractModule(viewModel, customers, onSimulateAction)
            "Analytics" -> AnalyticsModule(viewModel, customers, appointments, tickets)
            else -> CompanyAboutModule(onSimulateAction)
        }
    }
}

@Composable
fun AmcContractModule(
    viewModel: ElevatorViewModel,
    customers: List<Customer>,
    onSimulateAction: (String, String) -> Unit
) {
    val context = LocalContext.current
    val totalAmc = customers.size
    val pendingAmc = customers.count { it.amcStatus != "Active" }

    Text("AMC CONTRACT EXPIRY & RENEWALS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(customers) { cust ->
            val isUrgent = cust.amcStatus == "Expired" || cust.amcStatus == "Renewal Due"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, if (isUrgent) BrandColors.AccentOrange else Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cust.buildingName,
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.DeepNavy,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (cust.amcType == "Comprehensive") Color(0xFFECEFDF) else Color(0xFFEFF6FF))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = cust.amcType.uppercase(),
                                color = if (cust.amcType == "Comprehensive") Color(0xFF6B8B00) else Color(0xFF1E40AF),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Owner Liaison: ${cust.name}", fontSize = 11.sp, color = Color.Gray)
                    Text("Expiry Date: ${cust.amcExpiryDate}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("PAYMENT STATUS: ", fontSize = 8.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (cust.amcPaymentStatus == "Paid") BrandColors.StatusGreenBg else BrandColors.StatusRedBg)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = cust.amcPaymentStatus.uppercase(),
                                    color = if (cust.amcPaymentStatus == "Paid") BrandColors.StatusGreenText else BrandColors.StatusRedText,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Renew Quick Click Button
                        Button(
                            onClick = {
                                // Add 1 Year to Date
                                val newExpiry = "2027-06-05"
                                viewModel.renewAMC(cust.id, cust.amcType, newExpiry, "Paid")
                                Toast.makeText(context, "AMC Renewed for ${cust.buildingName}!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandColors.AccentOrange),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("RENEW 1 YR", color = BrandColors.DeepNavy, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsModule(
    viewModel: ElevatorViewModel,
    customers: List<Customer>,
    appointments: List<Appointment>,
    tickets: List<Ticket>
) {
    val completedApptsCount = appointments.count { it.status == "Completed" }
    val openTicketsCount = tickets.count { it.status != "Resolved" && it.status != "Closed" }

    // Breakdown for complaint most common categories visual charts
    val catNotWorking = tickets.count { it.title.contains("not working", ignoreCase = true) }
    val catNoise = tickets.count { it.title.contains("noise", ignoreCase = true) }
    val catDoor = tickets.count { it.title.contains("door", ignoreCase = true) }
    val catOther = tickets.size - (catNotWorking + catNoise + catDoor)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("BUSINESS CHARTS & PERFORMANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))

        // Revenue Visual Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandColors.DeepNavy)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("CUMULATIVE INCOME THIS MONTH", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("₹2,42,500.00", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Calculation based on Active Comprehensive subscriptions in Bhiwandi", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simple Progress Line Chart representing: "Most common complaint types"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("MOST COMMON COMPLAINTS (MONTH-TO-DATE)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
                Spacer(modifier = Modifier.height(12.dp))

                // Bar 1: Elevator Stuck / Not Working
                ComplaintStatRow(label = "Elevator Stuck / Not Working", count = catNotWorking, total = tickets.size, color = Color(0xFFEF4444))
                Spacer(modifier = Modifier.height(8.dp))

                // Bar 2: Noise Issue
                ComplaintStatRow(label = "Strange Screech / Hoist Noise", count = catNoise, total = tickets.size, color = Color(0xFFF97316))
                Spacer(modifier = Modifier.height(8.dp))

                // Bar 3: Door Intermittent Fault
                ComplaintStatRow(label = "Door Sensor Problems", count = catDoor, total = tickets.size, color = Color(0xFF3B82F6))
                Spacer(modifier = Modifier.height(8.dp))

                // Bar 4: Other
                ComplaintStatRow(label = "Other Electrical / Routine Complaints", count = catOther.coerceAtLeast(1), total = tickets.size, color = Color(0xFF8B5CF6))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Performance widget metrics cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$completedApptsCount", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrandColors.DeepNavy)
                    Text("Routine Visits Done", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$openTicketsCount", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Red)
                    Text("Emergency Jobs Left", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun ComplaintStatRow(label: String, count: Int, total: Int, color: Color) {
    val totalCount = if (total == 0) 1 else total
    val ratio = count.toFloat() / totalCount.toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, color = BrandColors.SlateBlue, fontWeight = FontWeight.Medium)
            Text("$count cases (${(ratio * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceAtLeast(0.05f))
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun CompanyAboutModule(onSimulateAction: (String, String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Branding Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(BrandColors.AccentOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Elevator, contentDescription = null, tint = BrandColors.DeepNavy, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("S ELEVATORS INDIA", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandColors.DeepNavy)
                Text("High Performance Lift Engineering", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Laying standards in elevator installations, modernization, maintenance contracts, and prompt 24/7 breakdown responses in Bhiwandi and surrounding areas of Maharashtra.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prominent Emergency Helpline Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onSimulateAction(
                        "Speed Dialing Emergency Helpline",
                        "Dialing S Elevators 24/7 Bhiwandi Hotline:\n+91 90491 99096\nService units status: Red alert dispatchers notified."
                    )
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            border = BorderStroke(1.5.dp, Color(0xFFEF4444))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text("EMERGENCY 24/7 HELPLINE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    Text("+91 90491 99096", fontSize = 18.sp, fontWeight = FontWeight.Black, color = BrandColors.DeepNavy)
                    Text("Click to speed-dial dispatch desk", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Services Card list
        Text("OUR ENGINEERING VERTICALS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        val verticals = listOf(
            VerticalItem("Installation", "Custom tailored passenger & heavy freight lifts.", Icons.Filled.Construction),
            VerticalItem("AMC Service", "Low-cost high-compliance standard checks.", Icons.Filled.Verified),
            VerticalItem("Repair & Spares", "Immediate gearless block and pit resolution.", Icons.Filled.Build),
            VerticalItem("Modernization", "Converting old cable installations to reliable energy efficient MRL controllers.", Icons.Filled.TrendingUp)
        )

        verticals.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandColors.AccentOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = item.icon, contentDescription = null, tint = BrandColors.AccentOrange, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandColors.DeepNavy)
                    Text(item.desc, fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

data class VerticalItem(val title: String, val desc: String, val icon: ImageVector)


// --- POPUP FORM DIALOG DEFINITIONS ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAppointmentDialog(
    customers: List<Customer>,
    technicians: List<Technician>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String, String, String, String, String, String, String, Int?, String?) -> Unit
) {
    var selectedCust by remember { mutableStateOf<Customer?>(null) }
    var serviceType by remember { mutableStateOf("AMC Service") }
    var customDate by remember { mutableStateOf("2026-06-05") }
    var customTime by remember { mutableStateOf("11:00 AM") }
    var notes by remember { mutableStateOf("") }
    var selectedTech by remember { mutableStateOf<Technician?>(null) }

    var expandedSelect by remember { mutableStateOf(false) }
    var expandedTechSelect by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Schedule New Appointment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
                Spacer(modifier = Modifier.height(14.dp))

                // Customer Selector
                Text("Customer Site Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                        .clickable { expandedSelect = !expandedSelect }
                        .padding(12.dp)
                ) {
                    Text(
                        text = selectedCust?.name ?: "Click to pick from CRM database",
                        color = if (selectedCust != null) BrandColors.DeepNavy else Color.Gray,
                        fontSize = 13.sp
                    )
                }

                if (expandedSelect) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        customers.forEach { c ->
                            Text(
                                text = "${c.name} (${c.buildingName})",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCust = c
                                        expandedSelect = false
                                    }
                                    .padding(vertical = 8.dp),
                                fontSize = 13.sp,
                                color = BrandColors.DeepNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Service Type Dropdown
                Text("Vertical Service", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf("Installation", "AMC Service", "Repair", "Inspection")
                    types.forEach { type ->
                        val active = serviceType == type
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { serviceType = type }
                                .background(if (active) BrandColors.DeepNavy else Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(type, color = if (active) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Time PICKERS
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Day (YYYY-MM-DD)", fontSize = 11.sp, color = Color.Gray)
                        TextField(
                            value = customDate,
                            onValueChange = { customDate = it },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Expected Time Slot", fontSize = 11.sp, color = Color.Gray)
                        TextField(
                            value = customTime,
                            onValueChange = { customTime = it },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Assign Force Technician availability previewer
                Text("Select & Dispatch Technician", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                        .clickable { expandedTechSelect = !expandedTechSelect }
                        .padding(12.dp)
                ) {
                    Text(
                        text = selectedTech?.name ?: "Click to select and assign",
                        color = if (selectedTech != null) BrandColors.DeepNavy else Color.Gray,
                        fontSize = 13.sp
                    )
                }

                if (expandedTechSelect) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        technicians.forEach { tech ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTech = tech
                                        expandedTechSelect = false
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tech.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = tech.availability.uppercase(),
                                    color = if (tech.availability == "Available") Color.Green else Color.Yellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Special Instructions", fontSize = 11.sp, color = Color.Gray)
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Eg. grease rope line/check block panel sensors") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            selectedCust?.let { c ->
                                onConfirm(
                                    c.id,
                                    c.name,
                                    c.phone,
                                    c.address,
                                    c.elevatorModel,
                                    serviceType,
                                    customDate,
                                    customTime,
                                    notes,
                                    selectedTech?.id,
                                    selectedTech?.name
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColors.AccentOrange),
                        enabled = selectedCust != null
                    ) {
                        Text("CONFIRM BOOKING", color = BrandColors.DeepNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RaiseTicketDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String, String, String, String) -> Unit
) {
    var selectedCust by remember { mutableStateOf<Customer?>(null) }
    var title by remember { mutableStateOf("Elevator not working") }
    var priority by remember { mutableStateOf("High") }
    var desc by remember { mutableStateOf("") }
    var expandedSelect by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Raise Technical Complaint / Ticket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
                Spacer(modifier = Modifier.height(14.dp))

                // Customer Profile select
                Text("Select Registered Site Customer", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                        .clickable { expandedSelect = !expandedSelect }
                        .padding(12.dp)
                ) {
                    Text(
                        text = selectedCust?.name ?: "Click to select customer",
                        color = if (selectedCust != null) BrandColors.DeepNavy else Color.Gray,
                        fontSize = 13.sp
                    )
                }

                if (expandedSelect) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        customers.forEach { c ->
                            Text(
                                text = "${c.name} (${c.buildingName})",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCust = c
                                        expandedSelect = false
                                    }
                                    .padding(vertical = 8.dp),
                                fontSize = 13.sp,
                                color = BrandColors.DeepNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ticket Titles options
                Text("Complaint Nature", fontSize = 11.sp, color = Color.Gray)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val problems = listOf("Elevator not working", "Noise issue", "Door problem", "Electrical fault", "Routine complaint")
                    problems.forEach { pr ->
                        val active = title == pr
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { title = pr }
                                .background(if (active) BrandColors.DeepNavy else Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(pr, color = if (active) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Urgent Priorities options
                Text("Impact Level / Alert Priority", fontSize = 11.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val limits = listOf("Low", "Medium", "High", "Emergency")
                    limits.forEach { lm ->
                        val current = priority == lm
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { priority = lm }
                                .background(if (current) BrandColors.AccentOrange else Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lm, color = BrandColors.DeepNavy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Incident Fault Description", fontSize = 11.sp, color = Color.Gray)
                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    placeholder = { Text("Explain what happens. e.g. Lift trapped with 2 bags on 3rd floor.") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            selectedCust?.let { c ->
                                onConfirm(c.id, c.name, c.phone, title, priority, desc)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColors.DeepNavy),
                        enabled = selectedCust != null
                    ) {
                        Text("EMPLACE FAULT REPORT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bldName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var elevatorModel by remember { mutableStateOf("Passenger Lift MRL") }
    var amcType by remember { mutableStateOf("Comprehensive") }
    var expiryDate by remember { mutableStateOf("2027-06-05") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Register New Customer Site", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Contract Holder Name", fontSize = 11.sp, color = Color.Gray)
                TextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))

                Spacer(modifier = Modifier.height(8.dp))

                Text("Contact Phone (+91 Indian Format)", fontSize = 11.sp, color = Color.Gray)
                TextField(value = phone, onValueChange = { phone = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))

                Spacer(modifier = Modifier.height(8.dp))

                Text("Email Coordinate", fontSize = 11.sp, color = Color.Gray)
                TextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))

                Spacer(modifier = Modifier.height(8.dp))

                Text("Building Sadan Name", fontSize = 11.sp, color = Color.Gray)
                TextField(value = bldName, onValueChange = { bldName = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))

                Spacer(modifier = Modifier.height(8.dp))

                Text("Complete Bhiwandi Street Address", fontSize = 11.sp, color = Color.Gray)
                TextField(value = address, onValueChange = { address = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))

                Spacer(modifier = Modifier.height(8.dp))

                Text("Cabin/Elevator Carriage Spec Model", fontSize = 11.sp, color = Color.Gray)
                TextField(value = elevatorModel, onValueChange = { elevatorModel = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AMC Product", fontSize = 11.sp, color = Color.Gray)
                        Row {
                            RadioButton(selected = amcType == "Comprehensive", onClick = { amcType = "Comprehensive" })
                            Text("Comp.", fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
                            Spacer(modifier = Modifier.width(4.dp))
                            RadioButton(selected = amcType == "Basic", onClick = { amcType = "Basic" })
                            Text("Basic", fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contract Expiry", fontSize = 11.sp, color = Color.Gray)
                        TextField(value = expiryDate, onValueChange = { expiryDate = it }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onConfirm(name, phone, email, bldName, address, elevatorModel, "2026-06-05", amcType, "Active", expiryDate, "Paid")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColors.AccentOrange),
                        enabled = name.isNotEmpty() && phone.isNotEmpty()
                    ) {
                        Text("SAVE PROFILE", color = BrandColors.DeepNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTechnicianDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("Routine Maintenance") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Enroll Field Maintenance Force", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandColors.DeepNavy)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Technician Full Name", fontSize = 11.sp, color = Color.Gray)
                TextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))

                Spacer(modifier = Modifier.height(10.dp))

                Text("Mobile Phone", fontSize = 11.sp, color = Color.Gray)
                TextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))

                Spacer(modifier = Modifier.height(10.dp))

                Text("Staff Speciality Rating", fontSize = 11.sp, color = Color.Gray)
                val spec = listOf("Routine Maintenance", "Emergency Screech Expert", "Pit Water Extraction Specialist", "Safety Regulator")
                spec.forEach { sp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { skill = sp }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = skill == sp, onClick = { skill = sp })
                        Text(sp, fontSize = 13.sp, color = BrandColors.DeepNavy, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onConfirm(name, phone, skill) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColors.AccentOrange),
                        enabled = name.isNotEmpty() && phone.isNotEmpty()
                    ) {
                        Text("ENROLL FORCE", color = BrandColors.DeepNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
