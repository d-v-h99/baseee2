package com.github.zieiony.base.arch

import androidx.lifecycle.MutableLiveData
import java.lang.ref.WeakReference


open class UniqueMutableLiveData<T> : MutableLiveData<T> {
    //chống phát lại giá trị trùng (hoặc cho phép phát mọi lần tuỳ chế độ).
    enum class NotificationMode {
        // notify on all changes
        ALL,

        // notify on unique values only
        UNIQUE
    }

    val notificationMode: NotificationMode
    private var prevValue: WeakReference<T>? = null// giữ giá trị lần phát trước (dạng weak).

    constructor(notificationMode: NotificationMode = NotificationMode.UNIQUE) : super() {
        this.notificationMode = notificationMode
    }

    constructor(
        value: T?,
        notificationMode: NotificationMode = NotificationMode.UNIQUE
    ) : super(value) {
        this.notificationMode = notificationMode
    }

    override fun setValue(value: T?) {
        if (notificationMode == NotificationMode.ALL || value != prevValue?.get()) {
            super.setValue(value)
            prevValue = if (value == null) null else WeakReference(value)
            //: chỉ super.setValue() khi value khác prevValue?.get().
        }
    }
    // WeakReference?
    //Tránh giữ chặt (strong reference) tới object cũ → GC có thể dọn nếu không còn ai giữ.
    //
    //Khi GC dọn object cũ, prevValue?.get() sẽ trả null.
    //→ Nếu bạn set null lần nữa thì vẫn coi là “khác” (vì trước đó đã null), logic vẫn ổn.
    //=> ngăn UI render lại khi state không đổi.
}