package com.github.zieiony.base.app

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.github.zieiony.base.navigation.Navigator
import com.github.zieiony.base.navigation.Result
import java.io.Serializable


abstract class BaseActivity : AppCompatActivity(), Navigator {

    open val layoutId: Int = INVALID_ID
    open val titleId: Int = INVALID_ID
    open val iconId: Int = INVALID_ID

    private var _navigatorId: Int = INVALID_ID
    private var _resultTarget: Int = INVALID_ID //nơi (navigatorId) mà this sẽ gửi result tới nếu nó là “con” của ai đó (ít dùng tại Activity, hay dùng ở Fragment).

    var icon: Drawable? = null

    private val _results = ArrayList<Result>()//anh sách Result đang chờ xử lý (treo) của activity.

    private var coldStart = true// cờ để chạy onColdStart() 1 lần duy nhất ở lần onStart() đầu tiên.

    override fun getNavigatorId() = _navigatorId

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            savedInstanceState.getSerializable(NAVIGATOR_RESULT)?.let {
                _results.addAll(it as List<Result>)
            }
            _navigatorId = savedInstanceState.getInt(NAVIGATOR_ID)
        } else {
            _navigatorId = BaseFragment.navigatorIdCounter++
        }
        //Nếu rotate/restore: đọc lại _results và _navigatorId.
        //Nếu lần đầu: cấp phát ID mới từ BaseFragment.navigatorIdCounter++.
        //Set layout/title/icon nếu lớp con có override id.

        super.onCreate(savedInstanceState)

        if (layoutId != INVALID_ID)
            setContentView(layoutId)
        if (titleId != INVALID_ID)
            title = resources.getString(titleId)
        if (iconId != INVALID_ID)
            icon = resources.getDrawable(iconId)
    }

    open fun onColdStart() {//à hook chạy một lần (ví dụ điều hướng đến màn hình đầu tiên).
    }

    override fun onStart() {
        super.onStart()
        if (coldStart) {
            onColdStart()
            coldStart = false
        }
    }

    override fun onResume() {
        super.onResume()
        //hử giao lại các Result còn treo cho activity xử lý:
        //Nếu onResult(...) trả true ⇒ đã xử lý xong ⇒ xóa result đó.
        for (result in results) {
            if (onResult(result.key, result.value))
                clearResult(result)
        }
    }

    override fun navigateTo(
        fragmentClass: Class<out Fragment?>,
        arguments: java.util.HashMap<String, Serializable?>?
    ) = navigateTo(instantiateFragment(fragmentClass, arguments))

    override fun navigateTo(fragment: Fragment) {
        if (onNavigateTo(fragment)) {
            if (fragment is Navigator)
                fragment.resultTarget = navigatorId
        } else {
            parentNavigator?.navigateTo(this, fragment)
        }
    }

    override fun navigateTo(originalNavigator: Navigator, fragment: Fragment) {
        if (onNavigateTo(fragment)) {
            if (fragment is Navigator)
                fragment.resultTarget = originalNavigator.navigatorId
        } else {
            parentNavigator?.navigateTo(originalNavigator, fragment)
        }
    }

    override fun navigateTo(intent: Intent) {
        if (!onNavigateTo(intent))
            parentNavigator?.navigateTo(intent)
    }

    private fun instantiateFragment(
        fragmentClass: Class<out Fragment>,
        arguments: java.util.HashMap<String, Serializable?>?
    ): Fragment {
        //Dùng FragmentFactory.instantiate(...) + chuyển arguments (HashMap) sang Bundle.putSerializable(...) để gán cho fragment.arguments.
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            fragmentClass.classLoader!!,
            fragmentClass.name
        )
        arguments?.let {
            val bundle = Bundle()
            fragment.arguments = bundle
            it.forEach { entry ->
                bundle.putSerializable(entry.key, entry.value)
            }
        }
        return fragment
    }

    open fun onNavigateTo(fragment: Fragment): Boolean {
        if (fragment is DialogFragment) {
            fragment.show(supportFragmentManager, DIALOG_TAG)
            return true
        }
        return false
        //Mặc định: nếu là DialogFragment ⇒ show ngay và trả true.
        //
        //Nếu true (đã xử lý): và fragment cũng là Navigator ⇒ set resultTarget về ID của Activity này ⇒ fragment con khi setResult() sẽ bắn lên đúng đích.
        //
        //Nếu false: uỷ quyền cho parentNavigator (nếu có) xử lý tiếp (bubbling).
    }

    open fun onNavigateTo(intent: Intent): Boolean {
        startActivityForResult(intent, _navigatorId)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (data.hasExtra(NAVIGATOR_RESULT)) {
                val navigatorResult =
                    data.getSerializableExtra(NAVIGATOR_RESULT) as HashMap<String, Serializable>
                for (resultEntry in navigatorResult)
                    setResult(resultEntry.key, resultEntry.value)
            }
        }
    }

    override fun navigateBack() {
        onNavigateBack()
    }

    open fun onNavigateBack(): Boolean {
        onBackPressed()
        return true
    }

    override fun getResults(): List<Result> {
        return _results
    }

    override fun getResult(navigatorId: Int, key: String): Result? {
        return _results.find { it.target == navigatorId && it.key == key }
    }

    override fun setResult(result: Result) {
        if (result.target != navigatorId || !onResult(result.key, result.value)) {
            _results.add(result)
            if (intent.hasExtra(STARTED_FOR_RESULT)) {
                val resultData = Intent()
                resultData.putExtra(NAVIGATOR_RESULT, _results)
                setResult(Activity.RESULT_OK, resultData)
            }
        }
    }

    override fun clearResult(result: Result) {
        _results.remove(result)
    }

    override fun setResultTarget(resultTarget: Int) {
        _resultTarget = resultTarget
    }

    override fun getResultTarget() = _resultTarget

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(NAVIGATOR_RESULT, _results)
        outState.putInt(NAVIGATOR_ID, navigatorId)
    }

    companion object {
        private const val NAVIGATOR_ID = "navigatorId"
        private const val STARTED_FOR_RESULT = "startedForResult"
        private const val NAVIGATOR_RESULT = "navigatorResult"
        private const val DIALOG_TAG = "dialog"
        private const val INVALID_ID = 0
    }
}
