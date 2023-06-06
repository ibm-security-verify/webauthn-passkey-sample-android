/*
 * Copyright contributors to the IBM Security Verify Sample App for Passkey on Android
 */

package com.ibm.security.passkeydemo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ibm.security.relyingpartysdk.NetworkHelper
import com.ibm.security.relyingpartysdk.RelyingPartyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URL


class MainActivity : AppCompatActivity() {

    private val TAG = Constants.TAG

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val relyingPartyClient = RelyingPartyClient(URL(Constants.SERVER))

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(
            Constants.SHARED_PREFERENCES,
            Context.MODE_PRIVATE
        )

        sharedPreferences.getString(Constants.SHARED_PREF_TOKEN, null)?.let {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.button_signin).setOnClickListener {
            onClickSignIn()
        }
    }

    private fun onClickSignIn() {

        var userName = findViewById<EditText>(R.id.editTextTextUsername).text.toString()
        var password = findViewById<EditText>(R.id.editTextTextPassword).text.toString()

        coroutineScope.async {
            relyingPartyClient.authenticate(
                userName, password
            ).onSuccess { token ->
                token.let {
                    Log.d(TAG, "Success: $token")
                    sharedPreferences.edit()
                        ?.putString(Constants.SHARED_PREF_TOKEN, Json.encodeToString(token))
                        ?.apply()

                    token.idToken?.let {
                        sharedPreferences.edit().putString(
                            Constants.SHARED_PREF_DISPLAY_NAME,
                            Constants.getDisplayName(it)
                        )?.apply()
                    }

                    startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
                }
            }.onFailure {
                Log.d(TAG, "Failure: ${it.message}")
            }
        }
    }
}