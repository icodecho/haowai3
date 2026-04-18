package evilcode.notification.hwpush.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import evilcode.notification.hwpush.R
import evilcode.notification.hwpush.databinding.ItemLogBinding
import evilcode.notification.hwpush.model.AppLog
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(
    private val context: Context,
    private val logs: MutableList<AppLog> = mutableListOf()
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    var selectedLogs: MutableSet<Long> = mutableSetOf()
    var onSelectionChanged: ((Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int = logs.size

    fun refreshData(newLogs: List<AppLog>) {
        logs.clear()
        logs.addAll(newLogs)
        selectedLogs.clear()
        notifyDataSetChanged()
    }

    inner class LogViewHolder(private val binding: ItemLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        fun bind(log: AppLog) {
            val levelColor = when (log.level) {
                "ERROR" -> binding.root.context.getColor(R.color.error)
                "WARN" -> binding.root.context.getColor(android.R.color.holo_orange_light)
                "INFO" -> binding.root.context.getColor(R.color.success)
                else -> binding.root.context.getColor(R.color.text_secondary)
            }

            binding.tvLevel.text = log.level
            binding.tvLevel.setTextColor(levelColor)
            binding.tvTag.text = log.tag
            binding.tvMessage.text = log.message
            binding.tvTime.text = dateFormat.format(Date(log.timestamp))
            binding.tvSelectedIndicator.visibility = if (selectedLogs.contains(log.id)) View.VISIBLE else View.GONE

            val isSelected = selectedLogs.contains(log.id)
            binding.llContainer.setBackgroundResource(if (isSelected) R.color.selected_bg else 0)

            binding.llContainer.setOnClickListener {
                onSelectionChanged?.let { callback ->
                    if (selectedLogs.isEmpty()) {
                        selectedLogs.add(log.id)
                        callback(true)
                    } else {
                        if (selectedLogs.contains(log.id)) {
                            selectedLogs.remove(log.id)
                        } else {
                            selectedLogs.add(log.id)
                        }
                        if (selectedLogs.isEmpty()) {
                            callback(false)
                        }
                    }
                    notifyItemChanged(adapterPosition)
                }
            }

            binding.llContainer.setOnLongClickListener {
                if (!selectedLogs.contains(log.id)) {
                    selectedLogs.add(log.id)
                    onSelectionChanged?.invoke(true)
                    notifyItemChanged(adapterPosition)
                }
                true
            }
        }
    }

    fun getSelectedLogList(): List<AppLog> {
        return logs.filter { selectedLogs.contains(it.id) }
    }

    fun copySelectedToClipboard() {
        val selectedList = getSelectedLogList()
        if (selectedList.isEmpty()) return

        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        selectedList.forEach { log ->
            sb.append("级别: ${log.level}\n")
            sb.append("标签: ${log.tag}\n")
            sb.append("内容: ${log.message}\n")
            sb.append("时间: ${dateFormat.format(Date(log.timestamp))}\n")
            sb.append("------------------------\n")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("运行日志", sb.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show()
    }
}
