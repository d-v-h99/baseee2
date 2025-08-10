package com.github.zieiony.base.app

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.zieiony.base.arch.BaseNavigatorViewModel
import com.github.zieiony.base.arch.BaseViewModel
import com.github.zieiony.base.navigation.Navigator
import java.io.Serializable
import java.lang.ref.WeakReference


abstract class BaseDialogFragment() : DialogFragment(), Navigator {

    open val layoutId: Int = INVALID_ID
    open val titleId: Int = INVALID_ID
    open val iconId: Int = INVALID_ID

    private var _navigatorId by FragmentArgumentDelegate<Int>()
    private var _resultTarget by FragmentArgumentDelegate<Int>()
    private val viewModels = mutableListOf<WeakReference<out BaseViewModel>>()

    var title: String? = null

    var icon: Drawable? = null

    private var parentNavigator: Navigator? = null

    private var coldStart = true

    init {
        _resultTarget = INVALID_ID
    }

    override fun getParentNavigator() = parentNavigator

    override fun getNavigatorId() = _navigatorId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null)
            arguments = Bundle()
        try {
            val id = _navigatorId
        } catch (e: IllegalStateException) {
            _navigatorId = BaseFragment.navigatorIdCounter++
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState != null)
            coldStart = false
        if (layoutId != INVALID_ID)
            return inflater.inflate(layoutId, container, false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        var parent = parentFragment
        while (parent != null) {
            if (parent is Navigator) {
                parentNavigator = parent
                break
            }
            parent = parent.parentFragment
        }
        if (parentNavigator == null && activity is Navigator)
            parentNavigator = activity as Navigator

        if (titleId != INVALID_ID)
            title = context.resources.getString(titleId)
        if (iconId != INVALID_ID)
            icon = context.resources.getDrawable(iconId)
    }

    open fun onColdStart() {
    }

    override fun onStart() {
        super.onStart()
        if (coldStart) {
            onColdStart()
            coldStart = false
        }
    }

    override fun onDetach() {
        super.onDetach()
        parentNavigator = null
    }

    override fun navigateTo(
        fragmentClass: Class<out Fragment?>,
        arguments: java.util.HashMap<String, Serializable?>?
    ) = navigateTo(instantiateFragment(fragmentClass, arguments))

    override fun navigateTo(fragment: Fragment) {
        if (!onNavigateTo(fragment))
            parentNavigator?.navigateTo(fragment)
    }

    override fun navigateTo(originalNavigator: Navigator, fragment: Fragment) {
        if (!onNavigateTo(fragment))
            parentNavigator?.navigateTo(originalNavigator, fragment)
    }

    override fun navigateTo(intent: Intent) {
        if (!onNavigateTo(intent))
            parentNavigator?.navigateTo(intent)
    }

    private fun instantiateFragment(
        fragmentClass: Class<out Fragment>,
        arguments: java.util.HashMap<String, Serializable?>?
    ): Fragment {
        val fragment = childFragmentManager.fragmentFactory.instantiate(
            fragmentClass.classLoader!!,
            fragmentClass.name
        )
        arguments?.let {
            val bundle = Bundle()
            fragment.arguments = bundle
            it.forEach { entry ->
                bundle.putSerializable(entry.key, entry.value)
            }
        }
        return fragment
    }

    open fun onNavigateTo(fragment: Fragment): Boolean {
        return false
    }

    open fun onNavigateTo(intent: Intent): Boolean {
        return false
    }

    override fun navigateBack() {
        if (!onNavigateBack())
            parentNavigator?.navigateBack()
    }

    open fun onNavigateBack(): Boolean {
        dismiss()
        return true
    }

    override fun setResultTarget(resultTarget: Int) {
        _resultTarget = resultTarget
    }

    override fun getResultTarget() = _resultTarget

    fun <T : ViewModel> getViewModel(c: Class<T>, factory: ViewModelProvider.Factory? = null): T {
        val viewModel = ViewModelProviders.of(this, factory).get("" + _navigatorId, c)
        if (viewModel is BaseViewModel) {
            val bundle = arguments?.getBundle(VIEWMODEL_STATE + c.canonicalName + _navigatorId)
            if (viewModel is BaseNavigatorViewModel) {
                viewModel.init(bundle, this)
            } else {
                viewModel.init(bundle)
            }
            viewModels.add(WeakReference(viewModel))
        }
        return viewModel
    }

    override fun onStop() {
        super.onStop()
        viewModels.forEach { weakReference ->
            val bundle = Bundle()
            weakReference.get()?.let {
                it.saveState(bundle)
                arguments?.putBundle(
                    VIEWMODEL_STATE + it.javaClass.canonicalName + _navigatorId,
                    bundle
                )
            }
        }
    }

    companion object {
        private const val INVALID_ID = 0
        private const val VIEWMODEL_STATE = "viewModelState"
    }
}
