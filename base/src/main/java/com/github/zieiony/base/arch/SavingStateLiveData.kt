package com.github.zieiony.base.arch

import android.os.Bundle
import java.io.Serializable
//LiveData “tự lưu state vào Bundle”
internal class SavingStateLiveData<T : Serializable?>(
    private var bundle: Bundle?,
    private var key: String,
    value: T?,
    notificationMode: NotificationMode = NotificationMode.UNIQUE
) : UniqueMutableLiveData<T>(value, notificationMode) {

    override fun setValue(value: T?) {
        //Mỗi lần setValue(value):
        //
        //Nếu có bundle → ghi ngay bundle.putSerializable(key, value)
        //
        //Gọi super.setValue(value) → phát tới observer (và áp dụng lọc trùng nếu UNIQUE).
        if (bundle != null) {
            bundle!!.putSerializable(key, value)
        }
        super.setValue(value)
        // Tức là đồng bộ LiveData ↔ Bundle theo key. Hữu ích khi bạn muốn giữ state qua xoay màn hình/process death mà không dùng SavedStateHandle.
    }
    //Serializable chậm hơn Parcelable; nếu value đổi thường xuyên hoặc là list lớn, cân nhắc dùng Parcelable (và 1 lớp LiveData tương tự cho Parcelable).
    //
    //bundle có thể là null → khi đó chỉ phát LiveData, không ghi state.
    //
    //postValue() cuối cùng cũng dẫn đến setValue() trên main thread ⇒ vẫn được lưu vào Bundle.
    //
    //Với kiểu mutable (ví dụ MutableList), nếu bạn chỉnh sửa tại chỗ mà không gán object mới, UNIQUE có thể không phát lại (vì equals có thể coi là như cũ). Khi cần, hãy gán bản sao mới.
}