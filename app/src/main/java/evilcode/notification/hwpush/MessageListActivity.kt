package evilcode.notification.hwpush

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import evilcode.notification.hwpush.adapter.MessageAdapter
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.databinding.ActivityMessageListBinding
import evilcode.notification.hwpush.util.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageListBinding
    private lateinit var adapter: MessageAdapter
    private val database by lazy { HaoWaiDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupActions()
        loadMessages()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(this)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter

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
            adapter.copySelectedToClipboard()
            exitSelectionMode()
        }

        binding.btnDelete.setOnClickListener {
            adapter.showDeleteDialog {
                deleteSelectedMessages()
            }
        }

        binding.btnCancel.setOnClickListener {
            exitSelectionMode()
        }

        binding.btnClearAll.setOnClickListener {
            adapter.showClearAllDialog {
                clearAllMessages()
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            val messages = withContext(Dispatchers.IO) {
                database.pushMessageDao().getAllMessages()
            }
            adapter.refreshData(messages)
            binding.tvEmpty.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            LogManager.i("MessageListActivity", "Loaded ${messages.size} messages")
        }
    }

    private fun deleteSelectedMessages() {
        val selectedList = adapter.getSelectedMessageList()
        if (selectedList.isEmpty()) return

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.pushMessageDao().deleteMessages(selectedList)
            }
            adapter.selectedMessages.clear()
            loadMessages()
            exitSelectionMode()
            Toast.makeText(this@MessageListActivity, R.string.delete_success, Toast.LENGTH_SHORT).show()
            LogManager.i("MessageListActivity", "Deleted ${selectedList.size} messages")
        }
    }

    private fun clearAllMessages() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.pushMessageDao().deleteAllMessages()
            }
            adapter.selectedMessages.clear()
            loadMessages()
            exitSelectionMode()
            Toast.makeText(this@MessageListActivity, R.string.clear_success, Toast.LENGTH_SHORT).show()
            LogManager.i("MessageListActivity", "Cleared all messages")
        }
    }

    private fun exitSelectionMode() {
        adapter.selectedMessages.clear()
        adapter.notifyDataSetChanged()
        binding.actionBar.visibility = View.GONE
    }

    private fun updateSelectedCount() {
        val count = adapter.selectedMessages.size
        binding.tvSelectedCount.text = getString(R.string.selected_count, count)
    }
}
