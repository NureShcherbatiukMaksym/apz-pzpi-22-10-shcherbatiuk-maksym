package com.example.soilscout.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soilscout.databinding.FragmentDashboardBinding
import com.example.soilscout.ui.createeditfield.CreateEditFieldActivity
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.widget.PopupMenu
import android.graphics.drawable.Drawable
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.soilscout.R


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var fieldAdapter: FieldAdapter

    private lateinit var fieldEditorLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fieldEditorLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Field was created or updated, refresh the list
                dashboardViewModel.fetchUserFields()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fieldAdapter = FieldAdapter { fieldId ->
            val intent = Intent(context, CreateEditFieldActivity::class.java)
            intent.putExtra("field_id", fieldId)
            fieldEditorLauncher.launch(intent)
        }
        binding.fieldsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.fieldsRecyclerView.adapter = fieldAdapter


        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dashboardViewModel.setSearchTerm(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.buttonSortCriteria.setOnClickListener {
            showSortCriteriaMenu(it)
        }

        binding.buttonSortOrderToggle.setOnClickListener {
            dashboardViewModel.toggleSortOrder()
        }

        binding.buttonFilter.setOnClickListener {
            showFilterMenu(it)
        }

        binding.buttonCreateField.setOnClickListener {
            val intent = Intent(context, CreateEditFieldActivity::class.java)
            fieldEditorLauncher.launch(intent)
        }


        dashboardViewModel.loadUserDataFromPrefs()

        dashboardViewModel.userDataText.observe(viewLifecycleOwner) { userData ->
            binding.textDashboard.text = userData
        }

        dashboardViewModel.displayedFields.observe(viewLifecycleOwner) { fieldsList ->
            fieldAdapter.submitList(fieldsList)

            if (dashboardViewModel.isLoading.value == false) {
                if (fieldsList.isNullOrEmpty()) {
                    binding.messageTextView.visibility = View.VISIBLE
                    if (dashboardViewModel.error.value == null) {
                        val currentSearchTerm = dashboardViewModel.searchTerm.value.orEmpty()
                        val currentFilter = dashboardViewModel.filter.value.orEmpty()

                        if (currentSearchTerm.isNotBlank() || currentFilter != "all") {
                            binding.messageTextView.text = "Полів не знайдено за вашим запитом або фільтрами."
                        } else {
                            binding.messageTextView.text = "Полів немає."
                        }

                    }
                    binding.fieldsRecyclerView.visibility = View.GONE
                } else {
                    binding.messageTextView.visibility = View.GONE
                    binding.fieldsRecyclerView.visibility = View.VISIBLE
                }
                binding.fieldsListTitle.visibility = View.VISIBLE
            } else {
                binding.messageTextView.visibility = View.GONE
                binding.fieldsRecyclerView.visibility = View.GONE
                binding.fieldsListTitle.visibility = View.VISIBLE
            }
        }


        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

            if (isLoading) {
                binding.messageTextView.visibility = View.GONE
                binding.fieldsRecyclerView.visibility = View.GONE
            }
        }


        dashboardViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                if (dashboardViewModel.displayedFields.value.isNullOrEmpty() && dashboardViewModel.isLoading.value == false) {
                    binding.messageTextView.text = "Помилка завантаження: $errorMessage"
                    binding.messageTextView.visibility = View.VISIBLE
                    binding.fieldsRecyclerView.visibility = View.GONE
                }
            }
        }

        dashboardViewModel.sortBy.observe(viewLifecycleOwner) { updateSortButtonsUI() }
        dashboardViewModel.sortOrder.observe(viewLifecycleOwner) { updateSortButtonsUI() }
        dashboardViewModel.filter.observe(viewLifecycleOwner) { updateFilterButtonUI() }


        return root
    }

    private fun showSortCriteriaMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val sortOptions = resources.getStringArray(R.array.sort_criteria_options)

        sortOptions.forEachIndexed { index, optionText ->
            popupMenu.menu.add(0, index, index, optionText)
        }


        popupMenu.setOnMenuItemClickListener { menuItem ->
            val selectedSortCriteria = when (menuItem.title.toString()) {
                getString(R.string.sort_by_name) -> "name"
                getString(R.string.sort_by_area) -> "area"
                getString(R.string.sort_by_created_at) -> "created_at"
                else -> "name" // Дефолт
            }
            dashboardViewModel.setSortCriteria(selectedSortCriteria)
            true
        }
        popupMenu.show()
    }

    private fun showFilterMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val filterOptions = resources.getStringArray(R.array.filter_criteria_options)

        filterOptions.forEachIndexed { index, optionText ->
            popupMenu.menu.add(0, index, index, optionText)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val selectedFilterCriteria = when (menuItem.title.toString()) {
                getString(R.string.filter_all) -> "all"
                getString(R.string.filter_active) -> "active"
                getString(R.string.filter_inactive) -> "inactive"
                else -> "all" // Дефолт
            }
            dashboardViewModel.setFilter(selectedFilterCriteria)
            true
        }
        popupMenu.show()
    }


    private fun updateSortButtonsUI() {
        val sortByText = when (dashboardViewModel.sortBy.value) {
            "name" -> getString(R.string.sort_by_name)
            "area" -> getString(R.string.sort_by_area)
            "created_at" -> getString(R.string.sort_by_created_at)
            else -> getString(R.string.sort_by_name)
        }
        binding.buttonSortCriteria.text = sortByText

        val sortOrderIconRes = if (dashboardViewModel.sortOrder.value == "asc") {
            R.drawable.ic_arrow_up
        } else {
            R.drawable.ic_arrow_down
        }
        val sortOrderIcon: Drawable? = ContextCompat.getDrawable(requireContext(), sortOrderIconRes)
        binding.buttonSortOrderToggle.icon = sortOrderIcon
    }

    private fun updateFilterButtonUI() {

        val filterIconColor = when (dashboardViewModel.filter.value) {
            "all" -> ContextCompat.getColor(requireContext(), R.color.purple_200)
            else ->  ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary)
        }

        binding.buttonFilter.iconTint = android.content.res.ColorStateList.valueOf(filterIconColor)

        binding.buttonFilter.contentDescription = when(dashboardViewModel.filter.value) {
            "all" -> getString(R.string.filter_all)
            "active" -> getString(R.string.filter_active)
            "inactive" -> getString(R.string.filter_inactive)
            else -> "Фільтр"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}