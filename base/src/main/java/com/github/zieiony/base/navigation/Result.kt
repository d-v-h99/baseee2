package com.github.zieiony.base.navigation

import java.io.Serializable


data class Result(val target: Int, val key: String, val value: Serializable?) : Serializable
//Truyền kết quả từ một màn hình (Activity/Fragment) về một màn hình khác.
//Đóng gói thông tin để xử lý điều hướng (navigation).
//Giống như một “bưu kiện” chứa:
//Người nhận (target)
//Loại dữ liệu (key)
//Giá trị (value)