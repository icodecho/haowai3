package evilcode.notification.hwpush.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import evilcode.notification.hwpush.R
import evilcode.notification.hwpush.databinding.ItemMessageBinding
import evilcode.notification.hwpush.model.PushMessage
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val context: Context,
    private val messages: MutableList<PushMessage> = mutableListOf()
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    var selectedMessages: MutableSet<Long> = mutableSetOf()
    var onSelectionChanged: ((Boolean) -> Unit)? = null
    var onMessagesDeleted: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun refreshData(newMessages: List<PushMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        selectedMessages.clear()
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        fun bind(message: PushMessage) {
            binding.tvMessageTitle.text = message.title ?: "(无标题)"
            binding.tvMessageBody.text = message.body ?: message.data ?: "(无内容)"
            binding.tvMessageTime.text = dateFormat.format(Date(message.receivedTime))
            binding.tvSelectedIndicator.visibility = if (selectedMessages.contains(message.id)) View.VISIBLE else View.GONE

            val isSelected = selectedMessages.contains(message.id)
            binding.llContainer.setBackgroundResource(if (isSelected) R.color.selected_bg else 0)

            binding.llContainer.setOnClickListener {
                onSelectionChanged?.let { callback ->
                    if (selectedMessages.isEmpty()) {
                        selectedMessages.add(message.id)
                        callback(true)
                    } else {
                        if (selectedMessages.contains(message.id)) {
                            selectedMessages.remove(message.id)
                        } else {
                            selectedMessages.add(message.id)
                        }
                        if (selectedMessages.isEmpty()) {
                            callback(false)
                        }
                    }
                    notifyItemChanged(adapterPosition)
                    updateSelectedCount()
                }
            }

            binding.llContainer.setOnLongClickListener {
                if (!selectedMessages.contains(message.id)) {
                    selectedMessages.add(message.id)
                    onSelectionChanged?.invoke(true)
                    notifyItemChanged(adapterPosition)
                    updateSelectedCount()
                }
                true
            }
        }

        private fun updateSelectedCount() {
            // This will be handled by the activity
        }
    }

    fun getSelectedMessageList(): List<PushMessage> {
        return messages.filter { selectedMessages.contains(it.id) }
    }

    fun copySelectedToClipboard() {
        val selectedList = getSelectedMessageList()
        if (selectedList.isEmpty()) return

        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        selectedList.forEach { msg ->
            sb.append("标题: ${msg.title ?: "无"}\n")
            sb.append("内容: ${msg.body ?: msg.data ?: "无"}\n")
            sb.append("时间: ${dateFormat.format(Date(msg.receivedTime))}\n")
            sb.append("ID: ${msg.messageId}\n")
            sb.append("------------------------\n")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("推送消息", sb.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show()
    }

    fun showDeleteDialog(onDelete: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.delete_confirm)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                onDelete()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    fun showClearAllDialog(onClear: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.clear_confirm)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                onClear()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
}
