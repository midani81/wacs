package com.midani.wacs.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.midani.wacs.data.DetectedNumber
import com.midani.wacs.data.WacsDatabase
import com.midani.wacs.utils.PhoneNumberDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Accessibility Service لمراقبة واتساب واستخراج الأرقام غير المحفوظة
 * 
 * كيف يشتغل:
 * 1. يراقب events من تطبيق WhatsApp Business
 * 2. لما يفتح المستخدم قائمة المحادثات، يقرأ العناصر
 * 3. يميز بين الأسماء (محفوظة) والأرقام (غير محفوظة)
 * 4. يحفظ الأرقام الجديدة في قاعدة بيانات محلية
 * 5. المستخدم يقدر يراجعها ويحفظها في جهات الاتصال
 */
class WhatsAppAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "WACS_Service"
        
        // Package names للواتساب
        private const val WA_BUSINESS = "com.whatsapp.w4b"
        private const val WA_NORMAL = "com.whatsapp"
        
        // للحماية من تكرار نفس الرقم خلال session
        private val sessionCache = mutableSetOf<String>()
        
        @Volatile
        var isRunning = false
            private set
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: WacsDatabase
    private lateinit var phoneDetector: PhoneNumberDetector
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service onCreate")
        database = WacsDatabase.getInstance(applicationContext)
        phoneDetector = PhoneNumberDetector()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.i(TAG, "Service connected and running")
        
        // ضبط إعدادات الخدمة runtime
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_SCROLLED
            
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            
            packageNames = arrayOf(WA_BUSINESS, WA_NORMAL)
            notificationTimeout = 300
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        val packageName = event.packageName?.toString() ?: return
        if (packageName != WA_BUSINESS && packageName != WA_NORMAL) return
        
        // منع الإفراط في المعالجة
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }
        
        val rootNode = rootInActiveWindow ?: return
        
        try {
            scanForPhoneNumbers(rootNode, packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning nodes", e)
        }
    }
    
    /**
     * يبحث في شجرة الـ UI عن الأرقام غير المحفوظة
     * 
     * في واتساب، قائمة المحادثات عبارة عن RecyclerView
     * كل عنصر فيه عنوان (اسم/رقم) ورسالة
     * لو العنوان بدأ بـ + أو رقم، يكون جهة غير محفوظة
     */
    private fun scanForPhoneNumbers(root: AccessibilityNodeInfo, packageName: String) {
        val foundTexts = mutableListOf<String>()
        collectTitleTexts(root, foundTexts)
        
        if (foundTexts.isEmpty()) return
        
        Log.d(TAG, "Collected ${foundTexts.size} potential titles")
        
        foundTexts.forEach { text ->
            if (phoneDetector.looksLikePhoneNumber(text)) {
                val normalized = phoneDetector.normalize(text) ?: return@forEach
                
                // تجنب التكرار داخل نفس الجلسة
                if (sessionCache.contains(normalized)) return@forEach
                sessionCache.add(normalized)
                
                Log.i(TAG, "Detected new phone: $normalized (raw: $text)")
                saveDetectedNumber(normalized, text, packageName)
            }
        }
    }
    
    /**
     * يجمع النصوص اللي محتمل تكون عناوين محادثات
     * واتساب بيستخدم resource-id معينة للعناوين
     */
    private fun collectTitleTexts(
        node: AccessibilityNodeInfo?,
        result: MutableList<String>,
        depth: Int = 0
    ) {
        node ?: return
        if (depth > 30) return  // حماية من recursion عميق
        
        val viewId = node.viewIdResourceName ?: ""
        val text = node.text?.toString()?.trim() ?: ""
        
        // واتساب بيستخدم "conversations_row_contact_name" أو مشابه للاسم
        // بس كل نسخة ممكن تكون مختلفة، فلنجمع كل نص فيه رقم محتمل
        if (text.isNotBlank() && text.length <= 50) {
            // شروط العنوان المحتمل:
            // - طوله معقول (رقم موبايل أو اسم قصير)
            // - يحتوي أرقام
            if (text.any { it.isDigit() } || text.startsWith("+")) {
                // فحص إضافي: إذا الـ view id يحتوي "contact" أو "name" أو "title"
                val isLikelyTitle = viewId.contains("contact", ignoreCase = true) ||
                        viewId.contains("name", ignoreCase = true) ||
                        viewId.contains("title", ignoreCase = true) ||
                        viewId.isEmpty()  // بعض العناصر ما إلها id
                
                if (isLikelyTitle) {
                    result.add(text)
                }
            }
        }
        
        for (i in 0 until node.childCount) {
            collectTitleTexts(node.getChild(i), result, depth + 1)
        }
    }
    
    private fun saveDetectedNumber(
        normalizedNumber: String,
        rawText: String,
        sourcePackage: String
    ) {
        serviceScope.launch {
            try {
                val dao = database.detectedNumberDao()
                val existing = dao.findByNumber(normalizedNumber)
                
                if (existing == null) {
                    val detected = DetectedNumber(
                        phoneNumber = normalizedNumber,
                        rawText = rawText,
                        detectedAt = System.currentTimeMillis(),
                        sourceApp = sourcePackage,
                        isSaved = false,
                        isIgnored = false
                    )
                    dao.insert(detected)
                    Log.i(TAG, "Saved new detection: $normalizedNumber")
                } else {
                    Log.d(TAG, "Number already exists: $normalizedNumber")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving detection", e)
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        isRunning = false
        sessionCache.clear()
        Log.i(TAG, "Service destroyed")
        super.onDestroy()
    }
}
