package com.example.homegarden_miot.homePage

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.homegarden_miot.databinding.FragmentFertilizerBinding

class FertilizerFragment : Fragment() {

    companion object {
        private val lineSet= listOf(
            "label1" to 4.5F,
            "label2" to 6F,
            "label3" to 10F,
            "label4" to 3F,
            "label5" to 7F,
            "label6" to 5.5F
        )
        private const val animationDuration = 1000L
    }

    private var _binding: FragmentFertilizerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFertilizerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lineChart.gradientFillColors = intArrayOf(
            Color.parseColor("#9CE5C2"),
            Color.TRANSPARENT
        )
        binding.lineChart.animation.duration = animationDuration
        binding.lineChart.animate(lineSet)
    }

}