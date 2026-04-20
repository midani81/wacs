package com.midani.wacs.utils

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.util.Log

/**
 * مسؤول عن حفظ جهات الاتصال في هاتف المستخدم
 * بمجرد حفظها، Android سيسنكرنها مع Google Contacts تلقائياً
 * لو الحساب مرتبط بـ Google
 */
object ContactsHelper {
    
    private const val TAG = "WACS_Contacts"
    
    /**
     * يحفظ جهة اتصال جديدة
     * @return true إذا نجح الحفظ
     */
    fun saveContact(
        context: Context,
        name: String,
        phoneNumber: String,
        note: String? = null
    ): Boolean {
        return try {
            val ops = ArrayList<ContentProviderOperation>()
            
            // إنشاء raw contact جديد
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )
            
            // إضافة الاسم
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        name
                    )
                    .build()
            )
            
            // إضافة رقم الهاتف
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                    .build()
            )
            
            // إضافة ملاحظة (اختياري)
            if (!note.isNullOrBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, note)
                        .build()
                )
            }
            
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            Log.i(TAG, "Contact saved: $name / $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving contact", e)
            false
        }
    }
    
    /**
     * يفحص إذا الرقم موجود مسبقاً في جهات الاتصال
     */
    fun isContactSaved(context: Context, phoneNumber: String): Boolean {
        return try {
            val uri = android.net.Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(phoneNumber)
            )
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null, null, null
            )
            val exists = cursor?.use { it.count > 0 } ?: false
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Error checking contact", e)
            false
        }
    }
}
