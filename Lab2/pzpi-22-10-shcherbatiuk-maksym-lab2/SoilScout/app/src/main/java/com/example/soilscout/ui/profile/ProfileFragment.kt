// app/src/main/java/com/example/soilscout/ui/profile/ProfileFragment.kt
package com.example.soilscout.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.soilscout.MyApplication
import com.example.soilscout.R
import com.example.soilscout.SessionManager
import com.example.soilscout.databinding.FragmentProfileBinding
import com.example.soilscout.model.User
import com.google.android.material.snackbar.Snackbar
import com.example.soilscout.LoginActivity
import com.example.soilscout.MainActivity
import com.example.soilscout.util.LocaleManager


sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>()
    data class Error( val message: String, val cause: Exception? = null) : ResultWrapper<Nothing>()
    object Loading : ResultWrapper<Nothing>()
}

interface ProfileUpdateListener {
    fun onProfileDataUpdated(user: User)
}

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }

    private var profileUpdateListener: ProfileUpdateListener? = null
    private var selectedImageUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("ProfileFragment", "onAttach called.")
        if (context is ProfileUpdateListener) {
            profileUpdateListener = context
            Log.d("ProfileFragment", "ProfileUpdateListener attached.")
        } else {
            Log.w("ProfileFragment", "Hosting activity does not implement ProfileUpdateListener. This might be expected.")
        }
    }

    override fun onDetach() {
        super.onDetach()
        profileUpdateListener = null
        Log.d("ProfileFragment", "onDetach called. ProfileUpdateListener detached.")
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                selectedImageUri = it
                viewModel.setSelectedImageUri(it)
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .circleCrop()
                    .into(binding.imageViewProfilePhoto)
                Toast.makeText(requireContext(), getString(R.string.change_photo_text), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()

        binding.languageSwitch.isChecked = LocaleManager.getLanguage() == "uk"

        if (savedInstanceState == null) {
            viewModel.fetchUserProfile()
        }
    }

    private fun setupListeners() {
        binding.buttonSaveChanges.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val currentPassword = binding.editTextCurrentPassword.text.toString().trim()
            val newPassword = binding.editTextNewPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
            viewModel.updateUserProfile(name, email, currentPassword, newPassword, confirmPassword)
        }

        binding.buttonLogout.setOnClickListener {
            SessionManager.clearAuthToken()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.textViewChangePhoto.setOnClickListener {
            openImageChooser()
        }

        binding.languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val langCode = if (isChecked) "uk" else "en"
            if (LocaleManager.getLanguage() != langCode) {
                Log.d("ProfileFragment", "Language changed from ${LocaleManager.getLanguage()} to $langCode")
                LocaleManager.setLanguage(requireContext(), langCode)

                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultWrapper.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.textViewMessage.text = getString(R.string.loading_profile_data)
                    setInteractionEnabled(false)
                }
                is ResultWrapper.Success -> {
                    binding.progressBar.isVisible = false
                    binding.textViewMessage.text = ""
                    updateUIWithUserData(result.data)
                    setInteractionEnabled(true)
                    Log.d("ProfileFragment", "Received user data update. User ID: ${result.data.id}, Photo URL: ${result.data.profile_picture_url}")
                    profileUpdateListener?.onProfileDataUpdated(result.data)
                }
                is ResultWrapper.Error -> {
                    binding.progressBar.isVisible = false
                    binding.textViewMessage.text = result.message
                    binding.textViewMessage.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                    setInteractionEnabled(true)
                    showSnackbar(result.message)
                    if (result.message.contains("Unauthorized") || result.cause is java.net.UnknownHostException) {
                        SessionManager.clearAuthToken()
                        val loginIntent = Intent(requireContext(), LoginActivity::class.java)
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(loginIntent)
                        requireActivity().finish()
                    }
                }
            }
        }

        viewModel.updateProfileResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultWrapper.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.textViewMessage.text = getString(R.string.saving_changes)
                    setInteractionEnabled(false)
                }
                is ResultWrapper.Success -> {
                    binding.progressBar.isVisible = false
                    binding.textViewMessage.text = getString(R.string.changes_saved_successfully)
                    binding.textViewMessage.setTextColor(resources.getColor(R.color.purple_700, null))
                    updateUIWithUserData(result.data)
                    clearPasswordFields()
                    setInteractionEnabled(true)
                    showSnackbar(getString(R.string.profile_updated), false)
                    viewModel.fetchUserProfile()
                    selectedImageUri = null

                    // ДОДАНО: Перезавантаження активностей після успішного збереження змін
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is ResultWrapper.Error -> {
                    binding.progressBar.isVisible = false
                    binding.textViewMessage.text = result.message
                    binding.textViewMessage.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                    setInteractionEnabled(true)
                    showSnackbar(result.message)
                }
            }
        }

        viewModel.validationError.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.textViewMessage.text = it
                binding.textViewMessage.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                showSnackbar(it)
            }
        }
    }

    private fun updateUIWithUserData(user: User) {
        binding.editTextEmail.setText(user.email)
        binding.editTextName.setText(user.name)

        if (selectedImageUri == null && !user.profile_picture_url.isNullOrEmpty()) {
            val imageUrl = "${MyApplication.BASE_IMAGE_URL}${user.profile_picture_url}"
            Log.d("ProfileFragment", "Loading profile image from URL: $imageUrl")

            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(binding.imageViewProfilePhoto)
        } else if (selectedImageUri == null) {
            binding.imageViewProfilePhoto.setImageResource(R.drawable.ic_launcher_foreground)
            Log.d("ProfileFragment", "No profile image URL and no selected image, showing default placeholder.")
        }

        val hasPasswordSet = !user.password.isNullOrEmpty()

        binding.textViewPasswordChangeTitle.isVisible = hasPasswordSet
        binding.textInputLayoutCurrentPassword.isVisible = hasPasswordSet
        binding.textInputLayoutNewPassword.isVisible = hasPasswordSet
        binding.textInputLayoutConfirmPassword.isVisible = hasPasswordSet

        if (!hasPasswordSet) {
            clearPasswordFields()
        }
    }

    private fun clearPasswordFields() {
        binding.editTextCurrentPassword.text = null
        binding.editTextNewPassword.text = null
        binding.editTextConfirmPassword.text = null
    }

    private fun setInteractionEnabled(isEnabled: Boolean) {
        binding.buttonSaveChanges.isEnabled = isEnabled
        binding.buttonLogout.isEnabled = isEnabled
        binding.editTextName.isEnabled = isEnabled
        binding.textViewChangePhoto.isEnabled = isEnabled
        binding.editTextCurrentPassword.isEnabled = isEnabled
        binding.editTextNewPassword.isEnabled = isEnabled
        binding.editTextConfirmPassword.isEnabled = isEnabled
        binding.languageSwitch.isEnabled = isEnabled
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun showSnackbar(message: String, isError: Boolean = true) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(resources.getColor(android.R.color.holo_red_light, null))
        } else {
            snackbar.setBackgroundTint(resources.getColor(R.color.purple_200, null))
        }
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}