package com.github.zieiony.base.arch

import android.os.Bundle
import com.github.zieiony.base.navigation.DeferredNavigator
import com.github.zieiony.base.navigation.Navigator

open class BaseNavigatorViewModel : BaseViewModel() {
    private val _navigator = DeferredNavigator()
    protected val navigator: Navigator = _navigator

    fun init(bundle: Bundle? = null, navigator: Navigator? = null) {
        super.init(bundle)

        _navigator.restoreState(arguments)
        _navigator.navigator = navigator
    }

    override fun saveState(bundle: Bundle) {
        _navigator.saveState(bundle)

        super.saveState(bundle)
    }

    override fun onCleared() {
        _navigator.navigator = null

        super.onCleared()
    }
}
