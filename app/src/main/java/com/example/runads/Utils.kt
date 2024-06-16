package com.example.runads

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi

class Utils {
    companion object
    {
        @RequiresApi(Build.VERSION_CODES.M)
        fun isConnectedInternet(context: Context,network: Network):Boolean
        {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = manager.getNetworkCapabilities(network)
            if (
                networkCapabilities != null && networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                )
            ) {
                return true
            } else if (networkCapabilities != null && networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            ) {
                return true
            }
            return false
        }
    }

}