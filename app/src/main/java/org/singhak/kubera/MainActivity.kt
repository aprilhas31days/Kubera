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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.AppBottomBar
import org.singhak.kubera.ui.AppTab
import org.singhak.kubera.ui.analysis.AnalysisScreen
import org.singhak.kubera.ui.autopay.AutopayScreen
import org.singhak.kubera.ui.circle.CircleScreen
import org.singhak.kubera.ui.home.AllTransactionsScreen
import org.singhak.kubera.ui.home.EditTransactionScreen
import org.singhak.kubera.ui.home.HomeScreen
import org.singhak.kubera.ui.home.NoPermissionScreen
import org.singhak.kubera.ui.home.TxnDetailScreen
import org.singhak.kubera.ui.settings.SettingsScreen
import org.singhak.kubera.ui.theme.KuberaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var smsPermissionGranted by mutableStateOf(false)
    private var selectedTab by mutableStateOf(AppTab.HOME)
    private var overlay by mutableStateOf<Overlay?>(null)

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
            KuberaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!smsPermissionGranted) {
                        NoPermissionScreen(onGrantAccess = { openAppSettings() })
                    } else when (val o = overlay) {
                        is Overlay.Transactions -> AllTransactionsScreen(
                            onBack = { overlay = null },
                            onTransactionClick = { overlay = Overlay.TxnDetail(it) },
                        )
                        is Overlay.TxnDetail -> TxnDetailScreen(
                            transaction = o.t,
                            onBack = { overlay = null },
                            onEdit = { overlay = Overlay.EditTxn(it) },
                        )
                        is Overlay.EditTxn -> EditTransactionScreen(
                            transaction = o.t,
                            onBack = { overlay = null },
                        )
                        is Overlay.Settings -> SettingsScreen(
                            onBack = { overlay = null },
                        )
                        null -> Scaffold(
                            bottomBar = {
                                AppBottomBar(
                                    currentTab = selectedTab,
                                    onTabSelected = { selectedTab = it },
                                    onAddTransaction = {
                                        overlay = Overlay.EditTxn(
                                            Transaction(
                                                amount = 0.0,
                                                type = TransactionType.DEBIT,
                                                channel = TransactionChannel.UPI,
                                                timestamp = System.currentTimeMillis(),
                                                bank = Bank.INDBNK,
                                            )
                                        )
                                    },
                                )
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                                when (selectedTab) {
                                    AppTab.HOME -> HomeScreen(
                                        onTransactionClick = { overlay = Overlay.TxnDetail(it) },
                                        onViewAll = { overlay = Overlay.Transactions },
                                        onAddTransaction = {
                                            overlay = Overlay.EditTxn(
                                                Transaction(
                                                    amount = 0.0,
                                                    type = TransactionType.DEBIT,
                                                    channel = TransactionChannel.UPI,
                                                    timestamp = System.currentTimeMillis(),
                                                    bank = Bank.INDBNK,
                                                )
                                            )
                                        },
                                        onOpenSettings = { overlay = Overlay.Settings },
                                    )
                                    AppTab.ANALYTICS -> AnalysisScreen()
                                    AppTab.CIRCLES -> CircleScreen(
                                        onTransactionClick = { overlay = Overlay.TxnDetail(it) },
                                    )
                                    AppTab.AUTOPAY -> AutopayScreen()
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
