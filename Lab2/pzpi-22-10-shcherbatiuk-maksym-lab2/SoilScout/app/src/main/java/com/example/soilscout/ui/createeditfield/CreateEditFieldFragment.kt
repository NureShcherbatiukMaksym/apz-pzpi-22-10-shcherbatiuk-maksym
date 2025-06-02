package com.example.soilscout.ui.createeditfield

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.soilscout.R
import com.example.soilscout.databinding.FragmentFieldCreateEditBinding
import com.example.soilscout.ui.dashboard.Result
import org.json.JSONObject

class CreateEditFieldFragment : Fragment() {


    private var _binding: FragmentFieldCreateEditBinding? = null
    private val binding get() = _binding!!


    private lateinit var viewModel: CreateEditFieldViewModel


    private var fieldId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val idFromArgs = it.getInt("field_id", -1)
            if (idFromArgs != -1) {
                fieldId = idFromArgs
            }
        }

    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFieldCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(CreateEditFieldViewModel::class.java)

        if (viewModel.fieldId == null && this.fieldId != null) {
            viewModel.fieldId = this.fieldId
            viewModel.loadFieldDetails(this.fieldId!!)
        }

        if (fieldId != null) {
            binding.buttonDelete.visibility = View.VISIBLE
            binding.buttonSave.text = getString(R.string.button_edit_field)
        } else {
            binding.buttonDelete.visibility = View.GONE
            binding.buttonSave.text = getString(R.string.button_create)
        }

        viewModel.fieldDetails.observe(viewLifecycleOwner) { field ->
            field?.let {

                binding.editTextFieldName.setText(it.name)
                binding.editTextArea.setText(it.area.toString())

                val geoZoneString = it.geo_zone

                val formattedCoordsString = try {
                    val geoJson = JSONObject(geoZoneString)

                    val coordinatesArray = geoJson.getJSONArray("coordinates")

                    if (coordinatesArray.length() > 0) {
                        val polygonRing = coordinatesArray.getJSONArray(0)

                        val coordsListForDisplay = mutableListOf<String>()

                        for (i in 0 until polygonRing.length()) {
                            val pointArray = polygonRing.getJSONArray(i)
                            if (pointArray.length() == 2) {
                                val lat = pointArray.getDouble(0)
                                val lng = pointArray.getDouble(1)


                                coordsListForDisplay.add("[$lat, $lng]")
                            }
                        }
                        coordsListForDisplay.joinToString(",\n")

                    } else {
                        ""
                    }
                } catch (e: Exception) {
                    Log.e("CreateEditFieldFragment", "Помилка парсингу GeoJSON для відображення", e)
                    "Помилка завантаження координат"
                }


                binding.editTextManualCoords.setText(formattedCoordsString)


            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You might want to show/hide a progress bar here
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }


        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is Result.Success -> {
                        Toast.makeText(requireContext(), if (fieldId == null) "Поле створено!" else "Поле оновлено!", Toast.LENGTH_SHORT).show()
                        requireActivity().setResult(Activity.RESULT_OK) // Set result to OK
                        requireActivity().finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
                }
                viewModel.clearSaveResult()
            }
        }


        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is Result.Success -> {
                        Toast.makeText(requireContext(), "Поле успішно видалено!", Toast.LENGTH_SHORT).show()
                        requireActivity().setResult(Activity.RESULT_OK) // Set result to OK
                        requireActivity().finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
                }
                viewModel.clearDeleteResult()
            }
        }


        binding.buttonCancel.setOnClickListener {
            requireActivity().finish()
        }

        binding.buttonSave.setOnClickListener {

            val name = binding.editTextFieldName.text.toString()
            val areaString = binding.editTextArea.text.toString()
            val manualCoordsString = binding.editTextManualCoords.text.toString()

            val geoZoneCoordinates: List<List<Double>>? = try {
                val cleanedString = manualCoordsString
                    .trim()
                    .replace("\\s+".toRegex(), "")

                val pairsString = cleanedString.removePrefix("[").removeSuffix("]").split("],[")

                val parsedCoords = pairsString.map { pairString ->
                    pairString.split(",")
                        .map { it.toDouble() }
                        .filter { it.isFinite() }
                }.filter { it.size == 2 }


                if (parsedCoords.size < 3) {
                    throw IllegalArgumentException("Недостатньо точок для полігону (мінімум 3).")
                }

                // If the first and last points are not the same, close the polygon
                if (parsedCoords.size >= 1 && parsedCoords.first() != parsedCoords.last()) {
                    parsedCoords + listOf(parsedCoords.first())
                } else {
                    parsedCoords
                }


            } catch (e: NumberFormatException) {
                Log.e("CreateEditFieldFragment", "Помилка парсингу чисел координат", e)
                Toast.makeText(requireContext(), "Невірний формат чисел координат. Перевірте синтаксис.", Toast.LENGTH_LONG).show()
                null
            } catch (e: IllegalArgumentException) {
                Log.e("CreateEditFieldFragment", "Недостатньо точок для полігону або інша невалідна структура", e)
                Toast.makeText(requireContext(), e.message ?: "Невірні координати або недостатньо точок.", Toast.LENGTH_LONG).show()
                null
            }
            catch (e: Exception) {
                Log.e("CreateEditFieldFragment", "Невідома помилка парсингу координат", e)
                Toast.makeText(requireContext(), "Невірний формат координат. Перевірте синтаксис.", Toast.LENGTH_LONG).show()
                null
            }


            if (name.isBlank() || areaString.isBlank() || geoZoneCoordinates.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Будь ласка, заповніть назву, площу та введіть коректні координати.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (name.isBlank() || areaString.isBlank() || geoZoneCoordinates.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Будь ласка, заповніть назву, площу та введіть коректні координати.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val area = areaString.toFloatOrNull()

            if (area == null || area < 0) {
                Toast.makeText(requireContext(), "Введіть коректне числове значення для площі.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveField(name, area, geoZoneCoordinates)
        }

        binding.buttonDelete.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Видалити поле")
                .setMessage("Ви впевнені, що хочете видалити це поле? Цю дію не можна скасувати.")
                .setPositiveButton("Видалити") { dialog, which ->

                    fieldId?.let { id ->
                        viewModel.deleteField(id)
                    } ?: run {
                        Toast.makeText(requireContext(), "Помилка: неможливо видалити поле без ID.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Скасувати", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}