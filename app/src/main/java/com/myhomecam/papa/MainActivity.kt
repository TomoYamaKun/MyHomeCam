//app/src/main/java/com/myhomecam/papa/MainActivity.kt
//ver 1.02-12

package com.myhomecam.papa

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var repository: CameraRepository
    private lateinit var cameraView: CameraView
    private lateinit var settingsView: SettingsView
    private lateinit var logView: LogView

    private lateinit var cameraPage: View
    private lateinit var settingsPage: View
    private lateinit var logPage: View

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

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

        cameraPage =
            cameraView

        settingsPage =
            settingsView.createView()

        logPage =
            logView.createView()

        val pages: List<View> =
            listOf(
                cameraPage,
                settingsPage,
                logPage
            )

        viewPager.adapter =
            object :
                RecyclerView.Adapter<PageViewHolder>() {

                override fun onCreateViewHolder(
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

                override fun onBindViewHolder(
                    holder: PageViewHolder,
                    position: Int
                ) {
                }

                override fun getItemCount(): Int {
                    return pages.size
                }

                override fun getItemViewType(
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
                    0 -> "カメラ"
                    1 -> "設定"
                    2 -> "ログ"
                    else -> ""
                }
        }.attach()

        viewPager.registerOnPageChangeCallback(
            object :
                ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(
                    position: Int
                ) {
                    super.onPageSelected(position)

                    when (position) {

                        0 -> {
                            cameraView.refresh()
                        }

                        1 -> {
                            settingsView.refresh()
                        }

                        2 -> {
                            logView.refresh()
                        }
                    }
                }
            }
        )
    }

    private fun onSettingsChanged() {

        AppLogger.info(
            "MAIN",
            "設定変更を検出しました。画面を更新します。"
        )

        cameraView.refresh()
        logView.refresh()
    }

    override fun onResume() {
        super.onResume()

        if (::cameraView.isInitialized) {
            cameraView.refresh()
        }

        if (::settingsView.isInitialized) {
            settingsView.refresh()
        }

        if (::logView.isInitialized) {
            logView.refresh()
        }

        AppLogger.info(
            "MAIN",
            "MainActivity onResume"
        )
    }

    private class PageViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view)
}