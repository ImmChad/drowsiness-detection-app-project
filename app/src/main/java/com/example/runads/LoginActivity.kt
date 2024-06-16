package com.example.runads

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("com.example.runads", Context.MODE_PRIVATE)

        if(prefs.getString(PrefManager.KEY_PREF_DOMAIN_API, "").toString().trim().isEmpty() || prefs.getString(
                PrefManager.KEY_PREF_APP_ID,
                ""
            ).toString().trim().isEmpty()
        )
        {
//        request for hide statusBars
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.activity_login)
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            @Suppress("DEPRECATION")

//        hide statusBars
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {

                if(window.insetsController!=null)
                {
                    window.insetsController!!.hide(WindowInsets.Type.statusBars())
                }

            }
            else
            {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
                if(prefs.getString(PrefManager.KEY_PREF_DOMAIN_API, "").toString().trim().isEmpty())
                {

                    loadHandleSubmitDomainID()
                }
                else if(prefs.getString(PrefManager.KEY_PREF_APP_ID, "").toString().trim()
                        .isEmpty()
                )
                {
                    IApiService.changeBaseDomain(prefs.getString(PrefManager.KEY_PREF_DOMAIN_API,"").toString())

                    val containerDomainApi =findViewById<LinearLayout>(R.id.container_domain_api)
                    val containerAppId =findViewById<LinearLayout>(R.id.container_app_id)
                    containerDomainApi.visibility = View.GONE
                    containerAppId.visibility = View.VISIBLE
                    loadHandleClickSubmitAppID()
                }

        }
        else
        {
            finish()
            startActivity(Intent(applicationContext,MainActivity::class.java))
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration)
    {
        super.onConfigurationChanged(newConfig)
        println("config changed")

        when(newConfig.orientation)
        {
            (Configuration.ORIENTATION_LANDSCAPE)->{
                Log.d("tag","LANDSCAPE")
                println("LANDSCAPE")
            }
            else->
            {
                Log.d("tag","PORTRAIT")
                println("PORTRAIT")

            }
        }

    }

    private fun loadHandleClickSubmitAppID()
    {
        val btnSubmit =findViewById<Button>(R.id.btn_submit_app_id)
        val edtAppID =findViewById<EditText>(R.id.edt_app_ID)
        btnSubmit.setOnClickListener {
            finish()
            startActivity(Intent(applicationContext,MainActivity::class.java))
            prefs.edit().putString(PrefManager.KEY_PREF_APP_ID,edtAppID.text.toString()).apply()
        }
    }

    private fun loadHandleSubmitDomainID()
    {
        val containerDomainApi =findViewById<LinearLayout>(R.id.container_domain_api)
        val containerAppID =findViewById<LinearLayout>(R.id.container_app_id)
        containerAppID.visibility = View.GONE
        containerDomainApi.visibility = View.VISIBLE
        val btnSubmit =findViewById<Button>(R.id.btn_submit_domain_api)
        val edtDomainApi =findViewById<EditText>(R.id.edt_domain_api)
        loadHandleClickSubmitAppID()
        btnSubmit.setOnClickListener {

            if(URLUtil.isValidUrl(edtDomainApi.text.toString()))
            {   containerDomainApi.visibility = View.GONE
                containerAppID.visibility = View.VISIBLE
                prefs.edit().putString(PrefManager.KEY_PREF_DOMAIN_API,edtDomainApi.text.toString()).apply()
                IApiService.changeBaseDomain(prefs.getString(PrefManager.KEY_PREF_DOMAIN_API,"").toString())
            }
            else
            {
                Toast.makeText(applicationContext,"Domain Invalid!!!",Toast.LENGTH_SHORT).show()
            }

        }
    }
}