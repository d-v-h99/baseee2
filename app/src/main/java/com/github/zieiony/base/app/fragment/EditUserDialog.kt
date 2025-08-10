package com.github.zieiony.base.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.zieiony.base.app.BaseDialogFragment
import com.github.zieiony.base.app.FragmentArgumentDelegate
import com.github.zieiony.base.app.R
import com.github.zieiony.base.app.data.User
import com.github.zieiony.base.app.databinding.DialogEditUserBinding

class EditUserDialog : BaseDialogFragment() {

    var user: User by FragmentArgumentDelegate("user")

    private var _binding: DialogEditUserBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditUserBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onColdStart() {
        binding.etName.setText(user.name)

        binding.btnSave.setOnClickListener {
            val newName = binding.etName.text.toString()
            onClickSave(newName)
        }

        binding.btnDelete.setOnClickListener {
            onClickDelete()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun onClickSave(newName: String) {
        val updated = user.copy(name = newName)
        setResult(ResultKeys.USER_UPDATED, updated)
        if (isAdded && dialog?.isShowing == true) {
            dismiss()
        } else {
            // Trì hoãn dismiss nếu cần
            view?.post {
                if (isAdded && dialog?.isShowing == true) {
                    dismiss()
                }
            }
        }
    }

    private fun onClickDelete() {
        setResult(ResultKeys.USER_DELETED, user.id)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(user: User) = EditUserDialog().apply {
            arguments = Bundle().apply {
                putSerializable("user", user)
            }
        }
    }
}
