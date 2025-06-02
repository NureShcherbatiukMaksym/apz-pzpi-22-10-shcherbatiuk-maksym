package com.example.soilscout.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soilscout.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupClearButton()
        observeNotifications()

        return root
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter()
        binding.notificationsRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun setupClearButton() {
        binding.clearAllNotificationsButton.setOnClickListener {
            notificationsViewModel.clearAllNotifications()
        }
    }

    private fun observeNotifications() {
        notificationsViewModel.allNotifications.observe(viewLifecycleOwner) { notifications ->
            notificationAdapter.submitList(notifications)
            if (notifications.isNullOrEmpty()) {
                binding.notificationsRecyclerview.visibility = View.GONE
                binding.noNotificationsMessage.visibility = View.VISIBLE
            } else {
                binding.notificationsRecyclerview.visibility = View.VISIBLE
                binding.noNotificationsMessage.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}