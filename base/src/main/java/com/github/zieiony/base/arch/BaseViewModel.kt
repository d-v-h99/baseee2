package com.github.zieiony.base.arch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.cancel
import java.io.Serializable

open class BaseViewModel : ViewModel() {
    val arguments = Bundle()//Chỗ chứa tham số khởi tạo/state tạm cho ViewModel (giống SavedStateHandle dạng thô).

    internal val liveDatas = HashMap<String, UniqueMutableLiveData<out Serializable>?>()

    private val disposables = CompositeDisposable() //Gom tất cả RxJava Disposable để dọn dẹp khi ViewModel bị hủy.

    private fun addDisposable(disposable: Disposable) = disposables.add(disposable)

    protected fun Disposable.disposeOnCleared() {
        addDisposable(this)
    }

    open fun init(bundle: Bundle? = null) {
        if (bundle != null)
            arguments.putAll(bundle)
        //Nếu có bundle truyền vào → merge vào arguments.
        //Gọi onInit(arguments) cho subclass override.

        onInit(arguments)
    }

    open fun onInit(bundle: Bundle) {
    }

    open fun saveState(bundle: Bundle) {
        bundle.putAll(arguments)

        onSaveState(bundle)
    }

    open fun onSaveState(bundle: Bundle) {
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        viewModelScope.coroutineContext.cancel()
    }

}
