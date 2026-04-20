package com.midani.wacs.utils

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.NumberParseException

/**
 * يميز إذا النص عبارة عن رقم هاتف أم اسم
 * ويوحد صيغة الأرقام (E.164)
 */
class PhoneNumberDetector {
    
    private val phoneUtil = PhoneNumberUtil.getInstance()
    
    // الدول الافتراضية - تركيا والسعودية وسوريا (يمكن تخصيصها)
    private val defaultRegions = listOf("TR", "SA", "SY", "AE", "JO", "LB", "EG", "IQ")
    
    /**
     * فحص سريع إذا النص يبدو مثل رقم هاتف
     * مش validation كامل، بس filter أولي
     */
    fun looksLikePhoneNumber(text: String): Boolean {
        if (text.isBlank()) return false
        val cleaned = text.trim()
        
        // لو بدأ بـ + عالأغلب رقم
        if (cleaned.startsWith("+")) {
            val digits = cleaned.count { it.isDigit() }
            return digits in 7..15
        }
        
        // عد الأرقام والأحرف
        val digitCount = cleaned.count { it.isDigit() }
        val letterCount = cleaned.count { it.isLetter() }
        
        // لو فيه حروف عربية أو لاتينية أكتر، هاد اسم مش رقم
        if (letterCount > 2) return false
        
        // لازم يكون فيه 7 أرقام على الأقل
        if (digitCount < 7) return false
        
        // قد يكون فيه مسافات أو - أو ( )
        val allowedChars = cleaned.all { 
            it.isDigit() || it == ' ' || it == '-' || it == '(' || it == ')' || it == '+'
        }
        
        return allowedChars
    }
    
    /**
     * يوحد الرقم لصيغة E.164 (+905551234567)
     * يجرب أكتر من region لو ما فيه كود دولة
     */
    fun normalize(rawNumber: String): String? {
        val cleaned = rawNumber.trim()
        
        // محاولة parse مباشرة إذا فيه +
        if (cleaned.startsWith("+")) {
            try {
                val parsed = phoneUtil.parse(cleaned, null)
                if (phoneUtil.isValidNumber(parsed)) {
                    return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
                }
            } catch (e: NumberParseException) {
                // جرب الطرق التانية
            }
        }
        
        // جرب مع الـ regions الافتراضية
        for (region in defaultRegions) {
            try {
                val parsed = phoneUtil.parse(cleaned, region)
                if (phoneUtil.isValidNumber(parsed)) {
                    return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
                }
            } catch (e: NumberParseException) {
                continue
            }
        }
        
        // Fallback: تنظيف يدوي
        val digitsOnly = cleaned.filter { it.isDigit() || it == '+' }
        if (digitsOnly.length in 8..15) {
            return if (digitsOnly.startsWith("+")) digitsOnly else "+$digitsOnly"
        }
        
        return null
    }
}
