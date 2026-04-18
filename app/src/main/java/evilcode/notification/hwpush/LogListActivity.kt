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
        
        LogManager.i("LogListActivity", "Activity created")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogManager.i("LogListActivity", "Activity destroyed")
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            LogManager.i("LogListActivity", "Back button clicked, finishing activity")
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = LogAdapter(this)
        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        binding.rvLogs.adapter = adapter
        LogManager.i("LogListActivity", "RecyclerView initialized")

        adapter.onSelectionChanged = { hasSelection ->
            if (hasSelection) {
                binding.actionBar.visibility = View.VISIBLE
                updateSelectedCount()
            } else {
                binding.actionBar.visibility = View.GONE
            }
        }
    }

    private fun setupActions() {
        binding.btnCopy.setOnClickListener {
            LogManager.i("LogListActivity", "Copy selected logs to clipboard")
            adapter.copySelectedToClipboard()
            exitSelectionMode()
        }

        binding.btnDelete.setOnClickListener {
            LogManager.i("LogListActivity", "Delete selected logs dialog shown")
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm)
                .setPositiveButton(R.string.btn_confirm) { _, _ ->
                    deleteSelectedLogs()
                }
                .setNegativeButton(R.string.btn_cancel, null)
                .show()
        }

        binding.btnCancel.setOnClickListener {
            LogManager.i("LogListActivity", "Cancel selection mode")
            exitSelectionMode()
        }

        binding.btnClearAll.setOnClickListener {
            LogManager.i("LogListActivity", "Clear all logs dialog shown")
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

    private fun deleteSelectedLogs() {
        val selectedList = adapter.getSelectedLogList()
        if (selectedList.isEmpty()) return

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.appLogDao().deleteLogs(selectedList)
            }
            adapter.selectedLogs.clear()
            loadLogs()
            exitSelectionMode()
            Toast.makeText(this@LogListActivity, R.string.delete_success, Toast.LENGTH_SHORT).show()
            LogManager.i("LogListActivity", "Deleted ${selectedList.size} logs")
        }
    }

    private fun exitSelectionMode() {
        adapter.selectedLogs.clear()
        adapter.notifyDataSetChanged()
        binding.actionBar.visibility = View.GONE
    }

    private fun updateSelectedCount() {
        val count = adapter.selectedLogs.size
        binding.tvSelectedCount.text = getString(R.string.selected_count, count)
    }
}
