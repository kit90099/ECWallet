package com.bc.ecwallet.utils

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bc.ecwallet.SignUpPagerFragment

class SignUpPagerFragmentAdapter(fragment: Fragment):FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int =9

    override fun createFragment(position: Int): Fragment =
        SignUpPagerFragment(position)

}