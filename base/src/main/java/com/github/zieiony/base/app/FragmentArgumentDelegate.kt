package com.github.zieiony.base.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.zieiony.base.util.Value
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
//để bạn đọc/ghi arguments của Fragment theo tên biến — gọn hơn rất nhiều so với tự thao tác Bundle.
class FragmentArgumentDelegate<T : Serializable>(
    private val key: String? = null
) : ReadWriteProperty<Fragment, T> {

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val args = thisRef.arguments
            ?: throw IllegalStateException("Fragment ${thisRef::class.java.simpleName} has no arguments")

        @Suppress("UNCHECKED_CAST")
        return args.getSerializable(key ?: property.name) as? T
            ?: throw IllegalStateException("Property ${property.name} could not be read from arguments")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        val args = thisRef.arguments ?: Bundle().also { thisRef.arguments = it }
        args.putSerializable(key ?: property.name, value)
    }
}