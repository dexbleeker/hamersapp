package nl.ecci.hamers.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_hamers_tab.*
import nl.ecci.hamers.R

open class HamersTabFragment : HamersFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            tab_fragment_sliding_tabs.setTabTextColors(
                    ContextCompat.getColor(it, R.color.sliding_tabs_text_normal),
                    ContextCompat.getColor(it, R.color.sliding_tabs_text_selected)
            )
        }
        tab_fragment_sliding_tabs.setSelectedTabIndicatorColor(Color.WHITE)
        tab_fragment_sliding_tabs.setupWithViewPager(tab_fragment_viewpager)
    }

}
