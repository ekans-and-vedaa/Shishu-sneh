package com.shishusneh

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.shishusneh.data.Milestone
import com.shishusneh.data.ShishuDatabase
import com.shishusneh.data.ShishuRepository
import com.shishusneh.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = ShishuDatabase.getDatabase(this)
        val repository = ShishuRepository(database.dao())
        val workManager = WorkManager.getInstance(this)

        setContent {
            ShishuSnehTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) {}
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val viewModel: MainViewModel = viewModel(factory = ViewModelFactory(repository, workManager))
                MainApp(viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val hasProfile by viewModel.hasProfile.collectAsState(initial = false)

    if (!hasProfile) {
        OnboardingScreen { name, dob, gender ->
            viewModel.registerBaby(name, dob, gender)
        }
    } else {
        MainDashboard(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf("Home") }
    val isHindi = viewModel.isHindi

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shishu-Sneh", fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, color = TextDark) },
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (isHindi) "English" else "हिंदी",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftRose
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = { BottomNavigationBar(currentTab) { currentTab = it } }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(WarmWhite)
        ) {
            when (currentTab) {
                "Home" -> HomeScreen(viewModel)
                "Growth" -> GrowthScreen(viewModel)
                "Health" -> VaccineScreen(viewModel)
                "Skills" -> MilestoneScreen(viewModel)
            }
        }
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "baby_profile_photo.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

fun saveMilestoneImageToInternalStorage(context: Context, uri: Uri, milestoneId: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "milestone_$milestoneId.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val profile by viewModel.babyProfile.collectAsState(initial = null)
    val isHindi = viewModel.isHindi
    var showEditDialog by remember { mutableStateOf(false) }

    val babyAgeMonths = profile?.let { viewModel.getBabyAgeInMonths(it.dateOfBirth) } ?: 0
    val babyTip = FeedingTips.getBabyTip(babyAgeMonths, isHindi)
    val motherTip = FeedingTips.getMotherTip(babyAgeMonths, isHindi)
    val dobFormatted = profile?.dateOfBirth?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date(it)) } ?: ""

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val permanentPath = saveImageToInternalStorage(context, it)
            if (permanentPath != null) viewModel.updateBabyImage(permanentPath)
        }
    }

    if (showEditDialog && profile != null) {
        EditProfileDialog(
            initialName = profile!!.name,
            initialDob = dobFormatted,
            initialGender = profile!!.gender,
            isHindi = isHindi,
            onDismiss = { showEditDialog = false },
            onSave = { name, dob, gender ->
                viewModel.updateBabyProfile(name, dob, gender)
                showEditDialog = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())
    ) {
        // PROFILE CARD
        Card(
            modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = BlueLight.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White).clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile?.imageUri != null) {
                            AsyncImage(model = File(profile!!.imageUri!!), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = SoftBlue)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.name ?: "Baby",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = if (isHindi) "$babyAgeMonths महीने का" else "$babyAgeMonths Months Old",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Profile", tint = TextMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoItem(label = if (isHindi) "जन्म तिथि" else "DOB", value = dobFormatted)
                    InfoItem(label = if (isHindi) "लिंग" else "Gender", value = profile?.gender ?: "-")
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // BABY TIP SECTION
        TipSection(
            title = if (isHindi) "बच्चे के लिए आज की सलाह" else "Today's Tip for Baby",
            content = babyTip,
            icon = Icons.Rounded.ChildCare,
            backgroundColor = BlueLight,
            accentColor = SoftBlue
        )

        Spacer(modifier = Modifier.height(20.dp))

        // MOTHER TIP SECTION
        TipSection(
            title = if (isHindi) "माँ के लिए आज की सलाह" else "Today's Tip for Mother",
            content = motherTip,
            icon = Icons.Rounded.Face,
            backgroundColor = RoseLight,
            accentColor = SoftRose
        )
    }
}

