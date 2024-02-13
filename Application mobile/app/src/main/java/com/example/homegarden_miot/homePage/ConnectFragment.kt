package com.example.homegarden_miot.homePage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.homegarden_miot.R
import com.example.homegarden_miot.databinding.FragmentConnectBinding


class ConnectFragment : Fragment() {

    private var _binding: FragmentConnectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConnectBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}