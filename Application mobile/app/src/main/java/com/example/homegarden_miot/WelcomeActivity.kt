package com.example.homegarden_miot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.homegarden_miot.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private var _binding: ActivityWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttonAccederAuPotager: Button = findViewById(R.id.btnAccederPotager)

        buttonAccederAuPotager.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}