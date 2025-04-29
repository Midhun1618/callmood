package com.project.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class SwipeAdapter (f: FragmentActivity):FragmentStateAdapter(f) {
    override fun getItemCount()=2

        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> HomeFragment()
                1 -> SettingsFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
        fun getTabTitle(position: Int):String {
            return when (position) {
                0 -> "Dial"
                1 -> "Settings"
                else -> "ERROR404"
            }
        }

    }
