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
        setupSwipeRefresh()
        setupRecyclerView()
        setupActions()
        loadMessages()
        
        LogManager.i("MessageListActivity", "Activity created")
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            LogManager.i("MessageListActivity", "Back button clicked, finishing activity")
            finish()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            LogManager.i("MessageListActivity", "Swipe refresh triggered")
            loadMessages()
        }
        binding.swipeRefreshLayout.setColorSchemeColors(
            getColor(R.color.accent),
            getColor(R.color.success),
            getColor(R.color.error)
        )
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(this)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter
        LogManager.i("MessageListActivity", "RecyclerView initialized")

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
            LogManager.i("MessageListActivity", "Copy dialog shown")
            adapter.showCopyOptionsDialog()
            exitSelectionMode()
        }

        binding.btnDelete.setOnClickListener {
            LogManager.i("MessageListActivity", "Delete selected messages dialog shown")
            adapter.showDeleteDialog {
                deleteSelectedMessages()
            }
        }

        binding.btnCancel.setOnClickListener {
            LogManager.i("MessageListActivity", "Cancel selection mode")
            exitSelectionMode()
        }

        binding.btnClearAll.setOnClickListener {
            LogManager.i("MessageListActivity", "Clear all messages dialog shown")
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
            binding.swipeRefreshLayout.isRefreshing = false
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
