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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("MagicNumber")
@Composable
internal fun DateRangePickerSheet(
    fromMillis: Long?,
    toMillis: Long?,
    onDismiss: () -> Unit,
    onApply: (from: Long?, to: Long?) -> Unit,
) {
    val today = remember { Calendar.getInstance() }
    var viewYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var viewMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }

    var draftFrom by remember { mutableStateOf(fromMillis?.let { dayStart(it) }) }
    var draftTo by remember { mutableStateOf(toMillis?.let { dayStart(it) }) }
    var selecting by remember { mutableStateOf(if (fromMillis == null) "from" else "to") }

    val dateDisplayFmt = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val monthNameFmt = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    val monthLabel = remember(viewYear, viewMonth) {
        Calendar.getInstance().apply { set(viewYear, viewMonth, 1) }
            .let { monthNameFmt.format(it.time).uppercase(Locale.getDefault()) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                // Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Date Range",
                        fontSize = 16.sp,
                        fontWeight = FontWeight(600),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "×",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(4.dp),
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp),
                ) {
                    // FROM / TO cells
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        listOf("from" to draftFrom, "to" to draftTo).forEach { (key, millis) ->
                            val isActive = selecting == key
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(
                                        1.dp,
                                        if (isActive) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(12.dp),
                                    )
                                    .clickable { selecting = key }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text = key.uppercase(Locale.getDefault()),
                                    fontSize = 10.sp,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = millis?.let { dateDisplayFmt.format(Date(it)) } ?: "Select",
                                    fontSize = 13.sp,
                                    fontWeight = if (millis != null) FontWeight(600) else FontWeight(400),
                                    color = if (millis != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Month navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    if (viewMonth == 0) { viewMonth = 11; viewYear-- }
                                    else viewMonth--
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                        Text(
                            text = monthLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight(600),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "›",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    if (viewMonth == 11) { viewMonth = 0; viewYear++ }
                                    else viewMonth++
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Day-of-week header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { label ->
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Calendar grid
                    val cells = remember(viewYear, viewMonth) { buildCalendarCells(viewYear, viewMonth) }
                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { cellMillis ->
                                if (cellMillis == null) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val isFrom = draftFrom != null && sameDay(cellMillis, draftFrom!!)
                                    val isTo = draftTo != null && sameDay(cellMillis, draftTo!!)
                                    val inRange = draftFrom != null && draftTo != null &&
                                        cellMillis > draftFrom!! && cellMillis < draftTo!!
                                    val isEndpoint = isFrom || isTo
                                    val rangeAlpha = 0.08f
                                    val cal = Calendar.getInstance().apply { timeInMillis = cellMillis }
                                    val dayNum = cal.get(Calendar.DAY_OF_MONTH)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clickable {
                                                handleDayClick(
                                                    clickedMillis = cellMillis,
                                                    selecting = selecting,
                                                    draftFrom = draftFrom,
                                                    onResult = { newFrom, newTo, nextSelecting ->
                                                        draftFrom = newFrom
                                                        draftTo = newTo
                                                        selecting = nextSelecting
                                                    },
                                                )
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        // Range fill background
                                        if (inRange) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = rangeAlpha)),
                                            )
                                        }
                                        // Half-fill connectors for endpoints
                                        if (isFrom && draftTo != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(0.5f)
                                                    .align(Alignment.CenterEnd)
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = rangeAlpha)),
                                            )
                                        }
                                        if (isTo && draftFrom != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(0.5f)
                                                    .align(Alignment.CenterStart)
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = rangeAlpha)),
                                            )
                                        }
                                        // Day circle
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isEndpoint) MaterialTheme.colorScheme.onSurface
                                                    else Color.Transparent,
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = dayNum.toString(),
                                                fontSize = 13.sp,
                                                fontWeight = if (isEndpoint) FontWeight(700) else FontWeight(400),
                                                color = when {
                                                    isEndpoint -> MaterialTheme.colorScheme.background
                                                    inRange -> MaterialTheme.colorScheme.onSurface
                                                    else -> MaterialTheme.colorScheme.outline
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Status hint
                    Text(
                        text = when {
                            draftFrom == null -> "Select start date"
                            draftTo == null -> "Select end date"
                            else -> "Range selected"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                    )

                    Spacer(Modifier.height(20.dp))
                }

                // Footer
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { draftFrom = null; draftTo = null; selecting = "from" }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "Clear", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (draftFrom != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outlineVariant,
                            )
                            .clickable(enabled = draftFrom != null) { onApply(draftFrom, draftTo) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Apply",
                            fontSize = 13.sp,
                            fontWeight = FontWeight(700),
                            color = if (draftFrom != null) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

private fun handleDayClick(
    clickedMillis: Long,
    selecting: String,
    draftFrom: Long?,
    onResult: (from: Long?, to: Long?, nextSelecting: String) -> Unit,
) {
    if (selecting == "from") {
        onResult(clickedMillis, null, "to")
    } else {
        if (draftFrom != null && clickedMillis < draftFrom) {
            onResult(clickedMillis, null, "to")
        } else {
            onResult(draftFrom, clickedMillis, "from")
        }
    }
}

@Suppress("MagicNumber")
@Composable
internal fun SingleDatePickerSheet(
    initialMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val today = remember { Calendar.getInstance() }
    val initCal = remember(initialMillis) {
        (initialMillis?.let { Calendar.getInstance().apply { timeInMillis = it } } ?: today)
    }
    var viewYear by remember { mutableIntStateOf(initCal.get(Calendar.YEAR)) }
    var viewMonth by remember { mutableIntStateOf(initCal.get(Calendar.MONTH)) }
    var selectedMillis by remember { mutableStateOf(initialMillis?.let { dayStart(it) }) }

    val monthNameFmt = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthLabel = remember(viewYear, viewMonth) {
        Calendar.getInstance().apply { set(viewYear, viewMonth, 1) }
            .let { monthNameFmt.format(it.time).uppercase(Locale.getDefault()) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Select Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight(600),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "×",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.clickable { onDismiss() }.padding(4.dp),
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    if (viewMonth == 0) { viewMonth = 11; viewYear-- } else viewMonth--
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                        Text(
                            text = monthLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight(600),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "›",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    if (viewMonth == 11) { viewMonth = 0; viewYear++ } else viewMonth++
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { label ->
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    val cells = remember(viewYear, viewMonth) { buildCalendarCells(viewYear, viewMonth) }
                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { cellMillis ->
                                if (cellMillis == null) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val isSelected = selectedMillis != null && sameDay(cellMillis, selectedMillis!!)
                                    val cal = Calendar.getInstance().apply { timeInMillis = cellMillis }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clickable { selectedMillis = cellMillis },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onSurface
                                                    else Color.Transparent,
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = cal.get(Calendar.DAY_OF_MONTH).toString(),
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight(700) else FontWeight(400),
                                                color = if (isSelected) MaterialTheme.colorScheme.background
                                                else MaterialTheme.colorScheme.outline,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "Cancel", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedMillis != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outlineVariant,
                            )
                            .clickable(enabled = selectedMillis != null) { onConfirm(selectedMillis!!) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Confirm",
                            fontSize = 13.sp,
                            fontWeight = FontWeight(700),
                            color = if (selectedMillis != null) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

private fun sameDay(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
        ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}

private fun dayStart(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

@Suppress("MagicNumber")
private fun buildCalendarCells(year: Int, month: Int): List<Long?> {
    val cal = Calendar.getInstance().apply { set(year, month, 1) }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val cells = mutableListOf<Long?>()
    repeat(firstDayOfWeek) { cells.add(null) }
    for (day in 1..daysInMonth) {
        val dayCal = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cells.add(dayCal.timeInMillis)
    }
    while (cells.size % 7 != 0) cells.add(null)
    return cells
}
