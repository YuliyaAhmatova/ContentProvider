package com.example.contentprovider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contentprovider.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var customAdapter: CustomAdapter? = null
    private var contactModelList: MutableList<ContactModel>? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionContact.launch(Manifest.permission.READ_CONTACTS)
            customAdapter?.notifyDataSetChanged()
        } else {
            getContact()
        }
    }

    @SuppressLint("Range")
    private fun getContact() {
        contactModelList = ArrayList()
        val phones = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        while (phones!!.moveToNext()) {
            val name =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val contactModel = ContactModel(name, phoneNumber)
            contactModelList?.add(contactModel)
        }
        phones.close()
        customAdapter = CustomAdapter(contactModelList!!)
        binding.recyclerViewRV.adapter = customAdapter
        binding.recyclerViewRV.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRV.setHasFixedSize(true)
        customAdapter?.setOnPhoneClickListener(object :
            CustomAdapter.OnPhoneClickListener {
            override fun onPhoneClick(contact: ContactModel, position: Int) {
                val person = (contactModelList as ArrayList<ContactModel>)[position]
                val number = person.phone
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.CALL_PHONE
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    permissionOfCall.launch(Manifest.permission.CALL_PHONE)
                } else {
                    callTheNumber(number)
                }
            }
        })
        customAdapter?.setOnMailClickListener(object :
            CustomAdapter.OnMailClickListener {
            override fun onMailClick(contact: ContactModel, position: Int) {
                val person = (contactModelList as ArrayList<ContactModel>)[position]
                val number = person.phone
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.SEND_SMS
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    permissionOfMail.launch(Manifest.permission.SEND_SMS)
                } else {
                    sendMailNumber(number)
                }
            }
        })
    }

    private fun callTheNumber(number: String?) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    private fun sendMailNumber(number: String?) {
        val intent = Intent(this, MailActivity::class.java)
        intent.putExtra("number", number)
        startActivity(intent)
    }

    private val permissionContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Получен доступ к контактам", Toast.LENGTH_LONG).show()
            getContact()
        } else {
            Toast.makeText(this, "В разрешении отказано...", Toast.LENGTH_LONG).show()
        }
    }

    private val permissionOfCall = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Получен доступ к контактам", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "В разрешении отказано...", Toast.LENGTH_LONG).show()
        }
    }

    private val permissionOfMail = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Получен доступ к смс", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "В разрешении отказано...", Toast.LENGTH_LONG).show()
        }
    }
}