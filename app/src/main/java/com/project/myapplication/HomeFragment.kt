package com.project.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView


class HomeFragment : Fragment() {
    lateinit var dialview: TextView
    lateinit var d1: Button
    lateinit var d2: Button
    lateinit var d3: Button
    lateinit var d4: Button
    lateinit var d5: Button
    lateinit var d6: Button
    lateinit var d7: Button
    lateinit var d8: Button
    lateinit var d9: Button
    lateinit var ac_dial: Button
    lateinit var d0: Button
    lateinit var backspace: Button
    lateinit var dialnow: Button
    private var dialNumber = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dialview = view.findViewById(R.id.dialview)
        d1 = view.findViewById(R.id.d1)
        d2 = view.findViewById(R.id.d2)
        d3 = view.findViewById(R.id.d3)
        d4 = view.findViewById(R.id.d4)
        d5 = view.findViewById(R.id.d5)
        d6 = view.findViewById(R.id.d6)
        d7 = view.findViewById(R.id.d7)
        d8 = view.findViewById(R.id.d8)
        d9 = view.findViewById(R.id.d9)
        ac_dial = view.findViewById(R.id.ac_dial)
        d0 = view.findViewById(R.id.d0)
        backspace = view.findViewById(R.id.backspace)
        dialnow = view.findViewById(R.id.dialnow)




        d1.setOnClickListener{
            dialNumber+="1"
            dialview.text = dialNumber
        }
        d2.setOnClickListener{
            dialNumber+="2"
            dialview.text = dialNumber
        }
        d3.setOnClickListener{
            dialNumber+="3"
            dialview.text = dialNumber
        }
        d4.setOnClickListener{
            dialNumber+="4"
            dialview.text = dialNumber
        }
        d5.setOnClickListener{
            dialNumber+="5"
            dialview.text = dialNumber
        }
        d6.setOnClickListener{
            dialNumber+="6"
            dialview.text = dialNumber
        }
        d7.setOnClickListener{
            dialNumber+="7"
            dialview.text = dialNumber
        }
        d8.setOnClickListener{
            dialNumber+="8"
            dialview.text = dialNumber
        }
        d9.setOnClickListener{
            dialNumber+="9"
            dialview.text = dialNumber
        }
        d0.setOnClickListener{
            dialNumber+= "0"
            dialview.text = dialNumber
        }
        ac_dial.setOnClickListener {
            dialNumber = ""
            dialview.text = dialNumber
        }
        backspace.setOnClickListener {
            if (dialNumber.isNotEmpty()) {
                dialNumber = dialNumber.substring(0, dialNumber.length - 1)
                dialview.text = dialNumber
            }
        }
        dialnow.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$dialNumber")
            startActivity(intent)
        }
        return view
    }

}