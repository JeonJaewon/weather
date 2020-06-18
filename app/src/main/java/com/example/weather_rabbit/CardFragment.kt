package com.example.weather_rabbit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_card.*
import kotlinx.android.synthetic.main.fragment_home.*

//
class CardFragment(val t3hValue: Double) : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        if (23.0 <= t3hValue) {
            wear_text.text = "반팔 티셔츠"
            wear_image.setImageResource(R.drawable.shirt2)
        } else if (20.0 <= t3hValue && t3hValue < 23.0) {
            wear_text.text = "긴팔 티셔츠"
            wear_image.setImageResource(R.drawable.shirt)
        } else if (17.0 <= t3hValue && t3hValue < 20.0) {
            wear_text.text = "후드티"
            wear_image.setImageResource(R.drawable.hoodie)
        } else if (12.0 <= t3hValue && t3hValue < 17.0) {
            wear_text.text = "자켓"
            wear_image.setImageResource(R.drawable.jacket)
        } else if (10.0 <= t3hValue && t3hValue < 12.0) {
            wear_text.text = "트렌치 코트"
            wear_image.setImageResource(R.drawable.coat4)
        } else if (6.0 <= t3hValue && t3hValue < 10) {
            wear_text.text = "코트"
            wear_image.setImageResource(R.drawable.coat)
        } else if (t3hValue < 6.0){
            wear_text.text = "패딩, 목도리 등 전부!"
            wear_image.setImageResource(R.drawable.thermometer_2)
        }
    }
}