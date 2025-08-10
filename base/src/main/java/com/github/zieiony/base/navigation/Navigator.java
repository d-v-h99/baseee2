package com.github.zieiony.base.navigation;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public interface Navigator {
    default Navigator getParentNavigator() {
        return null;
    }
    //getParentNavigator() cho phép bọc lồng (child đẩy việc lên parent).
    //Lưu ý: mặc định trả null. Những class thật sự dùng default methods phải override để tránh NPE khi gọi các default method khác (vì nhiều method mặc định gọi tiếp lên parent).

    int getNavigatorId();

    void navigateTo(@NonNull Class<? extends Fragment> fragmentClass, HashMap<String, Serializable> arguments);

    void navigateTo(@NonNull Fragment fragment);

    void navigateTo(@NonNull Navigator originalNavigator, @NonNull Fragment fragment);//khi cần giữ ngữ cảnh navigator nguồn).


    void navigateTo(@NonNull Intent intent);

    void navigateBack();

    @NonNull
    default List<Result> getResults() {
        return getParentNavigator().getResults();
    }
    //ủy quyền lấy kết quả lên parent.
    //→ Root/top navigator sẽ giữ kho kết quả thực sự.

    default Result getResult(int navigatorId, @NonNull String key) {
        return getParentNavigator().getResult(navigatorId, key);
    }

    default void setResult(@NonNull String key, Serializable result) {
        getParentNavigator().setResult(new Result(getResultTarget(), key, result));
    }

    default void setResult(@NonNull Result result) {
        if (result.getTarget() != getNavigatorId() || !onResult(result.getKey(), result.getValue()))
            getParentNavigator().setResult(result);
    }

    default void clearResult(@NonNull Result result) {
        getParentNavigator().clearResult(result);
    }

    default boolean onResult(@NonNull String key, Serializable result) {
        return false;
    }

    void setResultTarget(int resultTarget);

    int getResultTarget();
    // trả kết quả result
    //Cha trước khi mở Con:
    //
    //child.setResultTarget(parent.getNavigatorId()).
    //
    //Con thực hiện xong:
    //
    //setResult("KEY", someSerializable)
    //→ Tạo Result(target = parentId, key, value) và đẩy lên parent.
    //
    //Trên đường đi lên:
    //
    //Mỗi Navigator so result.target với getNavigatorId().
    //
    //Tới đúng đích → gọi onResult(key, value). Nếu trả true, dừng; nếu false, đẩy tiếp.
}
