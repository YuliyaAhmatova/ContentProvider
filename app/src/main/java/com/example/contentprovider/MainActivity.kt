package com.example.contentprovider

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.RawContacts
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
        setSupportActionBar(binding.toolbarMain)
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

    override fun onResume() {
        super.onResume()
        binding.addBTN.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.WRITE_CONTACTS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                permissionWriteContract.launch(Manifest.permission.WRITE_CONTACTS)
            } else {
                addContact()
                customAdapter!!.notifyDataSetChanged()
                getContact()
            }
        }
    }

    private fun addContact() {
        val newContactName = binding.newContactNameET.text.toString()
        val newContactPhone = binding.newContactPhoneET.text.toString()
        val listCPO = ArrayList<ContentProviderOperation>()

        listCPO.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        listCPO.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, newContactName)
                .build()
        )
        listCPO.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, newContactPhone)
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .build()
        )
        Toast.makeText(this, "$newContactName добавлен в список контактов", Toast.LENGTH_LONG)
            .show()
        binding.newContactNameET.text.clear()
        binding.newContactPhoneET.text.clear()
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, listCPO)
        } catch (e: Exception) {
            Log.e("Exception ", e.message!!)
        }
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

    private val permissionWriteContract = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Получен доступ к записи контактов", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "В разрешении отказано...", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.exitMenuMain -> {
                finishAffinity()
                Toast.makeText(applicationContext, "Программа завершена", Toast.LENGTH_LONG).show()
            }
            R.id.searchMenuMain-> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("contactModelList", contactModelList as ArrayList<ContactModel>)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}


