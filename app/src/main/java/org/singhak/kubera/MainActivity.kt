package org.singhak.kubera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.ui.home.EditTransactionScreen
import org.singhak.kubera.ui.home.HomeScreen
import org.singhak.kubera.ui.home.HomeViewModel
import org.singhak.kubera.ui.rules.RulesScreen
import org.singhak.kubera.ui.rules.RulesViewModel
import org.singhak.kubera.ui.theme.KuberaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var smsPermissionGranted by mutableStateOf(false)
    private var showRules by mutableStateOf(false)
    private var selectedTransaction by mutableStateOf<Transaction?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        smsPermissionGranted = permissions.values.all { it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (hasSmsPermission()) {
            smsPermissionGranted = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS
                )
            )
        }

        setContent {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val rulesViewModel: RulesViewModel = hiltViewModel()
            val monthSummary by homeViewModel.monthSummary.collectAsState()
            val transactions by homeViewModel.transactions.collectAsState()
            val categoryBreakdown by homeViewModel.categoryBreakdown.collectAsState()
            val backfillState by homeViewModel.backfillState.collectAsState()
            val userRules by rulesViewModel.userRules.collectAsState()

            KuberaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val txn = selectedTransaction
                    when {
                        txn != null -> EditTransactionScreen(
                            transaction = txn,
                            onBack = { selectedTransaction = null },
                        )
                        showRules -> RulesScreen(
                            rules = userRules,
                            onBack = { showRules = false },
                            onAddRule = { keyword, category -> rulesViewModel.addRule(keyword, category) },
                            onDeleteRule = { rule -> rulesViewModel.deleteRule(rule) },
                        )
                        else -> HomeScreen(
                            hasPermission = smsPermissionGranted,
                            monthSummary = monthSummary,
                            transactions = transactions,
                            categoryBreakdown = categoryBreakdown,
                            backfillState = backfillState,
                            onGrantAccess = { openAppSettings() },
                            onBackfillFromDate = { date -> homeViewModel.backfillFromDate(date) },
                            onManageRules = { showRules = true },
                            onTransactionClick = { selectedTransaction = it },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!smsPermissionGranted && hasSmsPermission()) {
            smsPermissionGranted = true
        }
    }

    private fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) ==
            PackageManager.PERMISSION_GRANTED

    private fun openAppSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
    }
}
