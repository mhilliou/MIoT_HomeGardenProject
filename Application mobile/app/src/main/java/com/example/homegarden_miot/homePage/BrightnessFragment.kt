package com.example.homegarden_miot.homePage

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.homegarden_miot.R
import com.example.homegarden_miot.databinding.FragmentBrightnessBinding
import com.example.homegarden_miot.server.ApiService
import com.example.homegarden_miot.server.DayOfWeek
import com.example.homegarden_miot.server.IntensityRequest
import com.example.homegarden_miot.server.ModeLightRequest
import com.example.homegarden_miot.server.ScheduledLightResponse
import com.example.homegarden_miot.server.ServerViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BrightnessFragment : Fragment() {

    private var _binding: FragmentBrightnessBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ServerViewModel
    private lateinit var apiService: ApiService

    private var profileConnected: Boolean = false
    private lateinit var spinnerDaysOfWeek: Spinner
    private lateinit var btnChooseTime: ImageButton
    private var selectedDay: String = ""

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBrightnessBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(ServerViewModel::class.java)
        apiService = viewModel.getApiService()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerDaysOfWeek = binding.spinnerDaysOfWeek
        btnChooseTime = binding.btnClock

        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        profileConnected = sharedPreferences.getBoolean("connexion", false)

        if (profileConnected) {
            val modeProfile = sharedPreferences.getString("modeLumProfile", "manual")!!
            binding.switchButton.isChecked = modeProfile == "automatic"
        } else {
            val mode = sharedPreferences.getString("modeLum", "manual")!!
            binding.switchButton.isChecked = mode == "automatic"
        }

        if (binding.switchButton.isChecked) {
            lightMode("automatic")
            if (profileConnected) {
                sharedPreferences.edit().putString("modeLumProfile", "automatic").apply()
            } else {
                sharedPreferences.edit().putString("modeLum", "automatic").apply()
            }
            binding.calendar.visibility = View.GONE
            binding.textManuel.visibility = View.GONE
            binding.intensite.visibility = View.GONE
            binding.luminosite.visibility = View.GONE
        } else {
            lightMode("manual")
            if (profileConnected) {
                sharedPreferences.edit().putString("modeLumProfile", "manual").apply()
            } else {
                sharedPreferences.edit().putString("modeLum", "manual").apply()
            }
            binding.calendar.visibility = View.VISIBLE
            binding.textManuel.visibility = View.VISIBLE
            binding.intensite.visibility = View.VISIBLE
            binding.luminosite.visibility = View.VISIBLE
            binding.textLuminosity.text = sharedPreferences.getString("luminosity", "0.0")
            if (profileConnected) {
                if (sharedPreferences.getString("intensityProfile", "") == "") {
                    binding.slider.value = 0f
                } else {
                    binding.slider.value =
                        sharedPreferences.getString("intensityProfile", "")?.toFloat()!!
                }
                binding.textIntensity.text = sharedPreferences.getString("intensityProfile", "0.0")
            } else {
                if (sharedPreferences.getString("intensity", "") == "") {
                    binding.slider.value = 0f
                } else {
                    binding.slider.value = sharedPreferences.getString("intensity", "")?.toFloat()!!
                }
                binding.textIntensity.text = sharedPreferences.getString("intensity", "0.0")
            }
            binding.slider.addOnChangeListener { slider, value, fromUser ->
                intensity(binding.slider.value.toInt())
                binding.textIntensity.text = value.toString()
                if (profileConnected) {
                    sharedPreferences.edit().putString("intensityProfile", value.toString()).apply()
                } else {
                    sharedPreferences.edit().putString("intensity", value.toString()).apply()
                }
            }
        }
        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lightMode("automatic")
                if (profileConnected) {
                    sharedPreferences.edit().putString("modeLumProfile", "automatic").apply()
                } else {
                    sharedPreferences.edit().putString("modeLum", "automatic").apply()
                }
                binding.calendar.visibility = View.GONE
                binding.textManuel.visibility = View.GONE
                binding.intensite.visibility = View.GONE
                binding.luminosite.visibility = View.GONE
            } else {
                lightMode("manual")
                if (profileConnected) {
                    sharedPreferences.edit().putString("modeLumProfile", "manual").apply()
                } else {
                    sharedPreferences.edit().putString("modeLum", "manual").apply()
                }
                binding.calendar.visibility = View.VISIBLE
                binding.textManuel.visibility = View.VISIBLE
                binding.intensite.visibility = View.VISIBLE
                binding.luminosite.visibility = View.VISIBLE
                binding.textLuminosity.text = sharedPreferences.getString("luminosity", "0.0")
                if (profileConnected) {
                    if (sharedPreferences.getString("intensityProfile", "") == "") {
                        binding.slider.value = 0f
                    } else {
                        binding.slider.value =
                            sharedPreferences.getString("intensityProfile", "")?.toFloat()!!
                    }
                    binding.textIntensity.text =
                        sharedPreferences.getString("intensityProfile", "0.0")
                } else {
                    if (sharedPreferences.getString("intensity", "") == "") {
                        binding.slider.value = 0f
                    } else {
                        binding.slider.value =
                            sharedPreferences.getString("intensity", "")?.toFloat()!!
                    }
                    binding.textIntensity.text = sharedPreferences.getString("intensity", "0.0")
                }
                binding.slider.addOnChangeListener { _, value, _ ->
                    intensity(binding.slider.value.toInt())
                    binding.textIntensity.text = value.toString()
                    if (profileConnected) {
                        sharedPreferences.edit().putString("intensityProfile", value.toString())
                            .apply()
                    } else {
                        sharedPreferences.edit().putString("intensity", value.toString()).apply()
                    }
                }
            }
        }

        binding.btnReinitialiser.setOnClickListener {
            val daysOfWeek = resources.getStringArray(R.array.days_of_week)
            for (day in daysOfWeek) {
                if (profileConnected) {
                    sharedPreferences.edit().putString("lumPlanProfile $selectedDay", null).apply()
                } else {
                    sharedPreferences.edit().putString("lumPlan $selectedDay", null).apply()
                }
                binding.textSelectedTime.text = null
                binding.btnReinitialiser.visibility = View.GONE
                val date = when (selectedDay) {
                    "Lundi" -> DayOfWeek.lundi
                    "Mardi" -> DayOfWeek.mardi
                    "Mercredi" -> DayOfWeek.mercredi
                    "Jeudi" -> DayOfWeek.jeudi
                    "Vendredi" -> DayOfWeek.vendredi
                    "Samedi" -> DayOfWeek.samedi
                    "Dimanche" -> DayOfWeek.dimanche
                    else -> DayOfWeek.mardi
                }
                scheduledLight(date, "")
            }
        }

        val daysAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.days_of_week,
            android.R.layout.simple_spinner_item
        )
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDaysOfWeek.adapter = daysAdapter

        spinnerDaysOfWeek.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                selectedDay = parentView?.getItemAtPosition(position).toString()
                if (profileConnected) {
                    val timeProfile = sharedPreferences.getString("lumPlanProfile $selectedDay", "")
                    binding.textSelectedTime.text = timeProfile
                } else {
                    val time = sharedPreferences.getString("lumPlan $selectedDay", "")
                    binding.textSelectedTime.text = time
                }
                val date = when (selectedDay) {
                    "Lundi" -> DayOfWeek.lundi
                    "Mardi" -> DayOfWeek.mardi
                    "Mercredi" -> DayOfWeek.mercredi
                    "Jeudi" -> DayOfWeek.jeudi
                    "Vendredi" -> DayOfWeek.vendredi
                    "Samedi" -> DayOfWeek.samedi
                    "Dimanche" -> DayOfWeek.dimanche
                    else -> DayOfWeek.mardi
                }
                if (profileConnected) {
                    val timeProfile =
                        sharedPreferences.getString("lumPlanProfile $selectedDay", "")
                    scheduledLight(date, timeProfile!!)
                    if (timeProfile == "") {
                        binding.btnReinitialiser.visibility = View.GONE
                    } else {
                        binding.btnReinitialiser.visibility = View.VISIBLE
                    }
                } else {
                    val time = sharedPreferences.getString("lumPlan $selectedDay", "")
                    scheduledLight(date, time!!)
                    if (time == "") {
                        binding.btnReinitialiser.visibility = View.GONE
                    } else {
                        binding.btnReinitialiser.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
        btnChooseTime.setOnClickListener {
            showTimePicker()
        }
        binding.btnClock.setOnClickListener {
            showTimePicker()
        }
        getLuminosity()
    }

    fun lightMode(value: String) {
        val modeLightRequest = ModeLightRequest(value)
        val requestBody =
            Gson().toJson(modeLightRequest).toRequestBody("application/json".toMediaType())
        val call = apiService.lightMode(requestBody)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("Activity", "response: $response")
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Requête réussie!", Toast.LENGTH_SHORT).show()
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


    fun intensity(value: Int) {
        if(profileConnected) {
            sharedPreferences.edit().putString("intensityProfile", value.toString()).apply()
        } else {
            sharedPreferences.edit().putString("intensity", value.toString()).apply()
        }
        val intensityRequest = IntensityRequest(value)
        val requestBody =
            Gson().toJson(intensityRequest).toRequestBody("application/json".toMediaType())
        val call = apiService.intensity(requestBody)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("Activity", "response: $response")
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Requête réussie!", Toast.LENGTH_SHORT).show()
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

    fun showTimePicker() {
        Log.d("Activity", "jour: $selectedDay")
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Choisissez l'heure de luminosité")
            .build()
        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            if (profileConnected) {
                sharedPreferences.edit().putString("lumPlanProfile $selectedDay", selectedTime)
                    .apply()
            } else {
                sharedPreferences.edit().putString("lumPlan $selectedDay", selectedTime).apply()
            }
            binding.textSelectedTime.text = selectedTime
            binding.btnReinitialiser.visibility = View.VISIBLE
            val date = when (selectedDay) {
                "Lundi" -> DayOfWeek.lundi
                "Mardi" -> DayOfWeek.mardi
                "Mercredi" -> DayOfWeek.mercredi
                "Jeudi" -> DayOfWeek.jeudi
                "Vendredi" -> DayOfWeek.vendredi
                "Samedi" -> DayOfWeek.samedi
                "Dimanche" -> DayOfWeek.dimanche
                else -> DayOfWeek.mardi
            }
            scheduledLight(date, selectedTime)
        }
        timePicker.addOnNegativeButtonClickListener {}
        timePicker.show(requireActivity().supportFragmentManager, "timePicker")
    }

    fun scheduledLight(value: DayOfWeek, time: String) {
        val scheduledLightRequest = when (value) {
            DayOfWeek.lundi -> ScheduledLightResponse(
                mapOf(
                    DayOfWeek.lundi to time,
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.mardi -> ScheduledLightResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to time,
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.mercredi -> ScheduledLightResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to time,
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.jeudi -> ScheduledLightResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to time,
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.vendredi -> ScheduledLightResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to time,
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.samedi -> ScheduledLightResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to time,
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.dimanche -> ScheduledLightResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("lumPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("lumPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to time
                )
            )
        }
        Log.d("Activity", "${Gson().toJson(scheduledLightRequest)}")
        val requestBody =
            Gson().toJson(scheduledLightRequest).toRequestBody("application/json".toMediaType())
        val call = apiService.scheduledLight(requestBody)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("Activity", "response: $response")
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Requête réussie!", Toast.LENGTH_SHORT).show()
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

    fun getLuminosity() {
        val call = apiService.luminosity()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>){
                if (response.isSuccessful) {
                    try {
                        val json = response.body()?.string()
                        val luminosity = json?.toFloatOrNull() ?: 0f
                        sharedPreferences.edit().putFloat("luminosity", luminosity)
                        binding.textLuminosity.text = luminosity.toString()
                        Toast.makeText(requireContext(), "Requête réussie : temp = $luminosity", Toast.LENGTH_SHORT).show()
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

}