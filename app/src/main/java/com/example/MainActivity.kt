package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.Appraisal
import com.example.data.model.Domain
import com.example.data.repository.DomainRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DomainViewModel
import com.example.ui.viewmodel.ValuationUiState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DomainRepository(database.domainDao(), database.appraisalDao())

        setContent {
            MyApplicationTheme {
                val viewModel: DomainViewModel = viewModel(
                    factory = DomainViewModel.Factory(repository)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DomanerApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DomanerApp(viewModel: DomainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Core states observed from ViewModel
    val domains by viewModel.filteredDomains.collectAsStateWithLifecycle()
    val appraisalHistory by viewModel.appraisalHistory.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val valuationState by viewModel.valuationState.collectAsStateWithLifecycle()

    // Dialog state controllers
    var showAddDomainDialog by remember { mutableStateOf(false) }
    var selectedDomainDetails by remember { mutableStateOf<Domain?>(null) }
    var activeHistoryAppraisal by remember { mutableStateOf<Appraisal?>(null) }
    var customDomainToEvaluate by remember { mutableStateOf("") }

    // Constants
    val categoriesList = listOf("الكل", "ذكاء اصطناعي", "مالية واستثمار", "أسماء عربية", "تطبيقات وبرمجيات", "مواقع وعقارات")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF040815),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "دومينر — Domaner",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D4B2),
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "معرض وتقييم دومينات بريميوم بالذكاء الاصطناعي",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Plus button with ripple feedback to sell/list domain
                    IconButton(
                        onClick = { showAddDomainDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2942))
                            .testTag("add_domain_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Sell Domain",
                            tint = Color(0xFF00D4B2)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: AI Appraisal Panel (لوحة التقييم بالذكاء الاصطناعي)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .testTag("appraisal_input_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF00D4B2).copy(alpha = 0.6f), Color(0xFFF59E0B).copy(alpha = 0.6f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                            Text(
                                text = "مُقيّم الدومينات الذكي (مستوحى من Atom & GoDaddy)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            )
                        }

                        Text(
                            text = "أدخل أي دومين وسيقوم نموذج Gemini بتقدير قيمته المالية، مميزاته، نقاط القوة، واقتراح مجالات مناسبة لاستخدامه.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        )

                        // Input field
                        OutlinedTextField(
                            value = customDomainToEvaluate,
                            onValueChange = { customDomainToEvaluate = it },
                            placeholder = { Text("مثال: saudi-travel.com", color = Color(0xFF64748B)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00D4B2),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF070B18),
                                unfocusedContainerColor = Color(0xFF070B18),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("appraisal_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.evaluateDomain(customDomainToEvaluate)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("appraisal_execute_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Evaluate",
                                        tint = Color.Black
                                    )
                                    Text(
                                        "احسب القيمة بالذكاء الاصطناعي",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    )
                                }
                            }

                            if (customDomainToEvaluate.isNotEmpty()) {
                                IconButton(
                                    onClick = { customDomainToEvaluate = "" },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF334155).copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Valuation API output states
                        AnimatedVisibility(
                            visible = valuationState !is ValuationUiState.Idle,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            when (val state = valuationState) {
                                is ValuationUiState.Loading -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LinearProgressIndicator(
                                            color = Color(0xFF00D4B2),
                                            trackColor = Color(0xFF1E293B),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            "جاري تحليل الدومين وتقدير المعايير التجارية...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF00D4B2)
                                            )
                                        )
                                    }
                                }

                                is ValuationUiState.Success -> {
                                    val appraisal = state.appraisal
                                    LaunchedEffect(appraisal) {
                                        customDomainToEvaluate = ""
                                    }
                                    AppraisalResultCard(
                                        appraisal = appraisal,
                                        onClose = { viewModel.clearValuationState() },
                                        onListForSale = {
                                            // Pre-fill dialog to list appraised domain easily!
                                            viewModel.listNewDomain(
                                                name = appraisal.domainName,
                                                price = appraisal.score * 50.0 + 1000.0, // estimated base on score
                                                category = "ذكاء اصطناعي",
                                                description = appraisal.marketTrends
                                            )
                                            Toast.makeText(context, "تم إدراج الدومين في المعرض بنجاح بنطاق تسعيري موازٍ!", Toast.LENGTH_LONG).show()
                                            viewModel.clearValuationState()
                                        }
                                    )
                                }

                                is ValuationUiState.Error -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF7F1D1D)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    "خطأ في التحليل المالي",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                )
                                                IconButton(
                                                    onClick = { viewModel.clearValuationState() },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                state.message,
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFECACA))
                                            )
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }

            // Section 2: Filtering Controls (أدوات البحث والفلترة والمفضلة)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search OutlinedTextField
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("ابحث عن دومين...", color = Color(0xFF64748B), fontSize = 13.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF94A3B8)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00D4B2),
                                unfocusedBorderColor = Color(0xFF1E2942),
                                focusedContainerColor = Color(0xFF131B2E),
                                unfocusedContainerColor = Color(0xFF131B2E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        )

                        // Favorites Toggle Badge
                        IconButton(
                            onClick = { viewModel.toggleFavoritesOnly() },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (showFavoritesOnly) Color(0xFFE11D48).copy(alpha = 0.2f) else Color(0xFF131B2E))
                                .border(1.dp, if (showFavoritesOnly) Color(0xFFE11D48) else Color(0xFF1E2942), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorites",
                                tint = if (showFavoritesOnly) Color(0xFFE11D48) else Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Category scroll row (الأقسام المتاحة)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoriesList) { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF00D4B2) else Color(0xFF131B2E))
                                    .clickable { viewModel.selectCategory(category) }
                                    .border(1.dp, if (isSelected) Color(0xFF00D4B2) else Color(0xFF1E2942), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Catalog header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دومينات معروضة للبيع (${domains.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "اضغط مطولاً للتفاصيل",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Section 4: 3-COLUMNS DIRECT DOMAIN GALLERY GRID (معرض الدومينات في 3 أعمدة)
            item {
                if (domains.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(32.dp))
                            Text(
                                "لا توجد دومينات مطابقة لمعايير البحث في هذا القسم.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                } else {
                    // Let's lay out the domains using multiple 3-column rows to compile perfectly inside a LazyColumn parent scroll list
                    val chunkedDomains = domains.chunked(3)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chunkedDomains.forEach { rowDomains ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (i in 0 until 3) {
                                    val domain = rowDomains.getOrNull(i)
                                    if (domain != null) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                        ) {
                                            DomainGridItem(
                                                domain = domain,
                                                onFavoriteToggle = { viewModel.toggleFavorite(domain) },
                                                onClick = { selectedDomainDetails = domain }
                                            )
                                        }
                                    } else {
                                        Box(modifier = Modifier.weight(1f)) // Empty spacer for balanced 3-col layout
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: VALUATION HISTORY SAVED LOGS (سجل التقييمات المحفوظة)
            if (appraisalHistory.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "التقييمات الأخيرة (${appraisalHistory.size})",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            )
                            TextButton(
                                onClick = { viewModel.clearAppraisalHistory() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear History", modifier = Modifier.size(14.dp))
                                    Text("حذف السجل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                appraisalHistory.forEachIndexed { index, appraisal ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { activeHistoryAppraisal = appraisal }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = appraisal.domainName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                            Text(
                                                text = appraisal.estimatedValueRange,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF00D4B2),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Score indicator
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    "${appraisal.score}/100",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFF59E0B)
                                                    )
                                                )
                                            }

                                            // Delete single appraisal
                                            IconButton(
                                                onClick = { viewModel.deleteAppraisalHistory(appraisal) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (index < appraisalHistory.size - 1) {
                                        HorizontalDivider(color = Color(0xFF1E2942), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS & OVERLAYS ---

    // 1. ADD LISTING DIALOG
    if (showAddDomainDialog) {
        AddDomainDialog(
            categories = categoriesList.filter { it != "الكل" },
            onDismiss = { showAddDomainDialog = false },
            onConfirm = { name, price, category, pitch ->
                viewModel.listNewDomain(name, price, category, pitch)
                showAddDomainDialog = false
                Toast.makeText(context, "تم إدراج الدومين الخاص بك في الكتالوج بنجاح!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // 2. DOMAIN DETAILS CARD SHEET/DIALOG
    selectedDomainDetails?.let { domain ->
        DomainDetailsDialog(
            domain = domain,
            onDismiss = { selectedDomainDetails = null },
            onEvaluateNow = {
                customDomainToEvaluate = domain.name
                scope.launch { 
                    viewModel.evaluateDomain(domain.name)
                }
                selectedDomainDetails = null
                Toast.makeText(context, "جاري إطلاق المقيّم التلقائي وتوليد التقرير المالي...", Toast.LENGTH_SHORT).show()
            },
            onDelete = {
                viewModel.deleteDomain(domain)
                selectedDomainDetails = null
                Toast.makeText(context, "تمت إزالة الدومين بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3. RETRIEVE HISTORIC VALUATION REPORT
    activeHistoryAppraisal?.let { appraisal ->
        Dialog(onDismissRequest = { activeHistoryAppraisal = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "تقرير تقييم محفوظ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        )
                        IconButton(onClick = { activeHistoryAppraisal = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "dismiss", tint = Color.White)
                        }
                    }

                    AppraisalResultCard(
                        appraisal = appraisal,
                        onClose = { activeHistoryAppraisal = null },
                        onListForSale = null // Simple historic reading
                    )
                }
            }
        }
    }
}

// Renders an individual domain inside the 3-column Catalog
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DomainGridItem(
    domain: Domain,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onClick
            )
            .testTag("domain_item_${domain.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131B2E)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (domain.isPremium) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFF1E2942)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Heart button positioned at top right corner
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (domain.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "fav",
                    tint = if (domain.isFavorite) Color(0xFFEF4444) else Color(0xFF64748B),
                    modifier = Modifier.size(15.dp)
                )
            }

            // Crown indicator for premium
            if (domain.isPremium) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "premium",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(14.dp)
                )
            }

            // main contents
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Split name or truncate
                Text(
                    text = domain.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                // Price badge
                Text(
                    text = "$${String.format("%,.0f", domain.price)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D4B2),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// Complete rich presentation sheet of the Appraisal results
@Composable
fun AppraisalResultCard(
    appraisal: Appraisal,
    onClose: () -> Unit,
    onListForSale: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B18)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF1E2942))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = appraisal.domainName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "دقة المقيِّم ومستوى التداول",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "close", tint = Color(0xFF94A3B8))
                }
            }

            HorizontalDivider(color = Color(0xFF1E2942))

            // Score and Value Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular scoring feedback
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2942)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("قوة الدومين", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text(
                            "${appraisal.score}/100",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        )
                    }
                }

                // Estimated Market range
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2942)),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("السعر المقدر تجارياً", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text(
                            appraisal.estimatedValueRange,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D4B2)
                            )
                        )
                    }
                }
            }

            // Positives
            if (appraisal.positives.isNotEmpty()) {
                Text("نقاط القوة والمزايا:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00D4B2)))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    appraisal.positives.split("\n").filter { it.isNotBlank() }.forEach { positive ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00D4B2), modifier = Modifier.size(14.dp))
                            Text(positive.trim(), color = Color(0xFFE2E8F0), fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }
                }
            }

            // Negatives
            if (appraisal.negatives.isNotEmpty()) {
                Text("نقود الضعف والمحاذير:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    appraisal.negatives.split("\n").filter { it.isNotBlank() }.forEach { negative ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp).padding(top = 2.dp))
                            Text(negative.trim(), color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }
                }
            }

            // Market fit
            if (appraisal.marketTrends.isNotEmpty()) {
                Text("تحليل ووضع السوق الخليجي:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B)))
                Text(appraisal.marketTrends, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
            }

            // Suggested uses
            if (appraisal.suggestedUses.isNotEmpty()) {
                Text("شركات ومشاريع مقترحة له:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1)))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    appraisal.suggestedUses.split("\n").filter { it.isNotBlank() }.forEach { use ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(4.dp).background(Color(0xFF00D4B2), CircleShape))
                            Text(use.trim(), color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }
            }

            // List domain directly for sale button (if callback present)
            if (onListForSale != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onListForSale,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4B2)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text("عرض هذا الدومين للبيع بالكتالوج بسعر تقديري", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// Bottom sheet dialog for showing extensive domain listing details
@Composable
fun DomainDetailsDialog(
    domain: Domain,
    onDismiss: () -> Unit,
    onEvaluateNow: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = domain.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00D4B2).copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(domain.category, fontSize = 10.sp, color = Color(0xFF00D4B2))
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color(0xFF334155))

                // Detail Items
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("السعر المطلوب لحق الملكية:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            "$${String.format("%,.0f", domain.price)} USD",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D4B2)
                            )
                        )
                    }

                    Column {
                        Text("وصف وتوافق النطاق التجاري:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            domain.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        )
                    }
                }

                // Call to actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEvaluateNow,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Text("تقرير بالذكاء الاصطناعي", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Only allow deleting if it's user listed domain or for demo
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD90429)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text("حذف العرض", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// Modal Dialog to List a domain for Sale
@Composable
fun AddDomainDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, category: String, pitch: String) -> Unit
) {
    var domainName by remember { mutableStateOf("") }
    var askingPrice by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(categories.firstOrNull() ?: "ذكاء اصطناعي") }
    var description by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "إدراج دومين للبيع بالمعرض",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D4B2)
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "dismiss", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color(0xFF334155))

                // Input Name
                Column {
                    Text("اسم الدومين كلياً:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = domainName,
                        onValueChange = { domainName = it },
                        placeholder = { Text("مثال: work.com", color = Color(0xFF64748B), fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00D4B2),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Input Price
                Column {
                    Text("السعر المطلوب (بالدولار الأمريكي USD):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = askingPrice,
                        onValueChange = { askingPrice = it },
                        placeholder = { Text("مثال: 4500", color = Color(0xFF64748B), fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00D4B2),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Category selection dropdown
                Column {
                    Text("تصنيف الفئة التجارية للفحص:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedDropdown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2942)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedCat, color = Color.White, fontSize = 12.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                            }
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.background(Color(0xFF1E2942))
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = {
                                        selectedCat = cat
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Description
                Column {
                    Text("نبذة ترويجية قصيرة عن الدومين:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("مثال: نطاق قصير لشركات إدارة المشاريع والعمل الرقمي الشامل.", color = Color(0xFF64748B), fontSize = 12.sp) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00D4B2),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val parsedPrice = askingPrice.toDoubleOrNull() ?: 1000.0
                        if (domainName.trim().isNotBlank()) {
                            onConfirm(domainName, parsedPrice, selectedCat, description)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4B2)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("عرض الآن بالمعرض", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
