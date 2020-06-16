package com.example.weather_rabbit

import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.*

class DisplayInitializer(val frag: HomeFragment) {
    fun init() {
        val calendar = Calendar.getInstance()
        val dateStr = calendar.get(Calendar.YEAR)
            .toString() + " " + calendar.get(Calendar.MONTH) + " " + calendar.get(Calendar.DAY_OF_MONTH)

        val dateText = frag.activity?.findViewById<TextView>(R.id.date_text)
        dateText?.text = dateStr
    }
}

