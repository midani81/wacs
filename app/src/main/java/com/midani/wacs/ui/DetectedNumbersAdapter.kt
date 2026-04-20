package com.midani.wacs.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.midani.wacs.data.DetectedNumber
import com.midani.wacs.databinding.ItemDetectedNumberBinding

class DetectedNumbersAdapter(
    private val onSaveClick: (DetectedNumber) -> Unit,
    private val onIgnoreClick: (DetectedNumber) -> Unit
) : ListAdapter<DetectedNumber, DetectedNumbersAdapter.ViewHolder>(DIFF_CALLBACK) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetectedNumberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: ItemDetectedNumberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(number: DetectedNumber) {
            binding.tvPhoneNumber.text = number.phoneNumber
            binding.tvRawText.text = number.rawText
            binding.tvDetectedAt.text = DateUtils.getRelativeTimeSpanString(
                number.detectedAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            
            binding.btnSave.setOnClickListener { onSaveClick(number) }
            binding.btnIgnore.setOnClickListener { onIgnoreClick(number) }
        }
    }
    
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DetectedNumber>() {
            override fun areItemsTheSame(oldItem: DetectedNumber, newItem: DetectedNumber) =
                oldItem.id == newItem.id
            
            override fun areContentsTheSame(oldItem: DetectedNumber, newItem: DetectedNumber) =
                oldItem == newItem
        }
    }
}
