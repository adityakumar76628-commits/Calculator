package com.example

import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen()
                }
            }
        }
    }
}

// State
data class CalcState(
    val number1: String = "",
    val number2: String = "",
    val operator: String = ""
)

// Actions
sealed class CalcAction {
    data class Number(val number: Int) : CalcAction()
    object Clear : CalcAction()
    object Delete : CalcAction()
    object Decimal : CalcAction()
    object Calculate : CalcAction()
    data class Op(val operator: String) : CalcAction()
}

// ViewModel
class CalculatorViewModel : ViewModel() {
    var state by mutableStateOf(CalcState())
        private set

    fun onAction(action: CalcAction) {
        when (action) {
            is CalcAction.Number -> enterNumber(action.number)
            is CalcAction.Decimal -> enterDecimal()
            is CalcAction.Clear -> state = CalcState()
            is CalcAction.Delete -> performDeletion()
            is CalcAction.Op -> enterOperation(action.operator)
            is CalcAction.Calculate -> performCalculation()
        }
    }

    private fun enterNumber(number: Int) {
        if (state.operator.isBlank()) {
            if (state.number1.length >= 15) return
            state = state.copy(number1 = state.number1 + number)
            return
        }
        if (state.number2.length >= 15) return
        state = state.copy(number2 = state.number2 + number)
    }

    private fun enterDecimal() {
        if (state.operator.isBlank() && !state.number1.contains(".")) {
            if (state.number1.isBlank()) {
                state = state.copy(number1 = "0.")
            } else {
                state = state.copy(number1 = state.number1 + ".")
            }
            return
        }
        if (state.operator.isNotBlank() && !state.number2.contains(".")) {
            if (state.number2.isBlank()) {
                state = state.copy(number2 = "0.")
            } else {
                state = state.copy(number2 = state.number2 + ".")
            }
        }
    }

    private fun enterOperation(operator: String) {
        if (state.number1.isNotBlank()) {
            state = state.copy(operator = operator)
        }
    }

    private fun performCalculation() {
        val number1 = state.number1.toDoubleOrNull()
        val number2 = state.number2.toDoubleOrNull()
        if (number1 != null && number2 != null) {
            val result = when (state.operator) {
                "+" -> number1 + number2
                "−" -> number1 - number2
                "×" -> number1 * number2
                "÷" -> if (number2 != 0.0) number1 / number2 else return
                else -> return
            }
            val resultString = if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString().take(15)
            }
            state = state.copy(
                number1 = resultString,
                number2 = "",
                operator = ""
            )
        }
    }

    private fun performDeletion() {
        when {
            state.number2.isNotBlank() -> state = state.copy(number2 = state.number2.dropLast(1))
            state.operator.isNotBlank() -> state = state.copy(operator = "")
            state.number1.isNotBlank() -> state = state.copy(number1 = state.number1.dropLast(1))
        }
    }
}

// UI
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val state = viewModel.state
    val buttonSpacing = 12.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        AdBanner()
        Spacer(modifier = Modifier.weight(0.1f))
        
        // Display
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            val expression = if (state.operator.isNotBlank()) {
                "${state.number1} ${state.operator} ${state.number2}"
            } else {
                state.number1.ifBlank { "0" }
            }

            Text(
                text = expression,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 72.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .animateContentSize()
            )
        }

        // Buttons
        val rowModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(bottom = buttonSpacing)
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f), // Take available space nicely
            verticalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalculatorButton("AC", Modifier.weight(2f), containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer) { viewModel.onAction(CalcAction.Clear) }
                CalculatorButton("DEL", Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) { viewModel.onAction(CalcAction.Delete) }
                CalculatorButton("÷", Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) { viewModel.onAction(CalcAction.Op("÷")) }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalculatorButton("7", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(7)) }
                CalculatorButton("8", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(8)) }
                CalculatorButton("9", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(9)) }
                CalculatorButton("×", Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) { viewModel.onAction(CalcAction.Op("×")) }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalculatorButton("4", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(4)) }
                CalculatorButton("5", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(5)) }
                CalculatorButton("6", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(6)) }
                CalculatorButton("−", Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) { viewModel.onAction(CalcAction.Op("−")) }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalculatorButton("1", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(1)) }
                CalculatorButton("2", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(2)) }
                CalculatorButton("3", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Number(3)) }
                CalculatorButton("+", Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) { viewModel.onAction(CalcAction.Op("+")) }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalculatorButton("0", Modifier.weight(2f)) { viewModel.onAction(CalcAction.Number(0)) }
                CalculatorButton(".", Modifier.weight(1f)) { viewModel.onAction(CalcAction.Decimal) }
                CalculatorButton("=", Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) { viewModel.onAction(CalcAction.Calculate) }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick() 
        },
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(32.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test banner ad unit ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
