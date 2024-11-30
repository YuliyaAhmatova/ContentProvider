package com.example.contentprovider

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contentprovider.databinding.ActivityMailBinding
import com.example.contentprovider.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var contactModelList: MutableList<ContactModel>? = null
    private var searchContact: String? = null
    private var customAdapter: CustomAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbarSearch)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbarSearch.setNavigationOnClickListener {
            onBackPressed()
        }
        contactModelList = intent.getSerializableExtra("contactModelList") as ArrayList<ContactModel>?
        binding.findBTN.setOnClickListener{
            var contactModelListNew: MutableList<ContactModel>? = null
            if (binding.searchET.text.toString().trim().isEmpty()){
                Toast.makeText(applicationContext, "Введите данные для поиска", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            searchContact = binding.searchET.text.trim().toString()
            contactModelListNew = contactModelList?.filter { it.name!!.contains(searchContact!!, ignoreCase = true) || it.phone!!.contains(searchContact!!, ignoreCase = true)}?.toMutableList()

            if (contactModelListNew!!.isEmpty()){
                Toast.makeText(applicationContext, "Пользователь не найден", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            customAdapter = CustomAdapter(contactModelListNew!!)
            binding.recyclerViewSearchRV.adapter = customAdapter
            binding.recyclerViewSearchRV.layoutManager = LinearLayoutManager(this)
            binding.recyclerViewSearchRV.setHasFixedSize(true)
            binding.searchET.text.clear()
        }
    }
}