package com.github.zieiony.base.navigation

import android.content.Intent
import android.os.Parcel
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

//mô hình sự kiện điều hướng (event) gói gọn mọi “hành động điều hướng” thành các kiểu dữ liệu an toàn để đưa qua hàng đợi, state, hay lưu/khôi phục
// sealed class: giới hạn các kiểu con trong cùng file → when(event) sẽ exhaustive (không cần else)..
internal sealed class NavigationEvent : Serializable {
    //Điều hướng tới một Fragment theo tên class + arguments (map các giá trị Serializable?).
    internal class FragmentNavigationEvent(
        val className: String,
        val arguments: HashMap<String, Serializable?>? = null
    ) : NavigationEvent()
    //Điều hướng bằng Intent.
    //Intent không Serializable, nên tác giả tự định nghĩa 2 hàm đặc biệt của Java Serialization:
    internal class IntentNavigationEvent(var intent: Intent) : NavigationEvent() {

        @Throws(Exception::class)
        //ghi mảng byte vào ObjectOutputStream.
        private fun writeObject(oos: ObjectOutputStream) {
            val parcel = Parcel.obtain()
            intent.writeToParcel(parcel, 0)
            oos.write(parcel.marshall())
            parcel.recycle()
        }

        @Throws(Exception::class)
        private fun readObject(ois: ObjectInputStream) {
            //Đọc lại bytes từ ObjectInputStream.readBytes(), Parcel.unmarshall(...), rồi Intent.CREATOR.createFromParcel(parcel) → khôi phục intent.
            val bytes = ois.readBytes()
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            intent = Intent.CREATOR.createFromParcel(parcel)//Đây là “cầu nối” Serializable ⇄ Parcelable (Intent vốn Parcelable).
            parcel.recycle()
        }

    }
    //singleton (instance duy nhất) ngay khi được load.
    internal object BackNavigationEvent : NavigationEvent() //Singleton quay lại (back). Không cần dữ liệu kèm theo.

    internal class ResultNavigationEvent(val key: String, val result: Serializable?) ://Trả kết quả kiểu key–value (giá trị Serializable?).
        NavigationEvent()

    internal class ResultNavigationEvent2(val result: Result) : NavigationEvent()//Trả kết quả gói trong Result(target, key, value) (đã có target để định tuyến tới navigator đích).
}
/* | Modifier    | Truy cập được từ                                      | Android ví dụ                                 |
| ----------- | ----------------------------------------------------- | --------------------------------------------- |
| `public`    | Mọi nơi                                               | Class/method bạn muốn dùng ở module khác      |
| `internal`  | Trong cùng module                                     | Class helper chỉ dùng nội bộ app hoặc library |
| `protected` | Class con và cùng package (Java) / class con (Kotlin) | Method dùng trong class cha và class con      |
| `private`   | Trong cùng file hoặc class                            | Biến helper chỉ dùng nội bộ file hoặc class   |

sealed class là gì?
Là class đặc biệt trong Kotlin cho phép bạn giới hạn các lớp con (subclass) của nó.

Các lớp con phải được khai báo:

Trong cùng file với lớp sealed đó (nhưng có thể khác nhau về package).

Ý nghĩa: bạn kiểm soát được hết các kiểu con có thể xuất hiện.
. Tại sao cần sealed class?
Khi bạn xử lý logic when(...) với một sealed class, Kotlin sẽ kiểm tra tại compile-time:

Nếu bạn liệt kê hết tất cả các subclass → không cần else.

Nếu thiếu case nào → báo lỗi compile.

💡 Giúp code an toàn kiểu (type safety) và rõ ràng.

| Loại class       | Có thể kế thừa từ ngoài file không? | Dùng để…                                                |
| ---------------- | ----------------------------------- | ------------------------------------------------------- |
| `open class`     | Có                                  | Cho phép kế thừa tự do                                  |
| `final class`    | Không                               | Ngăn kế thừa                                            |
| `abstract class` | Có                                  | Làm base cho các class khác, không tạo object trực tiếp |
| `sealed class`   | Không (chỉ trong cùng file)         | Giới hạn kiểu con, giúp `when` exhaustive               |

*/