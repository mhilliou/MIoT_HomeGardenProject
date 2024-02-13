package com.example.homegarden_miot.homePage

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.homegarden_miot.databinding.FragmentWaterBinding
import com.example.homegarden_miot.server.ApiService
import com.example.homegarden_miot.server.ServerViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WaterFragment : Fragment() {

    companion object {
        private val lineSet= listOf(
            "label1" to 5F,
            "label2" to 12F,
            "label3" to 2F,
            "label4" to 3F,
            "label5" to 7F,
            "label6" to 1.5F,
            "label7" to 3F,
            "label8" to 2F,
            "label9" to 10F,
            "label10" to 6F,
        )
        private const val animationDuration=1000L
    }

    private var _binding: FragmentWaterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ServerViewModel
    private lateinit var apiService: ApiService

    private var profileConnected: Boolean = false

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWaterBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(ServerViewModel::class.java)
        apiService = viewModel.getApiService()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        profileConnected = sharedPreferences.getBoolean("connexion", false)

        val waterLevel = sharedPreferences.getFloat("waterLevel", 0f)
        binding.waterLevel.text = waterLevel.toString()
        if(waterLevel < 5.0) {
            binding.imgAttentionWater.visibility = View.VISIBLE
        }
        else {
            binding.imgAttentionWater.visibility = View.GONE
        }

        val humidity = sharedPreferences.getFloat("humidity", 0f)
        binding.humidity.text = humidity.toString()
        if(humidity < 10.0) {
            binding.imgAttentionHumidity.visibility = View.VISIBLE
        }
        else {
            binding.imgAttentionHumidity.visibility = View.GONE
        }

        val temperature = sharedPreferences.getFloat("temperature", 0f)
        binding.temp.text = temperature.toString()
        if(temperature > 30.0) {
            binding.imgAttentionTemperature.visibility = View.VISIBLE
        }
        else {
            binding.imgAttentionTemperature.visibility = View.GONE
        }

        getWaterLevel()
        getTemperature()
        getHumidity()
        binding.lineChart.gradientFillColors = intArrayOf(
            Color.parseColor("#9CE5C2"),
            Color.TRANSPARENT
        )
        binding.lineChart.animation.duration = animationDuration
        binding.lineChart.animate(lineSet)
    }

    fun getWaterLevel() {
        val call = apiService.waterLevel()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>){
                if (response.isSuccessful) {
                    try {
                        val json = response.body()?.string()
                        val waterLevel = json?.toFloatOrNull() ?: 0f
                        sharedPreferences.edit().putFloat("waterLevel", waterLevel)
                        binding.waterLevel.text = waterLevel.toString()
                        if(waterLevel < 5.0) {
                            binding.imgAttentionWater.visibility = View.VISIBLE
                            showAlertDialog()
                        }
                        else {
                            binding.imgAttentionWater.visibility = View.GONE
                        }
                        Toast.makeText(requireContext(), "Requête réussie : waterLevel = $waterLevel", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("Activity", "Exception while processing response: $e")
                    }
                } else {
                    Toast.makeText(requireContext(), "Requête échouée!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Activity", "throwable: $t")
                Toast.makeText(requireContext(), "Requête failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getHumidity() {
        val call = apiService.humidity()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>){
                if (response.isSuccessful) {
                    try {
                        val json = response.body()?.string()
                        val humidity = json?.toFloatOrNull() ?: 0f
                        sharedPreferences.edit().putFloat("humidity", humidity)
                        binding.humidity.text = humidity.toString()
                        if(humidity < 10.0) {
                            binding.imgAttentionHumidity.visibility = View.VISIBLE
                        }
                        else {
                            binding.imgAttentionHumidity.visibility = View.GONE
                        }
                        Toast.makeText(requireContext(), "Requête réussie : temp = $humidity", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("Activity", "Exception while processing response: $e")
                    }
                } else {
                    Toast.makeText(requireContext(), "Requête échouée!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Activity", "throwable: $t")
                Toast.makeText(requireContext(), "Requête failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getTemperature() {
        val call = apiService.temperature()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>){
                if (response.isSuccessful) {
                    try {
                        val json = response.body()?.string()
                        val temperature = json?.toFloatOrNull() ?: 0f
                        sharedPreferences.edit().putFloat("temperature", temperature)
                        binding.temp.text = temperature.toString()
                        if(temperature > 30.0) {
                            binding.imgAttentionTemperature.visibility = View.VISIBLE
                        }
                        else {
                            binding.imgAttentionTemperature.visibility = View.GONE
                        }
                        Toast.makeText(requireContext(), "Requête réussie : temp = $temperature", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("Activity", "Exception while processing response: $e")
                    }
                } else {
                    Toast.makeText(requireContext(), "Requête échouée!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Activity", "throwable: $t")
                Toast.makeText(requireContext(), "Requête failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Alerte")
            .setMessage("Le niveau d'eau est inférieur à 5L. Veuillez remplir le réservoir.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}