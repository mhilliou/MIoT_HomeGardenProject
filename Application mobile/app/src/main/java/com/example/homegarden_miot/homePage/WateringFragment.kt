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
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.ImageButton
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import com.example.homegarden_miot.databinding.FragmentWateringBinding
import com.example.homegarden_miot.server.ApiService
import android.widget.Toast
import com.example.homegarden_miot.R
import com.example.homegarden_miot.server.ActivePumpRequest
import com.example.homegarden_miot.server.DayOfWeek
import com.example.homegarden_miot.server.ModePumpRequest
import com.example.homegarden_miot.server.ScheduledPumpResponse
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

class WateringFragment : Fragment() {

    private var _binding: FragmentWateringBinding? = null
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
        _binding = FragmentWateringBinding.inflate(layoutInflater, container, false)
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
            val modeProfile = sharedPreferences.getString("modeProfile", "manual")!!
            binding.switchButton.isChecked = modeProfile == "automatic"
        } else {
            val mode = sharedPreferences.getString("mode", "manual")!!
            binding.switchButton.isChecked = mode == "automatic"
        }

        if (binding.switchButton.isChecked) {
            pumpMode("automatic")
            Log.d("Activity", "automatic")
            if (profileConnected) {
                sharedPreferences.edit().putString("modeProfile", "automatic").apply()
            } else {
                sharedPreferences.edit().putString("mode", "automatic").apply()
            }
            binding.calendar.visibility = View.GONE
            binding.textManuel.visibility = View.GONE
            binding.btnArroserPlantes.visibility = View.GONE
        } else {
            pumpMode("manual")
            Log.d("Activity", "manual")
            if (profileConnected) {
                sharedPreferences.edit().putString("modeProfile", "manual").apply()
            } else {
                sharedPreferences.edit().putString("mode", "manual").apply()
            }
            binding.calendar.visibility = View.VISIBLE
            binding.textManuel.visibility = View.VISIBLE
            binding.btnArroserPlantes.visibility = View.VISIBLE
            binding.btnArroserPlantes.setOnClickListener {
                activePump()
            }
        }
        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                pumpMode("automatic")
                if (profileConnected) {
                    sharedPreferences.edit().putString("modeProfile", "automatic").apply()
                } else {
                    sharedPreferences.edit().putString("mode", "automatic").apply()
                }
                binding.calendar.visibility = View.GONE
                binding.textManuel.visibility = View.GONE
                binding.btnArroserPlantes.visibility = View.GONE
            } else {
                pumpMode("manual")
                if (profileConnected) {
                    sharedPreferences.edit().putString("modeProfile", "manual").apply()
                } else {
                    sharedPreferences.edit().putString("mode", "manual").apply()
                }
                binding.calendar.visibility = View.VISIBLE
                binding.textManuel.visibility = View.VISIBLE
                binding.btnArroserPlantes.visibility = View.VISIBLE
                binding.btnArroserPlantes.setOnClickListener {
                    activePump()
                }
            }
        }

        binding.btnReinitialiser.setOnClickListener {
            val daysOfWeek = resources.getStringArray(R.array.days_of_week)
            for (day in daysOfWeek) {
                if (profileConnected) {
                    sharedPreferences.edit().putString("waterPlanProfile $selectedDay", null)
                        .apply()
                } else {
                    sharedPreferences.edit().putString("waterPlan $selectedDay", null).apply()
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
                scheduledPump(date, "")
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
                    val timeProfile =
                        sharedPreferences.getString("waterPlanProfile $selectedDay", "")
                    binding.textSelectedTime.text = timeProfile
                } else {
                    val time = sharedPreferences.getString("waterPlan $selectedDay", "")
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
                        sharedPreferences.getString("waterPlanProfile $selectedDay", "")
                    scheduledPump(date, timeProfile!!)
                    if (timeProfile == "") {
                        binding.btnReinitialiser.visibility = View.GONE
                    } else {
                        binding.btnReinitialiser.visibility = View.VISIBLE
                    }
                } else {
                    val time = sharedPreferences.getString("waterPlan $selectedDay", "")
                    scheduledPump(date, time!!)
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
    }

    fun activePump() {
        val activePumpRequest = ActivePumpRequest("on")
        val requestBody =
            Gson().toJson(activePumpRequest).toRequestBody("application/json".toMediaType())
        val call = apiService.activePump(requestBody)
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

    fun pumpMode(value: String) {
        val modePumpRequest = ModePumpRequest(value)
        val requestBody =
            Gson().toJson(modePumpRequest).toRequestBody("application/json".toMediaType())
        val call = apiService.pumpMode(requestBody)
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
            .setTitleText("Choisissez l'heure d'arrosage")
            .build()
        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            if (profileConnected) {
                sharedPreferences.edit().putString("waterPlanProfile $selectedDay", selectedTime)
                    .apply()
            } else {
                sharedPreferences.edit().putString("waterPlan $selectedDay", selectedTime).apply()
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
            scheduledPump(date, selectedTime)
        }
        timePicker.addOnNegativeButtonClickListener {}
        timePicker.show(requireActivity().supportFragmentManager, "timePicker")
    }

    fun scheduledPump(value: DayOfWeek, time: String) {
        val scheduledPumpRequest = when (value) {
            DayOfWeek.lundi -> ScheduledPumpResponse(
                mapOf(
                    DayOfWeek.lundi to time,
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.mardi -> ScheduledPumpResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to time,
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.mercredi -> ScheduledPumpResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to time,
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.jeudi -> ScheduledPumpResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to time,
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.vendredi -> ScheduledPumpResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to time,
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.samedi -> ScheduledPumpResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to time,
                    DayOfWeek.dimanche to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile dimanche", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan dimanche", "")!!
                    }
                )
            )

            DayOfWeek.dimanche -> ScheduledPumpResponse(
                mapOf(
                    DayOfWeek.lundi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile lundi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan lundi", "")!!
                    },
                    DayOfWeek.mardi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mardi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mardi", "")!!
                    },
                    DayOfWeek.mercredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile mercredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan mercredi", "")!!
                    },
                    DayOfWeek.jeudi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile jeudi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan jeudi", "")!!
                    },
                    DayOfWeek.vendredi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile vendredi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan vendredi", "")!!
                    },
                    DayOfWeek.samedi to if (profileConnected) {
                        sharedPreferences.getString("waterPlanProfile samedi", "")!!
                    } else {
                        sharedPreferences.getString("waterPlan samedi", "")!!
                    },
                    DayOfWeek.dimanche to time
                )
            )
        }
        Log.d("Activity", "${Gson().toJson(scheduledPumpRequest)}")
        val requestBody =
            Gson().toJson(scheduledPumpRequest).toRequestBody("application/json".toMediaType())
        val call = apiService.scheduledPump(requestBody)
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

}