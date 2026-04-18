package evilcode.notification.hwpush

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import evilcode.notification.hwpush.adapter.LogAdapter
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.databinding.ActivityLogListBinding
import evilcode.notification.hwpush.util.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogListBinding
    private lateinit var adapter: LogAdapter
    private val database by lazy { HaoWaiDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupActions()
        loadLogs()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = LogAdapter()
        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        binding.rvLogs.adapter = adapter
    }

    private fun setupActions() {
        binding.btnClearAll.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clear_confirm)
                .setPositiveButton(R.string.btn_confirm) { _, _ ->
                    clearAllLogs()
                }
                .setNegativeButton(R.string.btn_cancel, null)
                .show()
        }
    }

    private fun loadLogs() {
        lifecycleScope.launch {
            val logs = withContext(Dispatchers.IO) {
                database.appLogDao().getAllLogs()
            }
            adapter.refreshData(logs)
            binding.tvEmpty.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
            LogManager.i("LogListActivity", "Loaded ${logs.size} logs")
        }
    }

    private fun clearAllLogs() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.appLogDao().deleteAllLogs()
            }
            loadLogs()
            Toast.makeText(this@LogListActivity, R.string.clear_success, Toast.LENGTH_SHORT).show()
            LogManager.i("LogListActivity", "Cleared all logs")
        }
    }
}
