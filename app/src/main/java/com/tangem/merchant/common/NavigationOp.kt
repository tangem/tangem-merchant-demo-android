package com.tangem.merchant.common

import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.tangem.merchant.R

/**
 * Created by Anton Zhilenkov on 10.03.2020.
 */
fun getDefaultNavigationOptions(): NavOptions {
    return navOptions {
        anim {
            enter = R.anim.slide_in_right
            exit = R.anim.slide_out_left
            popEnter = R.anim.slide_in_left
            popExit = R.anim.slide_out_right
        }
    }
}