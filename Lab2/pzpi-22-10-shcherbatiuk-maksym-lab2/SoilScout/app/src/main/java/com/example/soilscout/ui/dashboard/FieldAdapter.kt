package com.example.soilscout.ui.dashboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soilscout.R
import com.example.soilscout.model.Field
import com.example.soilscout.ui.createeditfield.CreateEditFieldActivity
import com.example.soilscout.ui.fielddetails.FieldDetailsActivity


class FieldAdapter(private val onEditClicked: (Int) -> Unit) : ListAdapter<Field, FieldAdapter.FieldViewHolder>(FieldDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_list_item_field, parent, false)
        return FieldViewHolder(view)
    }


    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        val field = getItem(position)

        holder.bind(field)


        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            val intent = Intent(context, FieldDetailsActivity::class.java)
            intent.putExtra("field_id", field.id)
            context.startActivity(intent)
        }


        holder.editButton.setOnClickListener {
            onEditClicked(field.id)
        }


    }


    class FieldViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.fieldNameTextView)
        private val idTextView: TextView = itemView.findViewById(R.id.fieldIdTextView)
        val editButton: ImageButton = itemView.findViewById(R.id.imageButtonEdit)

        fun bind(field: Field) {
            nameTextView.text = field.name
            idTextView.text = "ID: ${field.id}"

        }
    }


    class FieldDiffCallback : DiffUtil.ItemCallback<Field>() {
        override fun areItemsTheSame(oldItem: Field, newItem: Field): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Field, newItem: Field): Boolean {
            return oldItem == newItem
        }
    }
}