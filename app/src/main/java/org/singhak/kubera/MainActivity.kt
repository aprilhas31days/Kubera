package org.singhak.kubera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.AppBottomBar
import org.singhak.kubera.ui.AppTab
import org.singhak.kubera.ui.analysis.AnalysisScreen
import org.singhak.kubera.ui.analysis.AnalysisViewModel
import org.singhak.kubera.ui.circle.CircleScreen
import org.singhak.kubera.ui.home.AllTransactionsScreen
import org.singhak.kubera.ui.home.EditTransactionScreen
import org.singhak.kubera.ui.home.HomeScreen
import org.singhak.kubera.ui.home.HomeViewModel
import org.singhak.kubera.ui.rules.RulesScreen
import org.singhak.kubera.ui.rules.RulesViewModel
import org.singhak.kubera.ui.theme.KuberaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var smsPermissionGranted by mutableStateOf(false)
    private var selectedTab by mutableStateOf(AppTab.HOME)
    private var selectedTransaction by mutableStateOf<Transaction?>(null)
    private var showAllTransactions by mutableStateOf(false)

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
            val permissions = mutableListOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }

        setContent {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val rulesViewModel: RulesViewModel = hiltViewModel()
            val analysisViewModel: AnalysisViewModel = hiltViewModel()

            val monthSummary by homeViewModel.monthSummary.collectAsState()
            val transactions by homeViewModel.transactions.collectAsState()
            val categoryBreakdown by homeViewModel.categoryBreakdown.collectAsState()
            val backfillState by homeViewModel.backfillState.collectAsState()
            val userRules by rulesViewModel.userRules.collectAsState()
            val analysisCategoryBreakdown by analysisViewModel.categoryBreakdown.collectAsState()
            val monthlyTrend by analysisViewModel.monthlyTrend.collectAsState()
            val topMerchants by analysisViewModel.topMerchants.collectAsState()

            KuberaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val txn = selectedTransaction
                    if (txn != null) {
                        EditTransactionScreen(
                            transaction = txn,
                            onBack = { selectedTransaction = null },
                        )
                    } else if (showAllTransactions) {
                        AllTransactionsScreen(
                            onBack = { showAllTransactions = false },
                            onTransactionClick = { selectedTransaction = it },
                        )
                    } else {
                        Scaffold(
                            bottomBar = {
                                AppBottomBar(
                                    currentTab = selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                                when (selectedTab) {
                                    AppTab.HOME -> HomeScreen(
                                        hasPermission = smsPermissionGranted,
                                        monthSummary = monthSummary,
                                        transactions = transactions,
                                        categoryBreakdown = categoryBreakdown,
                                        backfillState = backfillState,
                                        onGrantAccess = { openAppSettings() },
                                        onBackfillFromDate = { date -> homeViewModel.backfillFromDate(date) },
                                        onTransactionClick = { selectedTransaction = it },
                                        onViewAll = { showAllTransactions = true },
                                        onAddTransaction = {
                                            selectedTransaction = Transaction(
                                                amount = 0.0,
                                                type = TransactionType.DEBIT,
                                                channel = TransactionChannel.UPI,
                                                timestamp = System.currentTimeMillis(),
                                                bank = Bank.INDBNK,
                                            )
                                        },
                                    )
                                    AppTab.ANALYSIS -> AnalysisScreen(
                                        categoryBreakdown = analysisCategoryBreakdown,
                                        monthlyTrend = monthlyTrend,
                                        topMerchants = topMerchants,
                                    )
                                    AppTab.CIRCLE -> CircleScreen()
                                    AppTab.RULES -> RulesScreen(
                                        rules = userRules,
                                        onAddRule = { keyword, category -> rulesViewModel.addRule(keyword, category) },
                                        onDeleteRule = { rule -> rulesViewModel.deleteRule(rule) },
                                    )
                                }
                            }
                        }
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
