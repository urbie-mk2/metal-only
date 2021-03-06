package com.codingspezis.android.metalonly.player.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.codingspezis.android.metalonly.player.R
import com.codingspezis.android.metalonly.player.stream.metadata.Metadata
import org.androidannotations.annotations.EViewGroup
import org.androidannotations.annotations.UiThread
import org.androidannotations.annotations.ViewById

@EViewGroup(R.layout.view_showinformation)
open class ShowInformation(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    @JvmField
    @ViewById(R.id.marqueeMod)
    protected var marqueeMod: Marquee? = null

    @JvmField
    @ViewById(R.id.marqueeGenree)
    protected var marqueeGenre: Marquee? = null

    @JvmField
    @ViewById(android.R.id.progress)
    protected var progress: View? = null

    init {
        orientation = LinearLayout.VERTICAL
    }

    fun setMetadata(metadata: Metadata) {
        if (metadata.historicTrack().isValid) {
            updateViews(metadata.genre, metadata.moderator)
        }
    }

    @UiThread
    internal open fun updateViews(genre: String?, moderator: String?) {
        if (genre != null) update(marqueeGenre, genre)
        if (moderator != null) update(marqueeMod, moderator)
    }

    private fun update(view: Marquee?, value: String) {
        view?.text = value
        view?.visibility = View.VISIBLE
        progress?.visibility = View.GONE
    }

    fun setStats(stats: com.github.ironjan.metalonly.client_library.Stats) {
        updateViews(stats.genre, stats.moderator)
    }
}
