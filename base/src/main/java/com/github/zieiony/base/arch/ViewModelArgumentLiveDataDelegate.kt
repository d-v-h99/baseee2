package com.github.zieiony.base.arch

import androidx.lifecycle.MutableLiveData
import com.github.zieiony.base.arch.UniqueMutableLiveData.NotificationMode
import com.github.zieiony.base.util.Value
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
//à property delegate để tạo LiveData gắn với arguments + tự lưu state cho mỗi thuộc tính trong BaseViewModel, và chỉ cho đọc (ReadOnlyProperty) – tức là bạn không set lại delegate, mà set qua liveData.value.
class ViewModelArgumentLiveDataDelegate<T : Serializable?> :
    ReadOnlyProperty<BaseViewModel, MutableLiveData<T>> {

    private val notificationMode: NotificationMode

    private var initialValue: Value<T>? = null

    constructor(notificationMode: NotificationMode = NotificationMode.UNIQUE) {
        this.notificationMode = notificationMode
    }

    constructor(
        initialValue: T,
        notificationMode: NotificationMode = NotificationMode.UNIQUE
    ) : this(notificationMode) {
        this.initialValue = Value(initialValue)
    }

    override fun getValue(
        thisRef: BaseViewModel,
        property: KProperty<*>
    ): UniqueMutableLiveData<T> {
        val key = property.name
        thisRef.liveDatas[key]?.let {
            return it as UniqueMutableLiveData<T>
        }
        val liveData = SavingStateLiveData(
            thisRef.arguments,
            key,
            thisRef.arguments.get(key) as T,
            notificationMode
        )
        initialValue?.let {
            liveData.setValue(it.value)
        }
        thisRef.liveDatas[key] = liveData as UniqueMutableLiveData<Serializable>
        return liveData
    }
}