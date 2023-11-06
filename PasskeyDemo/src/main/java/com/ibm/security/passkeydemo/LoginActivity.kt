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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import com.ibm.security.relyingpartysdk.RelyingPartyClient
import com.ibm.security.relyingpartysdk.model.AuthenticationMethod
import com.ibm.security.relyingpartysdk.model.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.net.URL

class LoginActivity : AppCompatActivity() {

    private val TAG = "Passkey Demo"
    private val relyingPartyClient = RelyingPartyClient((URL(Constants.SERVER)))
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var credentialManager: CredentialManager
    private lateinit var nickName: String
    private lateinit var token: Token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences(
            Constants.SHARED_PREFERENCES,
            Context.MODE_PRIVATE
        )

        sharedPreferences.getString(Constants.SHARED_PREF_TOKEN, null)?.let {
            token = Json.decodeFromString(it)
        }

        sharedPreferences.getString(Constants.SHARED_PREF_DISPLAY_NAME, null)?.let {
            nickName = it
        }

        credentialManager = CredentialManager.create(this@LoginActivity)

        findViewById<Button>(R.id.button_login).setOnClickListener {
            onClickLogin()
        }

        findViewById<Button>(R.id.button_reset2).setOnClickListener {
            onClickReset()
        }
    }

    private fun onClickLogin() {

        coroutineScope.async {
            relyingPartyClient.challengeAssertion(
                displayName = nickName,
                token = token
            )
                .onSuccess { it ->
                    Log.d(TAG, "Success: $it")
                    signInWithPasskey(it)?.let { response ->
                        Log.d(TAG, "Result: " + response.authenticationResponseJson)
                        val responseJson = JSONObject(response.authenticationResponseJson)
                        val signature =
                            responseJson.getJSONObject("response").getString("signature")
                        val clientDataJSON =
                            responseJson.getJSONObject("response").getString("clientDataJSON")
                        val authenticatorData =
                            responseJson.getJSONObject("response").getString("authenticatorData")
                        val credentialId = responseJson.getString("id")
                        val userId = responseJson.getJSONObject("response").getString("userHandle")

                        relyingPartyClient.signin(
                            signature,
                            clientDataJSON,
                            authenticatorData,
                            credentialId,
                            userId
                        )
                            .onSuccess {
                                Log.d(TAG, "Success: $it")
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        WelcomeActivity::class.java
                                    )
                                )
                            }
                            .onFailure { throwable ->
                                Log.d(TAG, "Failure: " + throwable.message)
                            }
                    }
                }
                .onFailure {
                    Log.d(TAG, "Failure: ${it.message}")
                }
        }
    }

    private fun onClickReset() {
        sharedPreferences.edit().remove(Constants.SHARED_PREF_TOKEN).apply()
        sharedPreferences.edit().remove(Constants.SHARED_PREF_DISPLAY_NAME).apply()
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }


    private suspend fun signInWithPasskey(publicKeyCredentialOption: String): PublicKeyCredential? {
        var response: PublicKeyCredential? = null
        try {
            val getPublicKeyCredentialOption =
                GetPublicKeyCredentialOption(publicKeyCredentialOption, null, true)
            response = credentialManager.getCredential(
                GetCredentialRequest(
                    listOf(
                        getPublicKeyCredentialOption
                    )
                ), this@LoginActivity
            ).credential as PublicKeyCredential
        } catch (e: Exception) {
            Log.e(TAG, "Failure: $e")
        }
        return response
    }
}