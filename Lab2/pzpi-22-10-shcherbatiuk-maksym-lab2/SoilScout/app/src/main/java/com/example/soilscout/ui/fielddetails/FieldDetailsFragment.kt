package com.example.soilscout.ui.fielddetails

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.soilscout.R
import com.example.soilscout.databinding.FragmentFieldDetailsBinding
import com.example.soilscout.model.Field
import com.example.soilscout.model.MeasurementPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.UiSettings
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class FieldDetailsFragment : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    private var _binding: FragmentFieldDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FieldDetailsViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var googleMap: GoogleMap? = null
    private var fieldPolygon: Polygon? = null
    private val markers = mutableMapOf<String, Marker>()

    private var currentPointDialog: AlertDialog? = null
    private var dialogPointId: Int? = null
    private var dialogDataTextView: TextView? = null

    private var dialogStatusTextView: TextView? = null
    private var dialogActionButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fieldId = arguments?.getInt("field_id", -1) ?: -1
        viewModel = ViewModelProvider(this).get(FieldDetailsViewModel::class.java)
        viewModel.init(fieldId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFieldDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.fieldDetails.observe(viewLifecycleOwner) { field ->
            field?.let {
                val activity = requireActivity() as? AppCompatActivity
                activity?.supportActionBar?.title = it.name
                binding.fieldAreaTextView.text = "Площа: ${it.area}"
                binding.fieldCreatedAtTextView.text = "Створено: ${formatDate(it.created_at)}"
                drawFieldPolygon(it)
            }
        }

        viewModel.points.observe(viewLifecycleOwner) { pointsList ->
            updateMarkersOnMap(pointsList)

            if (currentPointDialog != null && dialogPointId != null) {
                val updatedPoint = pointsList.find { it.id == dialogPointId }
                updatedPoint?.let {
                    updateDialogData(it)
                    // Ensure the status and button are updated when the underlying data changes
                    updateDialogStatusAndButton(it)
                }
            }

            if (pointsList.isNullOrEmpty()) {
                binding.messageTextView.visibility = View.VISIBLE
                if (viewModel.error.value == null) {
                    binding.messageTextView.text = "Точок для відображення на карті немає."
                }
            } else {
                binding.messageTextView.visibility = View.GONE
                googleMap?.let { map ->
                    if (pointsList.isNotEmpty() && markers.isNotEmpty()) {
                        zoomToFitMarkers(pointsList.map { LatLng(it.latitude, it.longitude) })
                    }
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.messageTextView.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Log.e("FieldDetailsFragment", "Error observed: $errorMessage")
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                binding.messageTextView.text = "Помилка: $errorMessage"
                binding.messageTextView.visibility = View.VISIBLE
            } else {
                if (!viewModel.points.value.isNullOrEmpty()) {
                    binding.messageTextView.visibility = View.GONE
                }
            }
        }

        viewModel.socketStatus.observe(viewLifecycleOwner) { status ->
            Log.d("FieldDetailsFragment", "WebSocket Status: $status")
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.uiSettings.isMapToolbarEnabled = false

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        viewModel.fieldDetails.value?.let { drawFieldPolygon(it) }
        viewModel.points.value?.let {
            updateMarkersOnMap(it)
            if (it.isNotEmpty()) {
                zoomToFitMarkers(it.map { LatLng(it.latitude, it.longitude) })
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                if (googleMap != null) {
                    try {
                        googleMap?.isMyLocationEnabled = true
                        googleMap?.uiSettings?.isMyLocationButtonEnabled = true
                    } catch (e: SecurityException) {
                        Log.e("FieldDetailsFragment", "Location permission granted but cannot enable My Location Layer", e)
                    }
                }
            } else {
                Toast.makeText(context, "Дозвіл на геолокацію відхилено. Ваше місцезнаходження не буде відображено.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun drawFieldPolygon(field: Field) {
        googleMap?.let { map ->
            val polygonCoordinates = parseGeoZone(field.geo_zone)

            if (polygonCoordinates.isNotEmpty()) {
                fieldPolygon?.remove()
                val polygonOptions = PolygonOptions()
                    .addAll(polygonCoordinates)
                    .strokeColor(Color.BLUE)
                    .strokeWidth(5f)
                    .fillColor(Color.argb(100, 0, 0, 255))
                fieldPolygon = map.addPolygon(polygonOptions)
                if (polygonCoordinates.isNotEmpty() && viewModel.points.value.isNullOrEmpty()) {
                    zoomToFitMarkers(polygonCoordinates)
                }
            }
        }
    }

    private fun parseGeoZone(geoZoneString: String?): List<LatLng> {
        val coordinates = mutableListOf<LatLng>()
        if (geoZoneString.isNullOrEmpty()) return coordinates

        try {
            geoZoneString.split(';').forEach {
                val parts = it.split(',')
                if (parts.size == 2) {
                    coordinates.add(LatLng(parts[0].toDouble(), parts[1].toDouble()))
                }
            }
        } catch (e: Exception) {
            Log.e("FieldDetailsFragment", "Error parsing geo_zone: $geoZoneString", e)
            Toast.makeText(context, "Не вдалося розпарсити координати полігону", Toast.LENGTH_SHORT).show()
        }
        return coordinates
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        return drawableToBitmap(drawable)
    }

    private fun updateMarkersOnMap(pointsList: List<MeasurementPoint>) {
        googleMap?.let { map ->
            val context = context ?: return

            val pointIds = pointsList.map { it.id.toString() }.toSet()
            markers.keys.filterNot { pointIds.contains(it) }.forEach { markerId ->
                markers.remove(markerId)?.remove()
            }

            pointsList.forEach { point ->
                val latLng = LatLng(point.latitude, point.longitude)
                val marker = markers[point.id.toString()]

                val drawableRes = if (point.active) {
                    R.drawable.circle_with_border_green // Assuming you have a green circle for active points
                } else {
                    R.drawable.circle_with_border // Assuming you have a default circle for inactive points
                }

                val markerIcon: BitmapDescriptor? = bitmapFromDrawableRes(context, drawableRes)?.let {
                    BitmapDescriptorFactory.fromBitmap(it)
                }

                markerIcon?.let { icon ->
                    if (marker == null) {
                        val newMarker = map.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .icon(icon)
                        )
                        if (newMarker != null) {
                            newMarker.tag = point
                            markers[point.id.toString()] = newMarker
                        }
                    } else {
                        marker.position = latLng
                        marker.tag = point
                        marker.setIcon(icon)
                    }
                } ?: run {
                    Log.e("FieldDetailsFragment", "Failed to create marker icon for point ${point.id} from resource $drawableRes")
                }
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val point = marker.tag as? MeasurementPoint ?: return true

        if (currentPointDialog != null && dialogPointId == point.id) {
            Log.d("FieldDetailsFragment", "Dialog for point ${point.id} is already open.")
            return true
        }

        currentPointDialog?.dismiss()


        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_info_window, null)
        val coordinatesTextView = dialogView.findViewById<TextView>(R.id.info_coordinates)
        val dataTextView = dialogView.findViewById<TextView>(R.id.info_data)
        val statusTextView = dialogView.findViewById<TextView>(R.id.info_status)
        val actionButton = dialogView.findViewById<Button>(R.id.info_action_button)

        dialogDataTextView = dataTextView
        dialogStatusTextView = statusTextView
        dialogActionButton = actionButton

        coordinatesTextView.text = "Координати: ${String.format("%.5f", point.latitude)}, ${String.format("%.5f", point.longitude)}"

        updateDialogData(point)

        updateDialogStatusAndButton(point)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Деталі точки №${point.point_order}")
            .create()

        actionButton.setOnClickListener {
            val currentPointState = viewModel.points.value?.find { it.id == point.id }
            currentPointState?.let {
                if (it.active) {
                    viewModel.deactivatePoint(it.id)
                } else {
                    viewModel.activatePoint(it.id)
                }
            } ?: run {
                Log.e("FieldDetailsFragment", "Cannot find point with ID ${point.id} in current ViewModel data.")
                Toast.makeText(context, "Помилка: Точка не знайдена.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

        }

        dialog.setOnDismissListener {
            currentPointDialog = null
            dialogPointId = null
            dialogDataTextView = null
            dialogStatusTextView = null
            dialogActionButton = null
            Log.d("FieldDetailsFragment", "Dialog for point ${point.id} dismissed. References cleared.")
        }

        dialog.show()

        currentPointDialog = dialog
        dialogPointId = point.id
        Log.d("FieldDetailsFragment", "Dialog for point ${point.id} shown. References stored.")

        return true
    }

    private fun updateDialogData(point: MeasurementPoint) {
        val measurementData = buildString {
            append("Дані заміру:\n")
            point.latestSoilMoisture?.let { append("- Вологість: ${it}%\n") } ?: append("- Вологість: немає даних\n")
            point.latestTemperature?.let { append("- Температура: ${String.format("%.1f", it)}°C\n") } ?: append("- Температура: немає даних\n")
            point.latestAcidity?.let { append("- Кислотність: ${String.format("%.1f", it)}\n") } ?: append("- Кислотність: немає даних\n")
        }.trimEnd()
        dialogDataTextView?.text = measurementData
        Log.d("FieldDetailsFragment", "Dialog data updated for point ${point.id}")
    }

    private fun updateDialogStatusAndButton(point: MeasurementPoint) {
        if (point.active) {
            dialogStatusTextView?.text = "Статус: Активна"
            dialogStatusTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            dialogActionButton?.text = "Деактивувати"
        } else {
            dialogStatusTextView?.text = "Статус: Неактивна"
            dialogStatusTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            dialogActionButton?.text = "Активувати"
        }
        Log.d("FieldDetailsFragment", "Dialog status and button updated for point ${point.id}")
    }


    private fun zoomToFitMarkers(points: List<LatLng>) {
        if (points.isEmpty()) return
        googleMap?.let { map ->
            val builder = LatLngBounds.Builder()
            points.forEach { builder.include(it) }
            fieldPolygon?.points?.forEach { builder.include(it) }

            val bounds = builder.build()
            val padding = 150
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            try {
                map.animateCamera(cu)
            } catch (e: IllegalStateException) {
                Log.e("FieldDetailsFragment", "Cannot animate camera: ${e.message}")
                map.moveCamera(cu)
            } catch (e: IllegalArgumentException) {
                Log.e("FieldDetailsFragment", "Cannot zoom to bounds: ${e.message}")
                if (points.size == 1) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 15f))
                }
            }
        }
    }

    private fun formatDate(dateString: String): String {
        if (dateString.isNullOrEmpty()) return "Немає даних"

        val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
        val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return try {
            val date: Date? = apiFormat.parse(dateString)
            if (date != null) displayFormat.format(date) else dateString
        } catch (e: ParseException) {
            Log.e("FieldDetailsFragment", "Error parsing date: $dateString", e)
            dateString
        } catch (e: Exception) {
            Log.e("FieldDetailsFragment", "Unexpected error formatting date: $dateString", e)
            dateString
        }
    }

    override fun onStop() {
        super.onStop()
        currentPointDialog?.dismiss()
        lifecycleScope.launch {
            viewModel.deselectField()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        googleMap = null
        _binding = null
        currentPointDialog = null
        dialogPointId = null
        dialogDataTextView = null
        dialogStatusTextView = null
        dialogActionButton = null
    }
}