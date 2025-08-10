package com.github.zieiony.base.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager


object Keyboard {
    fun show(view: View) {
        KeyboardRunnable(view).run()
    }

    fun hide(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)

        //as? (safe cast)
        //Nếu cast thành công → trả về object đã cast.
        //Nếu cast thất bại → trả về null thay vì ném lỗi.

        //let -> chi chay khi bien do khac null -> trả về Kết quả của lambda
        //also -> 	Thực hiện thao tác phụ (logging, debug, gán biến) -> trả ve object gốc
    }

    fun hide(window: Window) {
        window.currentFocus?.let {
            hide(it)
        }
    }
}

class KeyboardRunnable(var view: View) : Runnable {

    private var activity: Activity? = null

    init {
        var context = view.context
        //view.context có thể là:
        //Activity
        //ContextThemeWrapper
        //Hoặc một lớp con khác của ContextWrapper.
        while (context is ContextWrapper && context !is Activity)//Nếu context hiện tại là một lớp bọc (ContextWrapper) và không phải Activity, thì bóc lớp bọc ra.
            context = context.baseContext// lấy context gốc bên trong lớp bọc.
        //Vòng lặp này sẽ gỡ từng lớp ra cho tới khi gặp Activity. View → ContextThemeWrapper → Activity
        if (context is Activity)
            activity = context
        // contextWrapper
        //Tái sử dụng Context hiện có nhưng thêm hoặc thay đổi hành vi mà không phải viết lại toàn bộ Context.
        //Thường được dùng khi bạn muốn:
        //Thêm chức năng trước/sau khi gọi Context gốc.
        //Gắn thêm logic tùy biến cho một số method (getResources(), getSystemService(),…).
        //Tạo các subclass như ContextThemeWrapper, Service, Application, Activity… — đều kế thừa từ ContextWrapper.
    }

    override fun run() {
        if (activity == null)
            return
        //InputMethodManager Dùng để điều khiển bàn phím
        val inputMethodManager = activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        if (!(view.isFocusable && view.isFocusableInTouchMode)) {
            //Đảm bảo view có thể focus - view chưa được phép focus (nhận con trỏ nhập liệu):
            //Nếu view.isFocusable và view.isFocusableInTouchMode chưa bật → bật lên và return (sẽ cần chạy lại ở lần sau).
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            return
        } else if (!view.requestFocus()) {
            //Nếu cố gắng requestFocus() nhưng không thành công → gọi post() để thử lại sau 100ms.
            //iew chưa hiển thị hoàn toàn trên màn hình, nên chưa thể focus ngay.
            post()
        } else if (!inputMethodManager.isActive(view)) {
            //iew chưa hiển thị hoàn toàn trên màn hình, nên chưa thể focus ngay.
            post()
        } else if (!inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)) {
            //Thử mở bàn phím với showSoftInput().
            //
            //Nếu trả về false → nghĩa là bàn phím chưa mở được → retry.
            post()
        }
        //Nếu tất cả các bước trên thành công → đặt chế độ SOFT_INPUT_ADJUST_RESIZE để khi bàn phím mở, layout sẽ tự co lại, không bị bàn phím che mất.
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun post() {
        //Đẩy lại Runnable này vào Handler main thread sau 100ms.
        //
        //Giúp “thử lại” cho đến khi bàn phím thực sự mở (ví dụ khi UI chưa render xong hoặc chưa focus kịp).
        handler.postDelayed(this, INTERVAL_MS)
    }

    companion object {
        private val INTERVAL_MS = 100L
        private val handler = Handler(Looper.getMainLooper())
    }
    //Các phần logic chính (init, run(), các method khác) được đặt lên trước.
    //Các const, biến static, helper thì để cuối class.
    //Giúp đọc code theo thứ tự:
    //Constructor & logic chính → để dev đọc trước.
    //Biến tĩnh (companion object) → để sau, khi cần tra cứu.
}
