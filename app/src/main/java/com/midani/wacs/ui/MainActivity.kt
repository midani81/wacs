package com.midani.wacs.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.midani.wacs.R
import com.midani.wacs.databinding.ActivityMainBinding
import com.midani.wacs.service.WhatsAppAccessibilityService
import com.midani.wacs.utils.ContactsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: DetectedNumbersAdapter
    
    companion object {
        private const val CONTACTS_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        checkPermissions()
    }
    
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
    
    private fun setupRecyclerView() {
        adapter = DetectedNumbersAdapter(
            onSaveClick = { number ->
                showSaveDialog(number.id, number.phoneNumber)
            },
            onIgnoreClick = { number ->
                viewModel.ignoreNumber(number.id)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }
    
    private fun setupObservers() {
        viewModel.pendingNumbers.observe(this) { numbers ->
            adapter.submitList(numbers)
            binding.emptyState.visibility = if (numbers.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.pendingCount.observe(this) { count ->
            binding.tvPendingCount.text = getString(R.string.pending_count, count)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnEnableService.setOnClickListener {
            openAccessibilitySettings()
        }
        
        binding.btnRefresh.setOnClickListener {
            updateServiceStatus()
        }
    }
    
    private fun updateServiceStatus() {
        val isEnabled = isAccessibilityServiceEnabled()
        if (isEnabled) {
            binding.tvServiceStatus.text = getString(R.string.service_running)
            binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.green))
            binding.btnEnableService.text = getString(R.string.open_settings)
        } else {
            binding.tvServiceStatus.text = getString(R.string.service_stopped)
            binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.btnEnableService.text = getString(R.string.enable_service)
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        val serviceName = "$packageName/${WhatsAppAccessibilityService::class.java.name}"
        return enabledServices.contains(serviceName)
    }
    
    private fun openAccessibilitySettings() {
        AlertDialog.Builder(this)
            .setTitle(R.string.accessibility_guide_title)
            .setMessage(R.string.accessibility_guide_message)
            .setPositiveButton(R.string.continue_to_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
        
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                notGranted.toTypedArray(),
                CONTACTS_PERMISSION_REQUEST
            )
        }
    }
    
    private fun showSaveDialog(numberId: Long, phoneNumber: String) {
        val editText = android.widget.EditText(this).apply {
            hint = getString(R.string.contact_name_hint)
            setText("WA - ${phoneNumber.takeLast(4)}")
        }
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.save_contact))
            .setMessage(phoneNumber)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotBlank()) {
                    saveToContacts(numberId, name, phoneNumber)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun saveToContacts(numberId: Long, name: String, phoneNumber: String) {
        val success = ContactsHelper.saveContact(
            context = this,
            name = name,
            phoneNumber = phoneNumber,
            note = "Auto-detected from WhatsApp"
        )
        
        if (success) {
            viewModel.markAsSaved(numberId, name)
            Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.contact_save_failed, Toast.LENGTH_LONG).show()
        }
    }
}
