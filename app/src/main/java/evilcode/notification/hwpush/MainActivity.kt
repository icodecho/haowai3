package evilcode.notification.hwpush

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.huawei.hms.push.HmsInstanceId
import evilcode.notification.hwpush.databinding.ActivityMainBinding
import evilcode.notification.hwpush.service.HwPushService
import evilcode.notification.hwpush.util.LogManager
import evilcode.notification.hwpush.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tokenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                HwPushService.ACTION_TOKEN_UPDATE -> {
                    val token = intent.getStringExtra(HwPushService.EXTRA_TOKEN)
                    if (!token.isNullOrEmpty()) {
                        updateTokenDisplay(token)
                        Toast.makeText(this@MainActivity, R.string.token_get_success, Toast.LENGTH_SHORT).show()
                    }
                }
                HwPushService.ACTION_MESSAGE_RECEIVED -> {
                    LogManager.i("MainActivity", "Message received broadcast")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        TokenManager.init(this)
        
        setupViews()
        registerReceivers()
        loadToken()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(tokenReceiver)
    }

    private fun setupViews() {
        binding.btnGetToken.setOnClickListener {
            getToken()
        }

        binding.btnDeleteToken.setOnClickListener {
            deleteToken()
        }

        binding.btnViewMessages.setOnClickListener {
            startActivity(Intent(this, MessageListActivity::class.java))
        }

        binding.btnViewLogs.setOnClickListener {
            startActivity(Intent(this, LogListActivity::class.java))
        }
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(HwPushService.ACTION_TOKEN_UPDATE)
            addAction(HwPushService.ACTION_MESSAGE_RECEIVED)
        }
        registerReceiver(tokenReceiver, filter)
    }

    private fun loadToken() {
        val token = TokenManager.getToken()
        if (!token.isNullOrEmpty()) {
            updateTokenDisplay(token)
        }
    }

    private fun getToken() {
        lifecycleScope.launch {
            try {
                val appId = getStringFromAgConnect()
                val token = withContext(Dispatchers.IO) {
                    HmsInstanceId.getInstance(this@MainActivity).getToken(appId, "HCM")
                }
                LogManager.i("MainActivity", "getToken success")
                if (!token.isNullOrEmpty()) {
                    TokenManager.saveToken(token)
                    updateTokenDisplay(token)
                    Toast.makeText(this@MainActivity, R.string.token_get_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, R.string.token_get_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                LogManager.e("MainActivity", "getToken failed: ${e.message}")
                Toast.makeText(this@MainActivity, "${getString(R.string.token_get_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteToken() {
        lifecycleScope.launch {
            try {
                val appId = getStringFromAgConnect()
                withContext(Dispatchers.IO) {
                    HmsInstanceId.getInstance(this@MainActivity).deleteToken(appId, "HCM")
                }
                TokenManager.clearToken()
                updateTokenDisplay(null)
                LogManager.i("MainActivity", "deleteToken success")
                Toast.makeText(this@MainActivity, R.string.token_delete_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                LogManager.e("MainActivity", "deleteToken failed: ${e.message}")
                Toast.makeText(this@MainActivity, "${getString(R.string.token_delete_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateTokenDisplay(token: String?) {
        if (token.isNullOrEmpty()) {
            binding.tvToken.setText(R.string.token_not_obtained)
            binding.tvToken.setTextColor(getColor(R.color.text_hint))
        } else {
            binding.tvToken.text = token
            binding.tvToken.setTextColor(getColor(R.color.success))
        }
    }

    private fun getStringFromAgConnect(): String {
        try {
            val clazz = Class.forName("com.huawei.agconnect.core.AGConnectServicesConfig")
            val method = clazz.getMethod("fromContext", Context::class.java)
            val config = method.invoke(null, this)
            val getAppIdMethod = config.javaClass.getMethod("getString", String::class.java)
            return getAppIdMethod.invoke(config, "client/app_id") as String
        } catch (e: Exception) {
            LogManager.e("MainActivity", "Failed to get app_id from agconnect: ${e.message}")
            return ""
        }
    }
}
