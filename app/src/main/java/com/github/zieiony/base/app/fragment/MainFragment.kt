package com.github.zieiony.base.app.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.zieiony.base.app.BaseFragment
import com.github.zieiony.base.app.R
import com.github.zieiony.base.app.adapter.UserAdapter
import com.github.zieiony.base.app.data.User
import com.github.zieiony.base.app.databinding.FragmentMainBinding
import java.io.Serializable

class MainFragment : BaseFragment() {

    override val layoutId get() = R.layout.fragment_main
    private lateinit var binding: FragmentMainBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)

        val adapter = UserAdapter(users) { user ->
            navigateTo(DetailFragment.newInstance(user))
        }

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
    }

    // Demo data
    private val users = listOf(
        User(1, "Alice"),
        User(2, "Bob"),
        User(3, "Charlie")
    )

    override fun onColdStart() {
//        navigateTo(DetailFragment.newInstance( User(2, "Bob"),))
        Toast.makeText(context, "Xin chao ngay moi", Toast.LENGTH_SHORT).show()
    }

    // Nhận result từ Detail/Edit
    override fun onResult(key: String, result: Serializable?): Boolean {
        when (key) {
            ResultKeys.USER_UPDATED -> {
                val updated = result as? User ?: return false
                // TODO: update UI/list, diff, v.v.
                Toast.makeText(context, updated.toString(), Toast.LENGTH_SHORT).show()
                return true
            }
            ResultKeys.USER_DELETED -> {
                val deletedId = result as? Int ?: return false
                println("MainFragment nhận USER_DELETED id=$deletedId")
                return true
            }
        }
        return false
    }

}

object ResultKeys {
    const val USER_UPDATED = "USER_UPDATED"
    const val USER_DELETED = "USER_DELETED"
}
