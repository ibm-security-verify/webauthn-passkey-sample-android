/*
 * Copyright contributors to the IBM Security Verify Sample App for Passkey on Android
 */

package com.ibm.security.passkeydemo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class WelcomeActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_welcome)

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)

        sharedPreferences.getString(Constants.SHARED_PREF_DISPLAY_NAME, null)?.let {
            findViewById<TextView>(R.id.textView).text = "Welcome, $it"
        }

        findViewById<Button>(R.id.button_logout).setOnClickListener {
            onClickLogout()
        }

        findViewById<Button>(R.id.button_reset).setOnClickListener {
            onClickReset()
        }
    }

    private fun onClickLogout() {
        startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
    }

    private fun onClickReset() {
        sharedPreferences.edit().remove(Constants.SHARED_PREF_TOKEN).apply()
        sharedPreferences.edit().remove(Constants.SHARED_PREF_DISPLAY_NAME).apply()
        startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
    }
}