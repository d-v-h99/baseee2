package com.github.zieiony.base.arch

import com.github.zieiony.base.util.Value
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
//để đọc/ghi argument của BaseViewModel vào Bundle arguments một cách “tự động” theo tên biến.
class ViewModelArgumentDelegate<T : Serializable?>() : ReadWriteProperty<BaseViewModel, T> {

    private var initialValue: Value<T>? = null

    constructor(initialValue: T) : this() {
        this.initialValue = Value(initialValue)
    }

    override fun getValue(thisRef: BaseViewModel, property: KProperty<*>): T {
        val key = property.name
        val arguments = thisRef.arguments
        if (!arguments.containsKey(key)) {
            if (property.returnType.isMarkedNullable)
                return null as T
            initialValue?.let {
                arguments.putSerializable(key, it.value)
                return it.value
            }
            throw java.lang.IllegalStateException("$key not present in arguments")
        }
        return arguments.get(key) as T
    }

    override fun setValue(thisRef: BaseViewModel, property: KProperty<*>, value: T) {
        thisRef.arguments.putSerializable(property.name, value)
    }
}