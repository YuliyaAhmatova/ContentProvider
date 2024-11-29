package com.example.contentprovider

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.contentprovider.databinding.ActivityMailBinding
import com.example.contentprovider.databinding.ActivityMainBinding

@Suppress("CAST_NEVER_SUCCEEDS")
class MailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMailBinding

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbarMail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbarMail.setNavigationOnClickListener {
            onBackPressed()
        }

        val phoneNumber = intent.getStringExtra("number")
        var message: String? = null
        binding.numberMailTV.text = phoneNumber

        binding.sendBTN.setOnClickListener {
            if (binding.messageMailEV.text.toString().trim().isEmpty()){
                Toast.makeText(applicationContext, "Ошибка! Пустое сообщение!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            message = binding.messageMailEV.text.toString()
            try {
                val smsManager: SmsManager
                if (Build.VERSION.SDK_INT >= 23) {
                    smsManager = this.getSystemService(SmsManager::class.java)
                } else {
                    smsManager = SmsManager.getDefault()
                }
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()
                binding.messageMailEV.text.clear()
            } catch (e: Exception) {
                Toast.makeText(
                    applicationContext,
                    "Please enter all the data.." + e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}