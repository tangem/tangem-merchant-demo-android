package com.tangem.merchant.application.ui.base.viewModel

import androidx.lifecycle.ViewModel
import com.tangem.merchant.application.domain.error.AppError
import com.tangem.merchant.common.SingleLiveEvent

/**
 * Created by Anton Zhilenkov on 15/07/2020.
 */
open class BaseVM: ViewModel() {
    val errorMessageSLE = SingleLiveEvent<AppError>()
}