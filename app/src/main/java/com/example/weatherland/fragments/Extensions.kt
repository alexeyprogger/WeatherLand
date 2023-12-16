package com.example.weatherland.fragments

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.isPermissionGranted(p: String): Boolean {
    return ContextCompat.checkSelfPermission(activity as AppCompatActivity, p) == PackageManager.PERMISSION_GRANTED
} //потому что если разрешение уже было, нет смысла спрашивать каждый раз