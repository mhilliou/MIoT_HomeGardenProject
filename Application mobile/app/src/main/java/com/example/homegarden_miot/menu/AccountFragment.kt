package com.example.homegarden_miot.menu

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.homegarden_miot.databinding.FragmentAccountBinding


class AccountFragment : Fragment(){

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private var connected: Boolean = false
    private lateinit var name: String
    private lateinit var firstName: String
    private lateinit var email: String
    private lateinit var phone: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        connected = sharedPreferences.getBoolean("connexion", false)
        Log.d("Activity", "connected: $connected")
        name = sharedPreferences.getString("name", "")!!
        firstName = sharedPreferences.getString("firstName", "")!!
        email = sharedPreferences.getString("email", "")!!
        phone = sharedPreferences.getString("phone", "")!!

        showPersonalInfo()

        binding.buttonLogin.setOnClickListener {
            val username = binding.textFieldName.editText.toString()
            val password = binding.textFieldPassword.editText.toString()
            if (checkCredentials(username, password)) {
                connected = true
                sharedPreferences.edit().putBoolean("connexion", true).apply()
                Log.d("Activity", "connected: $connected")
                showPersonalInfo()
            } else {
                Toast.makeText(requireContext(), "Identifiants incorrects", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonSave.setOnClickListener {
            name = binding.textFieldName.editText?.text.toString()
            firstName = binding.textFieldFirstName.editText?.text.toString()
            email = binding.textFieldEmail.editText?.text.toString()
            phone = binding.textFieldPhone.editText?.text.toString()
            update()
        }

        binding.buttonLogout.setOnClickListener {
            connected = false
            sharedPreferences.edit().putBoolean("connexion", false).apply()
            showPersonalInfo()
        }
    }

    private fun checkCredentials(username: String, password: String): Boolean {
        return true
    }

    private fun showPersonalInfo() {
        binding.apply {
            imageViewAccount.visibility = if (!connected) View.VISIBLE else View.GONE
            textFieldUserName.visibility = if (!connected) View.VISIBLE else View.GONE
            textFieldPassword.visibility = if (!connected) View.VISIBLE else View.GONE
            buttonLogin.visibility = if (!connected) View.VISIBLE else View.GONE
            grayZone.visibility = if(!connected) View.GONE else View.VISIBLE
            buttonLogout.visibility = if (!connected) View.GONE else View.VISIBLE
            imageViewProfile.visibility = if(!connected) View.GONE else View.VISIBLE
            textViewName.visibility = if(!connected) View.GONE else View.VISIBLE
            textViewFirstName.visibility = if(!connected) View.GONE else View.VISIBLE
            textFieldName.visibility = if(!connected) View.GONE else View.VISIBLE
            textFieldFirstName.visibility = if(!connected) View.GONE else View.VISIBLE
            textFieldEmail.visibility = if(!connected) View.GONE else View.VISIBLE
            textFieldPhone.visibility = if(!connected) View.GONE else View.VISIBLE
            buttonSave.visibility = if(!connected) View.GONE else View.VISIBLE

            binding.textViewName.text = name + " "
            binding.textViewFirstName.text = firstName
            textFieldName.editText?.setText(name)
            textFieldFirstName.editText?.setText(firstName)
            textFieldEmail.editText?.setText(email)
            textFieldPhone.editText?.setText(phone)
        }
    }

    private fun update() {
        binding.textViewName.text = name + " "
        binding.textViewFirstName.text = firstName
        binding.textFieldName.editText?.setText(name)
        binding.textFieldFirstName.editText?.setText(firstName)
        binding.textFieldEmail.editText?.setText(email)
        binding.textFieldPhone.editText?.setText(phone)

        sharedPreferences.edit().putString("name", name).apply()
        sharedPreferences.edit().putString("firstName", firstName).apply()
        sharedPreferences.edit().putString("email", email).apply()
        sharedPreferences.edit().putString("phone", phone).apply()
    }
}