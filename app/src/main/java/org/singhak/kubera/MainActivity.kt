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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.singhak.kubera.data.TransactionRepository
import org.singhak.kubera.ui.home.HomeScreen
import org.singhak.kubera.ui.home.HomeViewModel
import org.singhak.kubera.ui.theme.KuberaTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy { TransactionRepository(contentResolver) }
    private var smsPermissionGranted by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        smsPermissionGranted = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (hasSmsPermission()) {
            smsPermissionGranted = true
        } else {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }

        setContent {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(repository)
            )
            val transactions by homeViewModel.transactions.collectAsState()

            LaunchedEffect(smsPermissionGranted) {
                if (smsPermissionGranted) {
                    homeViewModel.loadTransactions()
                }
            }

            KuberaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(transactions = transactions)
                }
            }
        }
    }

    private fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED
}
