package com.example.contentprovider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val contacts: MutableList<ContactModel>) :
    RecyclerView.Adapter<CustomAdapter.ContactViewHolder>() {

    private var onContactClickListener: OnContactClickListener? = null
    private var onPhoneClickListener: OnPhoneClickListener? = null
    private var onMailClickListener: OnMailClickListener? = null

    interface OnContactClickListener {
        fun onContactClick(contact: ContactModel, position: Int)
    }

    interface OnPhoneClickListener {
        fun onPhoneClick(contact: ContactModel, position: Int)
    }

    interface OnMailClickListener {
        fun onMailClick(contact: ContactModel, position: Int)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView = itemView.findViewById(R.id.nameTV)
        val phoneTV: TextView = itemView.findViewById(R.id.phoneTV)
        val phoneIV: ImageView = itemView.findViewById(R.id.phoneIV)
        val mailIV: ImageView = itemView.findViewById(R.id.mailIV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTV.text = contact.name
        holder.phoneTV.text = contact.phone
        holder.mailIV.setOnClickListener {
            if (onMailClickListener != null) {
                onMailClickListener?.onMailClick(contact, position)
            }
        }
        holder.phoneIV.setOnClickListener {
            if (onPhoneClickListener != null) {
                onPhoneClickListener?.onPhoneClick(contact, position)
            }
        }
        holder.itemView.setOnClickListener {
            if (onContactClickListener != null) {
                onContactClickListener?.onContactClick(contact, position)
            }
        }
    }

    fun setOnContactClickListener(onContactClickListener: OnContactClickListener) {
        this.onContactClickListener = onContactClickListener
    }

    fun setOnPhoneClickListener(onPhoneClickListener: OnPhoneClickListener) {
        this.onPhoneClickListener = onPhoneClickListener
    }

    fun setOnMailClickListener(onMailClickListener: OnMailClickListener) {
        this.onMailClickListener = onMailClickListener
    }
}