@Composable
fun TipSection(title: String, content: String, icon: ImageVector, backgroundColor: Color, accentColor: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = TextDark,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = content, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
fun EditProfileDialog(
    initialName: String,
    initialDob: String,
    initialGender: String,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var dob by remember { mutableStateOf(initialDob) }
    var gender by remember { mutableStateOf(initialGender) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHindi) "प्रोफ़ाइल संपादित करें" else "Edit Profile", color = TextDark) },
        text = {
            Column {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text(if (isHindi) "नाम" else "Name") },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = dob, 
                    onValueChange = { dob = it }, 
                    label = { Text(if (isHindi) "जन्म तिथि (DD/MM/YYYY)" else "DOB (DD/MM/YYYY)") },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = gender, 
                    onValueChange = { gender = it }, 
                    label = { Text(if (isHindi) "लिंग" else "Gender") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, dob, gender) },
                colors = ButtonDefaults.buttonColors(containerColor = SoftRose)
            ) {
                Text(if (isHindi) "सहेजें" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel", color = TextMedium)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun VaccineScreen(viewModel: MainViewModel) {
    val vaccines by viewModel.vaccines.collectAsState(initial = emptyList())
    val isHindi = viewModel.isHindi
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Text(
                if (isHindi) "टीकाकरण मार्गदर्शिका" else "Immunization Guide", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        items(vaccines) { v ->
            val timeStr = if (v.dueDateMillis > 0) {
                dateFormat.format(java.util.Date(v.dueDateMillis))
            } else {
                VaccineTranslations.getTranslatedTimeframe(v.scheduledTimeframe, isHindi)
            }
            
            val displayReminer = if (v.dueDateMillis > 0) {
                if (isHindi) "$timeStr से पहले" else "Before $timeStr"
            } else {
                timeStr
            }

            val cardColor = if (v.isCompleted) SageLight else BlueLight
            val accentColor = if (v.isCompleted) SoftSage else SoftBlue

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (v.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Shield, 
                                contentDescription = null, 
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text(
                            text = VaccineTranslations.getTranslatedName(v.diseaseName, isHindi),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDark
                        )
                        val translatedDescription = VaccineTranslations.getTranslatedDescription(v.diseaseName, isHindi)
                        if (translatedDescription.isNotEmpty()) {
                            Text(
                                text = translatedDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMedium
                            )
                        }
                        Text(
                            text = displayReminer,
                            color = if (v.isCompleted) TextMedium else accentColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Checkbox(
                        checked = v.isCompleted, 
                        onCheckedChange = { viewModel.toggleVaccine(v) },
                        colors = CheckboxDefaults.colors(checkedColor = SoftSage, uncheckedColor = TextMedium)
                    )
                }
            }
        }
    }
}

@Composable
fun GrowthScreen(viewModel: MainViewModel) {
    val records by viewModel.growthRecords.collectAsState(initial = emptyList())
    val isHindi = viewModel.isHindi
    val daysUntilNext by viewModel.daysUntilNextGrowth.collectAsState()
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var showAnalysis by remember { mutableStateOf(false) }

    if (showAnalysis) {
        GrowthAnalysisDialog(viewModel) { showAnalysis = false }
    }

    Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isHindi) "विकास यात्रा" else "Growth Journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Button(
                onClick = { showAnalysis = true },
                colors = ButtonDefaults.buttonColors(containerColor = SoftRose),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.AutoGraph, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isHindi) "विश्लेषण" else "Analyze", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Logging Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (daysUntilNext > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Rounded.LockClock, contentDescription = null, tint = SoftBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isHindi) "अगला अपडेट $daysUntilNext दिनों में" else "Next update in $daysUntilNext days",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        if (isHindi) "आज का रिकॉर्ड दर्ज करें" else "Log Today's Growth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text(if (isHindi) "वजन (kg)" else "Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftBlue)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text(if (isHindi) "ऊंचाई (cm)" else "Height (cm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftBlue)
                        )
                    }
                    Button(
                        onClick = {
                            if (weight.isNotBlank() && height.isNotBlank()) {
                                viewModel.addGrowth(weight, height)
                                weight = ""
                                height = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBlue)
                    ) {
                        Text(if (isHindi) "प्रगति सहेजें" else "Save Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Growth Chart Card
        Text(
            if (isHindi) "विकास रुझान" else "Growth Trends",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                StyledLineChart(
                    entries1 = records.mapIndexed { i, r -> Entry(i.toFloat(), r.weightKg) },
                    label1 = if (isHindi) "वजन (kg)" else "Weight (kg)",
                    color1 = "#64B5F6",
                    entries2 = records.mapIndexed { i, r -> Entry(i.toFloat(), r.heightCm) },
                    label2 = if (isHindi) "ऊंचाई (cm)" else "Height (cm)",
                    color2 = "#F48FB1"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Past Records List
        Text(
            if (isHindi) "पिछले रिकॉर्ड" else "Past Records",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(12.dp))
        records.reversed().forEach { record ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = dateFormat.format(java.util.Date(record.date)),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.MonitorWeight, contentDescription = null, modifier = Modifier.size(20.dp), tint = SoftBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${record.weightKg} kg",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Height, contentDescription = null, modifier = Modifier.size(20.dp), tint = SoftRose)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${record.heightCm} cm",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StyledLineChart(
    entries1: List<Entry>,
    label1: String,
    color1: String,
    entries2: List<Entry>? = null,
    label2: String? = null,
    color2: String? = null,
    drawFilled: Boolean = true
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "" }
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.textColor = android.graphics.Color.GRAY
                
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = android.graphics.Color.parseColor("#EEEEEE")
                axisLeft.textColor = android.graphics.Color.GRAY
                
                axisRight.isEnabled = false
                legend.isEnabled = true
                legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                
                setTouchEnabled(true)
                setPinchZoom(true)
                setScaleEnabled(true)
                setExtraOffsets(10f, 10f, 10f, 10f)
            }
        },
        update = { chart ->
            val dataSets = mutableListOf<LineDataSet>()
            
            val set1 = LineDataSet(entries1, label1).apply {
                color = android.graphics.Color.parseColor(color1)
                setCircleColor(android.graphics.Color.parseColor(color1))
                lineWidth = 3f
                circleRadius = 5f
                setDrawCircleHole(true)
                circleHoleColor = android.graphics.Color.WHITE
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawValues(false)
                if (drawFilled) {
                    setDrawFilled(true)
                    fillAlpha = 40
                    fillColor = android.graphics.Color.parseColor(color1)
                }
            }
            dataSets.add(set1)
            
            if (entries2 != null && label2 != null && color2 != null) {
                val set2 = LineDataSet(entries2, label2).apply {
                    color = android.graphics.Color.parseColor(color2)
                    setCircleColor(android.graphics.Color.parseColor(color2))
                    lineWidth = 3f
                    circleRadius = 5f
                    setDrawCircleHole(true)
                    circleHoleColor = android.graphics.Color.WHITE
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                    if (drawFilled) {
                        setDrawFilled(true)
                        fillAlpha = 40
                        fillColor = android.graphics.Color.parseColor(color2)
                    }
                }
                dataSets.add(set2)
            }

            chart.data = LineData(dataSets as List<ILineDataSet>)
            chart.animateX(800)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun GrowthAnalysisDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val profile by viewModel.babyProfile.collectAsState()
    val records by viewModel.growthRecords.collectAsState()
    val isHindi = viewModel.isHindi
    
    if (profile == null) return

    val ageMonths = viewModel.getBabyAgeInMonths(profile!!.dateOfBirth).coerceIn(0, 12)
    val latestRecord = records.lastOrNull()
    val average = GrowthData.getAverageForMonth(ageMonths, profile!!.gender)
    val dob = profile!!.dateOfBirth
    
    val analysis = if (latestRecord != null) {
        GrowthData.analyze(latestRecord.weightKg, latestRecord.heightCm, average, isHindi)
    } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoGraph, contentDescription = null, tint = SoftRose, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isHindi) "स्मार्ट विकास विश्लेषण" else "Smart Growth Analysis",
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                    fontSize = 22.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (latestRecord != null) {
                    // Age Badge
                    Surface(
                        color = BlueLight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Text(
                            if (isHindi) "आयु: $ageMonths महीने" else "Current Age: $ageMonths Months",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftBlue
                        )
                    }
                    
                    // Comparison Stats
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ComparisonCard(
                            label = if (isHindi) "वजन (kg)" else "Weight (kg)",
                            actual = latestRecord.weightKg,
                            avg = average.weight,
                            color = SoftBlue,
                            modifier = Modifier.weight(1f)
                        )
                        ComparisonCard(
                            label = if (isHindi) "ऊंचाई (cm)" else "Height (cm)",
                            actual = latestRecord.heightCm,
                            avg = average.height,
                            color = SoftRose,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (analysis != null) {
                        Spacer(modifier = Modifier.height(28.dp))
                        // Status Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SageLight.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, SoftSage.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Stars, contentDescription = null, tint = SoftSage, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${if (isHindi) "स्थिति:" else "Status:"} ${analysis.weightStatus}",
                                        fontWeight = FontWeight.Black,
                                        color = TextDark,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = analysis.suggestion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextDark,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(if (isHindi) "विश्लेषण के लिए कोई डेटा नहीं है।" else "No data available yet. Start logging growth!", textAlign = TextAlign.Center, color = TextMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(36.dp))
                
                // Detailed Charts Section
                SectionHeader(if (isHindi) "वजन विकास मार्ग" else "Weight Growth Path")
                Spacer(modifier = Modifier.height(16.dp))
                ChartContainer {
                    val averages = if (profile!!.gender.lowercase().contains("girl") || profile!!.gender.contains("स्त्री") || profile!!.gender.contains("लड़की")) GrowthData.girlAverages else GrowthData.boyAverages
                    StyledLineChart(
                        entries1 = averages.map { Entry(it.month.toFloat(), it.weight) },
                        label1 = if (isHindi) "स्वस्थ औसत" else "Healthy Avg",
                        color1 = "#BDBDBD",
                        entries2 = records.map { r -> Entry((r.date - dob).toFloat() / (1000f * 60 * 60 * 24 * 30.44f), r.weightKg) }.sortedBy { it.x },
                        label2 = if (isHindi) "आपका बच्चा" else "Your Baby",
                        color2 = "#64B5F6",
                        drawFilled = false
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                SectionHeader(if (isHindi) "ऊंचाई विकास मार्ग" else "Height Growth Path")
                Spacer(modifier = Modifier.height(16.dp))
                ChartContainer {
                    val averages = if (profile!!.gender.lowercase().contains("girl") || profile!!.gender.contains("स्त्री") || profile!!.gender.contains("लड़की")) GrowthData.girlAverages else GrowthData.boyAverages
                    StyledLineChart(
                        entries1 = averages.map { Entry(it.month.toFloat(), it.height) },
                        label1 = if (isHindi) "स्वस्थ औसत" else "Healthy Avg",
                        color1 = "#BDBDBD",
                        entries2 = records.map { r -> Entry((r.date - dob).toFloat() / (1000f * 60 * 60 * 24 * 30.44f), r.heightCm) }.sortedBy { it.x },
                        label2 = if (isHindi) "आपका बच्चा" else "Your Baby",
                        color2 = "#F48FB1",
                        drawFilled = false
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SoftBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(if (isHindi) "ठीक है, धन्यवाद" else "Got it, Thanks!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(32.dp)
    )
}

@Composable
fun ChartContainer(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = TextMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun ComparisonCard(label: String, actual: Float, avg: Float, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$actual", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = color.copy(alpha = 0.1f), 
                shape = RoundedCornerShape(8.dp)
            ) {
                val diff = actual - avg
                val diffStr = if (diff >= 0) "+${String.format("%.1f", diff)}" else String.format("%.1f", diff)
                Text(
                    "$diffStr vs Avg",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall, 
                    color = color,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun MilestoneScreen(viewModel: MainViewModel) {
    val milestones by viewModel.filteredMilestones.collectAsState()
    val isHindi = viewModel.isHindi
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var selectedMilestoneForPhoto by remember { mutableStateOf<Milestone?>(null) }
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedMilestoneForPhoto?.let { milestone ->
                val path = saveMilestoneImageToInternalStorage(context, it, milestone.id)
                if (path != null) {
                    viewModel.updateMilestoneImage(milestone, path)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Text(
                if (isHindi) "विकास के मील के पत्थर" else "Skill Milestones",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                if (isHindi) "आपके बच्चे की उम्र के अनुसार चुनी गई सलाह" else "Personalized for your baby's age",
                style = MaterialTheme.typography.bodySmall,
                color = TextMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        items(milestones) { m ->
            val icon = getMilestoneIcon(m.id)
            val backgroundColor = if (m.isCompleted) SageLight else AmberLight
            val accentColor = if (m.isCompleted) SoftSage else SoftAmber

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                            Text(m.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TextDark)
                            Text(text = "${m.age}: ${m.description}", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                        }
                        
                        Checkbox(
                            checked = m.isCompleted, 
                            onCheckedChange = { viewModel.toggleMilestone(m) },
                            colors = CheckboxDefaults.colors(checkedColor = SoftSage, uncheckedColor = TextMedium)
                        )
                    }
                    
                    if (m.isCompleted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(if (m.imageUri != null) 200.dp else 60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .clickable {
                                    selectedMilestoneForPhoto = m
                                    photoLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (m.imageUri != null) {
                                AsyncImage(
                                    model = File(m.imageUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        Icons.Rounded.AddAPhoto,
                                        contentDescription = null,
                                        modifier = Modifier.padding(8.dp),
                                        tint = Color.White
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AddAPhoto, contentDescription = null, tint = accentColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (isHindi) "फोटो जोड़ें" else "Add Memory Photo",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getMilestoneIcon(id: String): ImageVector {
    return when {
        id.contains("smile") -> Icons.Rounded.SentimentSatisfiedAlt
        id.contains("coo") || id.contains("babble") || id.contains("words") -> Icons.Rounded.ChatBubble
        id.contains("head") -> Icons.Rounded.ChildCare
        id.contains("roll") -> Icons.Rounded.Sync
        id.contains("sit") -> Icons.Rounded.Chair
        id.contains("crawl") -> Icons.Rounded.Pets
        id.contains("stand") || id.contains("walk") || id.contains("run") -> Icons.Rounded.DirectionsRun
        id.contains("bye") -> Icons.Rounded.WavingHand
        id.contains("points") -> Icons.Rounded.TouchApp
        id.contains("kick") -> Icons.Rounded.SportsSoccer
        id.contains("climb") -> Icons.Rounded.Terrain
        id.contains("sentences") || id.contains("stories") -> Icons.Rounded.AutoStories
        else -> Icons.Rounded.Stars
    }
}

@Composable
fun BottomNavigationBar(active: String, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val items = listOf(
            Triple("Home", Icons.Rounded.Home, BlueLight),
            Triple("Growth", Icons.Rounded.AutoGraph, RoseLight),
            Triple("Health", Icons.Rounded.HealthAndSafety, BlueLight),
            Triple("Skills", Icons.Rounded.Star, AmberLight)
        )
        items.forEach { (name, icon, _) ->
            NavigationBarItem(
                selected = active == name, 
                onClick = { onSelect(name) }, 
                icon = { Icon(icon, null) }, 
                label = { Text(name) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SoftBlue,
                    selectedTextColor = SoftBlue,
                    indicatorColor = BlueLight
                )
            )
        }
    }
}
