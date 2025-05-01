package com.project.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.content.Context
import android.widget.Toast


class SettingsFragment : Fragment() {
    lateinit var developer : Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        developer = view.findViewById(R.id.kyd)

        developer.setOnClickListener {
            val url = "https://midhunkp.netlify.app"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
        }

        return view
    }

}
