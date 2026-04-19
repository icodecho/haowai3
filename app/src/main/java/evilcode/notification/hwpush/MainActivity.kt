package evilcode.notification.hwpush

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.huawei.hms.aaid.HmsInstanceId
import evilcode.notification.hwpush.databinding.ActivityMainBinding
import evilcode.notification.hwpush.service.HwPushService
import evilcode.notification.hwpush.util.LogManager
import evilcode.notification.hwpush.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var fullToken: String? = null
    private var isTokenVisible = false
    private val hideTokenHandler = Handler(Looper.getMainLooper())
    private val hideTokenRunnable = Runnable {
        isTokenVisible = false
        updateTokenDisplay()
        binding.btnShowToken.text = getString(R.string.btn_show_token)
    }

    private val tokenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                HwPushService.ACTION_TOKEN_UPDATE -> {
                    val token = intent.getStringExtra(HwPushService.EXTRA_TOKEN)
                    if (!token.isNullOrEmpty()) {
                        fullToken = token
                        updateTokenDisplay()
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
        
        LogManager.i("MainActivity", "Activity created")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(tokenReceiver)
        hideTokenHandler.removeCallbacks(hideTokenRunnable)
        LogManager.i("MainActivity", "Activity destroyed")
    }

    override fun onResume() {
        super.onResume()
        LogManager.i("MainActivity", "Activity resumed")
    }

    override fun onPause() {
        super.onPause()
        LogManager.i("MainActivity", "Activity paused")
    }

    override fun onStop() {
        super.onStop()
        LogManager.i("MainActivity", "Activity stopped")
    }

    private fun setupViews() {
        binding.btnGetToken.setOnClickListener {
            LogManager.i("MainActivity", "User clicked get token button")
            getToken()
        }

        binding.btnDeleteToken.setOnClickListener {
            LogManager.i("MainActivity", "User clicked delete token button")
            deleteToken()
        }

        binding.btnShowToken.setOnClickListener {
            if (fullToken.isNullOrEmpty()) {
                Toast.makeText(this, R.string.token_not_obtained, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isTokenVisible) {
                isTokenVisible = false
                hideTokenHandler.removeCallbacks(hideTokenRunnable)
                updateTokenDisplay()
                binding.btnShowToken.text = getString(R.string.btn_show_token)
                LogManager.i("MainActivity", "Token hidden")
            } else {
                isTokenVisible = true
                updateTokenDisplay()
                binding.btnShowToken.text = "隐藏TOKEN"
                hideTokenHandler.removeCallbacks(hideTokenRunnable)
                hideTokenHandler.postDelayed(hideTokenRunnable, 10000)
                LogManager.i("MainActivity", "Token shown (will auto-hide in 10s)")
            }
        }

        binding.btnCopyToken.setOnClickListener {
            val token = fullToken
            if (token.isNullOrEmpty()) {
                Toast.makeText(this, R.string.token_not_obtained, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Push Token", token)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show()
            LogManager.i("MainActivity", "Token copied to clipboard")
        }

        binding.btnViewMessages.setOnClickListener {
            LogManager.i("MainActivity", "Navigate to message list")
            startActivity(Intent(this, MessageListActivity::class.java))
        }

        binding.btnViewLogs.setOnClickListener {
            LogManager.i("MainActivity", "Navigate to log list")
            startActivity(Intent(this, LogListActivity::class.java))
        }
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(HwPushService.ACTION_TOKEN_UPDATE)
            addAction(HwPushService.ACTION_MESSAGE_RECEIVED)
        }
        registerReceiver(tokenReceiver, filter)
        LogManager.i("MainActivity", "Broadcast receivers registered")
    }

    private fun loadToken() {
        val token = TokenManager.getToken()
        if (!token.isNullOrEmpty()) {
            fullToken = token
            updateTokenDisplay()
            LogManager.i("MainActivity", "Cached token loaded")
        } else {
            LogManager.i("MainActivity", "No cached token found")
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
                    fullToken = token
                    TokenManager.saveToken(token)
                    updateTokenDisplay()
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
                fullToken = null
                isTokenVisible = false
                hideTokenHandler.removeCallbacks(hideTokenRunnable)
                updateTokenDisplay()
                binding.btnShowToken.text = getString(R.string.btn_show_token)
                LogManager.i("MainActivity", "deleteToken success")
                Toast.makeText(this@MainActivity, R.string.token_delete_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                LogManager.e("MainActivity", "deleteToken failed: ${e.message}")
                Toast.makeText(this@MainActivity, "${getString(R.string.token_delete_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateTokenDisplay() {
        val token = fullToken
        if (token.isNullOrEmpty()) {
            binding.tvToken.setText(R.string.token_not_obtained)
            binding.tvToken.setTextColor(getColor(R.color.text_hint))
        } else {
            if (isTokenVisible) {
                binding.tvToken.text = token
                binding.tvToken.setTextColor(getColor(R.color.success))
            } else {
                binding.tvToken.text = maskToken(token)
                binding.tvToken.setTextColor(getColor(R.color.accent))
            }
        }
    }

    private fun maskToken(token: String): String {
        if (token.length <= 16) {
            return token
        }
        val prefix = token.take(8)
        val suffix = token.takeLast(8)
        val maskedLength = token.length - 16
        val stars = "*".repeat(maskedLength)
        return "$prefix$stars$suffix"
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
