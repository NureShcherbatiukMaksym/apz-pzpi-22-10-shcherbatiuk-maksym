package com.example.soilscout.ui.fielddetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // Імпорт Button
import android.widget.TextView
import androidx.core.content.ContextCompat // Для роботи з кольорами
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soilscout.R // Імпорт R класу
import com.example.soilscout.model.MeasurementPoint // Імпорт моделі Point


class PointAdapter(
    private val onActivateClick: (pointId: Int) -> Unit,
    private val onDeactivateClick: (pointId: Int) -> Unit
) : ListAdapter<MeasurementPoint, PointAdapter.PointViewHolder>(PointDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_point, parent, false)
        return PointViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        val currentPoint = getItem(position)
        holder.bind(currentPoint, onActivateClick, onDeactivateClick)
    }

    class PointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pointNumberTextView: TextView = itemView.findViewById(R.id.pointNumberTextView)
        private val latitudeTextView: TextView = itemView.findViewById(R.id.latitudeTextView)
        private val longitudeTextView: TextView = itemView.findViewById(R.id.longitudeTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        private val activateDeactivateButton: Button = itemView.findViewById(R.id.activateDeactivateButton)


        fun bind(
            point: MeasurementPoint,
            onActivateClick: (pointId: Int) -> Unit,
            onDeactivateClick: (pointId: Int) -> Unit
        ) {

            pointNumberTextView.text = "№${point.point_order}"
            latitudeTextView.text = "Широта: ${point.latitude}"
            longitudeTextView.text = "Довгота: ${point.longitude}"


            if (point.active) {
                statusTextView.text = "Активна"
                statusTextView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                activateDeactivateButton.text = "Деактивувати"
                activateDeactivateButton.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))

                activateDeactivateButton.setOnClickListener { onDeactivateClick(point.id) }
            } else {
                statusTextView.text = "Неактивна"
                statusTextView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                activateDeactivateButton.text = "Активувати"
                activateDeactivateButton.setTextColor(ContextCompat.getColor(itemView.context, com.google.android.material.R.color.design_default_color_primary))

                activateDeactivateButton.setOnClickListener { onActivateClick(point.id) }
            }


        }
    }


    class PointDiffCallback : DiffUtil.ItemCallback<MeasurementPoint>() {
        override fun areItemsTheSame(oldItem: MeasurementPoint, newItem: MeasurementPoint): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MeasurementPoint, newItem: MeasurementPoint): Boolean {

            return oldItem.id == newItem.id &&
                    oldItem.field_id == newItem.field_id &&
                    oldItem.point_order == newItem.point_order &&
                    oldItem.latitude == newItem.latitude &&
                    oldItem.longitude == newItem.longitude &&
                    oldItem.active == newItem.active

        }
    }
}

