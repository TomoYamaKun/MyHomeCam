//app/src/main/java/com/myhomecam/papa/MainActivity.kt
//ver 1.01-01

package com.myhomecam.papa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var repository: CameraRepository
    private lateinit var cameraView: CameraView
    private lateinit var settingsView: SettingsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = CameraRepository(this)

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
        }

        val tabLayout = TabLayout(this)

        val viewPager = ViewPager2(this)

        cameraView = CameraView(
            this,
            repository
        )

        settingsView = SettingsView(
            this,
            repository
        )

        val pages = listOf(
            cameraView,
            settingsView
        )

        viewPager.adapter = object :
            androidx.recyclerview.widget.RecyclerView.Adapter<
                    PageViewHolder>() {

            override fun onCreateViewHolder(
                parent: android.view.ViewGroup,
                viewType: Int
            ): PageViewHolder {
                return PageViewHolder(
                    pages[viewType]
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
        }

        root.addView(
            tabLayout,
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            viewPager,
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        TabLayoutMediator(
            tabLayout,
            viewPager
        ) { tab, position ->
            tab.text = when (position) {
                0 -> "カメラ"
                1 -> "設定"
                else -> ""
            }
        }.attach()
    }

    override fun onResume() {
        super.onResume()

        if (::cameraView.isInitialized) {
            cameraView.refresh()
        }

        if (::settingsView.isInitialized) {
            settingsView.refresh()
        }
    }

    private class PageViewHolder(
        view: android.view.View
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}