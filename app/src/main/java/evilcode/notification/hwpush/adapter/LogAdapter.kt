package evilcode.notification.hwpush.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import evilcode.notification.hwpush.R
import evilcode.notification.hwpush.databinding.ItemLogBinding
import evilcode.notification.hwpush.model.AppLog
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(
    private val logs: MutableList<AppLog> = mutableListOf()
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

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
        }
    }
}
