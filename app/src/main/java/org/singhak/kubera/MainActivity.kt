package org.singhak.kubera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import org.singhak.kubera.home.HomeScreen
import org.singhak.kubera.transaction.Transaction
import org.singhak.kubera.transaction.readCurrentMonthTransactions
import org.singhak.kubera.ui.theme.KuberaTheme

class MainActivity : ComponentActivity() {

    private var transactions by mutableStateOf<List<Transaction>>(emptyList())

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            transactions = readCurrentMonthTransactions(contentResolver)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            transactions = readCurrentMonthTransactions(contentResolver)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }

        setContent {
            KuberaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(transactions = transactions)
                }
            }
        }
    }
}
