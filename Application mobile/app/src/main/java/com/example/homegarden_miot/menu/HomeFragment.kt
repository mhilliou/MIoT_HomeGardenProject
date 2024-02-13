package com.example.homegarden_miot.menu

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.homegarden_miot.R
import com.example.homegarden_miot.databinding.FragmentHomeBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        binding.btnArrosage.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_arrosage)
        }

        binding.btnLuminosite.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_luminosite)
        }

        binding.btnEau.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_eau)
        }

        binding.btnEngrais.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_engrais)
        }

        binding.buttonConnect.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_connect)
        }


        return binding.root
    }

}