package com.midani.wacs.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detected_numbers")
data class DetectedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,       // E.164 normalized format
    val rawText: String,            // النص كما ظهر في واتساب
    val detectedAt: Long,           // timestamp
    val sourceApp: String,          // com.whatsapp أو com.whatsapp.w4b
    val isSaved: Boolean = false,   // هل حفظناه في جهات الاتصال
    val isIgnored: Boolean = false, // المستخدم قرر يتجاهله
    val contactName: String? = null, // الاسم الذي سيُحفظ به
    val firstMessage: String? = null // أول رسالة (اختياري)
)
