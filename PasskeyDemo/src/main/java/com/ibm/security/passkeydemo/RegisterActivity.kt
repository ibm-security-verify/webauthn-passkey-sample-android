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
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import com.ibm.security.relyingpartysdk.RelyingPartyClient
import com.ibm.security.relyingpartysdk.model.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.net.URL

class RegisterActivity : AppCompatActivity() {

    private val TAG = Constants.TAG
    private val relyingPartyClient = RelyingPartyClient((URL(Constants.SERVER)))
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var nickName = "My Passkey"

    private lateinit var token: Token
    private lateinit var credentialManager: CredentialManager
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_register)

        credentialManager = CredentialManager.create(this@RegisterActivity)
        sharedPreferences = getSharedPreferences(
            Constants.SHARED_PREFERENCES,
            Context.MODE_PRIVATE
        )

        sharedPreferences.getString(Constants.SHARED_PREF_TOKEN, null)?.let {
            token = Json.decodeFromString(it)
        }

        findViewById<Button>(R.id.button_register).setOnClickListener {
            onClickRegister()
        }
    }

    private fun onClickRegister() {

        nickName = findViewById<EditText>(R.id.editTextTextNickname).text.toString()

        coroutineScope.async {
            relyingPartyClient.challengeAttestation(
                displayName = nickName,
                token = token
            )
                .onSuccess {
                    Log.d(TAG, "Success: $it")
                    createPasskey(it)?.let {  response ->
                        val responseJson = JSONObject(response.registrationResponseJson)
                        val clientDataJSON =
                            responseJson.getJSONObject("response").getString("clientDataJSON")
                        val attestationObject =
                            responseJson.getJSONObject("response").getString("attestationObject")
                        val id = responseJson.getString("id")

                        Log.d(
                            TAG,
                            "PublicKeyCredentialResponse: ${response.registrationResponseJson}"
                        )
                        relyingPartyClient.register(
                            nickName,
                            clientDataJSON,
                            attestationObject,
                            id,
                            token
                        )
                            .onSuccess {
                                Log.d(TAG, "Registration successfully completed")
                                startActivity(
                                    Intent(
                                        this@RegisterActivity,
                                        LoginActivity::class.java
                                    )
                                )
                            }
                            .onFailure { throwable ->
                                Log.d(TAG, "Registration failed with: " + throwable.message)
                            }
                    }
                }
                .onFailure {
                    Log.d(TAG, "Failure: ${it.message}")
                }
        }
    }

    private suspend fun createPasskey(credentialRegistrationOptions: String): CreatePublicKeyCredentialResponse? {

        val t = credentialRegistrationOptions.replace(
            "\"authenticatorSelection\":{}",
            "\"authenticatorSelection\":{\"authenticatorAttachment\": \"platform\",\"residentKey\": \"required\"}"
        )
        var response: CreatePublicKeyCredentialResponse? = null
        try {
            val createPublicKeyCredentialRequest =
                CreatePublicKeyCredentialRequest(t)
            response = credentialManager.createCredential(
                createPublicKeyCredentialRequest,
                this@RegisterActivity
            ) as CreatePublicKeyCredentialResponse
        } catch (e: Exception) {
            Log.e(TAG, "Failure: $e")
        }
        return response
    }
}