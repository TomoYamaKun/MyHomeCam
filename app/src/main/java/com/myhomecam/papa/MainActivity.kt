//app/src/main/java/com/myhomecam/papa/MainActivity.kt
//ver 1.03-13

package com.myhomecam.papa

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var repository:
        CameraRepository

    private lateinit var cameraView:
        CameraView

    private lateinit var settingsView:
        SettingsView

    private lateinit var logView:
        LogView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        AppLogger.initialize(this)

        AppLogger.info(
            "MAIN",
            "MainActivity onCreate"
        )

        repository =
            CameraRepository(this)

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { view, insets ->

            val statusBar =
                insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                )

            val oldLeft =
                view.paddingLeft

            val oldRight =
                view.paddingRight

            val oldBottom =
                view.paddingBottom

            view.setPadding(
                oldLeft,
                statusBar.top,
                oldRight,
                oldBottom
            )

            insets
        }

        val tabLayout =
            TabLayout(this)

        val viewPager =
            ViewPager2(this)

        cameraView =
            CameraView(
                this,
                repository
            )

        settingsView =
            SettingsView(
                this,
                repository
            ) {
                onSettingsChanged()
            }

        logView =
            LogView(
                this,
                repository
            )

        val pages:
            List<View> =
            listOf(
                cameraView,
                settingsView.createView(),
                logView.createView()
            )

        viewPager.adapter =
            object :
                RecyclerView.Adapter<
                    PageViewHolder
                >() {

                override fun
                    onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): PageViewHolder {

                    val page =
                        pages[viewType]

                    page.layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                    return PageViewHolder(
                        page
                    )
                }

                override fun
                    onBindViewHolder(
                        holder: PageViewHolder,
                        position: Int
                    ) {
                }

                override fun getItemCount():
                    Int {

                    return pages.size
                }

                override fun
                    getItemViewType(
                        position: Int
                    ): Int {

                    return position
                }
            }

        root.addView(
            tabLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            viewPager,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        TabLayoutMediator(
            tabLayout,
            viewPager
        ) { tab, position ->

            tab.text =
                when (position) {

                    0 ->
                        "カメラ"

                    1 ->
                        "設定"

                    2 ->
                        "ログ"

                    else ->
                        ""
                }
        }.attach()

        viewPager.registerOnPageChangeCallback(
            object :
                ViewPager2.OnPageChangeCallback() {

                override fun
                    onPageSelected(
                        position: Int
                    ) {

                    super.onPageSelected(
                        position
                    )

                    when (position) {

                        0 ->
                            cameraView.refresh()

                        1 ->
                            settingsView.refresh()

                        2 ->
                            logView.refresh()
                    }
                }
            }
        )
    }

    private fun onSettingsChanged() {

        AppLogger.info(
            "MAIN",
            "設定変更を検出"
        )

        cameraView.refresh()
        logView.refresh()
    }

    override fun onResume() {

        super.onResume()

        if (
            ::cameraView.isInitialized
        ) {
            cameraView.refresh()
        }

        if (
            ::settingsView.isInitialized
        ) {
            settingsView.refresh()
        }

        if (
            ::logView.isInitialized
        ) {
            logView.refresh()
        }
    }

    private class PageViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view)
}