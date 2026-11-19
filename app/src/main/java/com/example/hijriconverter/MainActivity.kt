package com.example.hijriconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // واجهة عربية من اليمين إلى اليسار بغض النظر عن لغة الجهاز
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ConverterScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen() {
    val today = remember { DateConverter.today() }

    // 0 = إدخال ميلادي (النتيجة هجري)، 1 = إدخال هجري (النتيجة ميلادي)
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var dayText by rememberSaveable { mutableStateOf(today.gregorian.dayOfMonth.toString()) }
    var month by rememberSaveable { mutableIntStateOf(today.gregorian.monthValue) }
    var yearText by rememberSaveable { mutableStateOf(today.gregorian.year.toString()) }

    val day = dayText.toIntOrNull()
    val year = yearText.toIntOrNull()
    val outcome: DateConverter.Outcome? = when {
        day == null || year == null -> null
        mode == 0 -> DateConverter.fromGregorian(year, month, day)
        else -> DateConverter.fromHijri(year, month, day)
    }

    fun setFields(d: Int, m: Int, y: Int) {
        dayText = d.toString()
        month = m
        yearText = y.toString()
    }

    fun fillToday(forMode: Int) {
        if (forMode == 0) {
            setFields(today.gregorian.dayOfMonth, today.gregorian.monthValue, today.gregorian.year)
        } else {
            setFields(today.hijriDay, today.hijriMonth, today.hijriYear)
        }
    }

    fun switchTo(newMode: Int) {
        if (newMode == mode) return
        val current = (outcome as? DateConverter.Outcome.Ok)?.result
        if (current != null) {
            // ننقل التاريخ المحوَّل إلى حقول التبويب الجديد
            if (newMode == 1) {
                setFields(current.hijriDay, current.hijriMonth, current.hijriYear)
            } else {
                val g = current.gregorian
                setFields(g.dayOfMonth, g.monthValue, g.year)
            }
        } else {
            fillToday(newMode)
        }
        mode = newMode
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("محوّل التاريخ", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TodayCard(today)

            TabRow(selectedTabIndex = mode) {
                Tab(selected = mode == 0, onClick = { switchTo(0) }, text = { Text("ميلادي ← هجري") })
                Tab(selected = mode == 1, onClick = { switchTo(1) }, text = { Text("هجري ← ميلادي") })
            }

            Text(
                text = if (mode == 0) "أدخل التاريخ الميلادي" else "أدخل التاريخ الهجري",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dayText,
                    onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) dayText = it },
                    label = { Text("اليوم") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                MonthPicker(
                    months = if (mode == 0) DateConverter.gregorianMonths else DateConverter.hijriMonths,
                    selected = month,
                    onSelect = { month = it },
                    modifier = Modifier.weight(1.8f)
                )
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) yearText = it },
                    label = { Text("السنة") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.3f)
                )
            }

            FilledTonalButton(onClick = { fillToday(mode) }, modifier = Modifier.fillMaxWidth()) {
                Text("استخدم تاريخ اليوم")
            }

            when (outcome) {
                null -> MessageCard("أدخل اليوم والسنة لعرض النتيجة.", isError = false)
                is DateConverter.Outcome.Error -> MessageCard(outcome.message, isError = true)
                is DateConverter.Outcome.Ok -> ResultCard(outcome.result, toHijri = mode == 0)
            }

            Text(
                text = "يعتمد التحويل على تقويم أم القرى المعتمد في المملكة العربية السعودية، " +
                    "وقد يختلف بيوم واحد عن الرؤية الشرعية للهلال في بعض الدول.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthPicker(
    months: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(months[selected - 1], maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(4.dp))
            Text("▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            months.forEachIndexed { index, name ->
                DropdownMenuItem(
                    text = { Text("${index + 1} - $name") },
                    onClick = {
                        onSelect(index + 1)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TodayCard(today: DateConverter.Result) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("اليوم", style = MaterialTheme.typography.labelMedium)
            Text(today.weekday, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("هجري: ${today.hijriText}", style = MaterialTheme.typography.bodyMedium)
            Text("ميلادي: ${today.gregorianText}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ResultCard(result: DateConverter.Result, toHijri: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("النتيجة", style = MaterialTheme.typography.labelLarge)
            Text(
                text = result.weekday,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (toHijri) result.hijriText else result.gregorianText,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "يقابل: " + if (toHijri) result.gregorianText else result.hijriText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MessageCard(message: String, isError: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
