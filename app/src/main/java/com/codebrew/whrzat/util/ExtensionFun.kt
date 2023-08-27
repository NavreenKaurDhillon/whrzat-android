package com.codebrew.whrzat.util

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.codebrew.whrzat.R

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
    
    val fragmentTransaction = beginTransaction().setCustomAnimations(R.anim.enter_from_right,
            R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
    fragmentTransaction.func()
    fragmentTransaction.addToBackStack(null)
    fragmentTransaction.commit()
}