package com.example.runads

import android.content.*
import android.content.pm.ActivityInfo
import android.net.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.example.runads.PrefManager.Companion.KEY_PREF_DOMAIN_API
import android.content.Context
import android.os.Bundle

import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.runads.MainViewModel
import com.example.runads.fragment.CameraFragment
import com.example.runads.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences

    //    Check Network change state
    private val networkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback()
        {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onAvailable(network: Network) {
                applicationContext?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                if (Utils.isConnectedInternet(applicationContext, network)) {

                }
            }

            override fun onLost(network: Network) {
                // network unavailable
            }
        }

    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
//        request for hide statusBars
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
//        hide statusBars

        val containerBgDetectHuman = findViewById<RelativeLayout>(R.id.container_bg_detect_human)
        containerBgDetectHuman.rotation = 270F

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (window.insetsController != null) {
                window.insetsController!!.hide(WindowInsets.Type.statusBars())
                window.insetsController!!.hide(WindowInsetsCompat.Type.systemBars())
                window.insetsController!!.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        prefs = getSharedPreferences("com.example.runads", MODE_PRIVATE)
        IApiService.changeBaseDomain(prefs.getString(KEY_PREF_DOMAIN_API, "").toString())

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE


        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CameraFragment())
            .commit()
    }

    override fun onBackPressed() {
        finish()
    }

}