package com.github.zieiony.base.app.fragment

import android.os.Bundle
import android.view.View
import com.github.zieiony.base.app.BaseFragment
import com.github.zieiony.base.app.FragmentArgumentDelegate
import com.github.zieiony.base.app.R
import com.github.zieiony.base.app.data.User
import com.github.zieiony.base.app.databinding.FragmentDetailBinding
import java.io.Serializable

class DetailFragment : BaseFragment() {

    override val layoutId get() = R.layout.fragment_detail

    // Nhận arg bằng delegate (key = tên biến)
    var user: User by FragmentArgumentDelegate()

    private lateinit var binding: FragmentDetailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailBinding.bind(view)

        binding.tvUserName.text = "ID: ${user.id} - Name: ${user.name}"
        binding.btnBack.setOnClickListener {
            navigateBack()
        }

        // Có thể thêm các button sửa / xoá tại đây...
    }
    override fun onColdStart() {
        val dialog = EditUserDialog.newInstance(user)
        dialog.resultTarget = navigatorId
        dialog.show(childFragmentManager, "edit_dialog")
    }
    override fun onResult(key: String, result: Serializable?): Boolean {
        return when (key) {
            ResultKeys.USER_UPDATED -> {
                val updated = result as? User ?: return false
                setResult(ResultKeys.USER_UPDATED, updated)
                navigateBack() // quay về MainFragment
                true
            }
            ResultKeys.USER_DELETED -> {
                val deletedId = result as? Int ?: return false
                setResult(ResultKeys.USER_DELETED, deletedId)
                navigateBack()
                true
            }
            else -> false
        }
    }
    override fun onResume() {
        super.onResume()
        for (result in results) {
            if (result.target == navigatorId && onResult(result.key, result.value))
                clearResult(result)
        }
    }





    companion object {
        fun newInstance(user: User): DetailFragment {
            val f = DetailFragment()
            f.user = user
            return f
        }
    }
}
