package com.github.zieiony.base.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.zieiony.base.app.fragment.MainFragment
import com.github.zieiony.base.arch.BaseNavigatorViewModel

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null)
            navigateTo(MainFragment())
    }

    override fun onNavigateTo(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(fragment::class.java.simpleName) // <- QUAN TRỌNG
            .commitAllowingStateLoss()
        return true
    }
}

//class MainViewModel : BaseNavigatorViewModel() {
//    fun loadData() {}
//}
//
//class MainFragment : BaseFragment() {
//
//    override val layoutId: Int
//        get() = R.layout.fragment_main
//
//    val viewModel by lazy { getViewModel(MainViewModel::class.java) }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        viewModel.loadData()
//    }
//}