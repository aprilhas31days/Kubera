package org.singhak.kubera.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class DatePickerMode { DAY, YEAR }

@Suppress("MagicNumber")
@Composable
internal fun DatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val today = remember { Calendar.getInstance() }
    var mode by remember { mutableStateOf(DatePickerMode.DAY) }
    var viewYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var viewMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    var yearPageStart by remember { mutableIntStateOf(today.get(Calendar.YEAR) - 5) }

    // Scrim — full-screen overlay, tap to dismiss
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Dialog container — consumes clicks so scrim doesn't dismiss
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header: nav arrows + month/year label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "<",
                        modifier = Modifier
                            .clickable {
                                if (mode == DatePickerMode.DAY) {
                                    val (y, m) = prevMonth(viewYear, viewMonth)
                                    viewYear = y; viewMonth = m
                                } else {
                                    yearPageStart -= 12
                                }
                            }
                            .padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = if (mode == DatePickerMode.DAY) {
                            "${monthName(viewMonth)} $viewYear"
                        } else {
                            "SELECT YEAR"
                        },
                        modifier = Modifier.clickable {
                            mode = if (mode == DatePickerMode.DAY) DatePickerMode.YEAR
                            else DatePickerMode.DAY
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = ">",
                        modifier = Modifier
                            .clickable {
                                if (mode == DatePickerMode.DAY) {
                                    val (y, m) = nextMonth(viewYear, viewMonth)
                                    viewYear = y; viewMonth = m
                                } else {
                                    yearPageStart += 12
                                }
                            }
                            .padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (mode == DatePickerMode.DAY) {
                    DayGrid(
                        viewYear = viewYear,
                        viewMonth = viewMonth,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        selectedDay = selectedDay,
                        onDaySelected = { day ->
                            selectedDay = day
                            selectedYear = viewYear
                            selectedMonth = viewMonth
                        },
                    )
                } else {
                    YearGrid(
                        yearPageStart = yearPageStart,
                        selectedYear = selectedYear,
                        onYearSelected = { year ->
                            selectedYear = year
                            viewYear = year
                            mode = DatePickerMode.DAY
                        },
                    )
                }
            }

            // Footer buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "CANCEL",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYear)
                                set(Calendar.MONTH, selectedMonth - 1)
                                set(Calendar.DAY_OF_MONTH, selectedDay)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onConfirm(cal.timeInMillis)
                        }
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "CONFIRM SELECTION",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayGrid(
    viewYear: Int,
    viewMonth: Int,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
) {
    // Day-of-week header
    Row(modifier = Modifier.fillMaxWidth()) {
        for (label in listOf("S", "M", "T", "W", "T", "F", "S")) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    // Calendar grid (42 cells = 6 rows × 7 cols)
    val calDays = remember(viewYear, viewMonth) { getCalendarDays(viewYear, viewMonth) }
    calDays.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { (day, isCurrentMonth) ->
                val isSelected = isCurrentMonth &&
                    day == selectedDay &&
                    viewYear == selectedYear &&
                    viewMonth == selectedMonth
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent,
                        )
                        .clickable(enabled = isCurrentMonth) { onDaySelected(day) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                    )
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun YearGrid(
    yearPageStart: Int,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
) {
    val years = (yearPageStart until yearPageStart + 12).toList()
    years.chunked(3).forEach { yearRow ->
        Row(modifier = Modifier.fillMaxWidth()) {
            yearRow.forEach { year ->
                val isSelected = year == selectedYear
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(16f / 9f)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent,
                        )
                        .clickable { onYearSelected(year) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Suppress("MagicNumber")
private fun getCalendarDays(year: Int, month: Int): List<Pair<Int, Boolean>> {
    val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val prevCal = Calendar.getInstance().apply { set(year, month - 2, 1) }
    val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val days = mutableListOf<Pair<Int, Boolean>>()
    repeat(firstDayOfWeek) { i -> days.add(daysInPrevMonth - firstDayOfWeek + 1 + i to false) }
    for (day in 1..daysInMonth) { days.add(day to true) }
    var nextDay = 1
    while (days.size < 42) { days.add(nextDay++ to false) }
    return days
}

private fun monthName(month: Int): String =
    SimpleDateFormat("MMMM", Locale.getDefault()).format(
        Calendar.getInstance().apply { set(1970, month - 1, 1) }.time,
    ).uppercase(Locale.getDefault())

private fun prevMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 1) year - 1 to 12 else year to month - 1

private fun nextMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 12) year + 1 to 1 else year to month + 1
