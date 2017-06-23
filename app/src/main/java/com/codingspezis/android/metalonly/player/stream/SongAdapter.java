package com.codingspezis.android.metalonly.player.stream;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codingspezis.android.metalonly.player.R;
import com.codingspezis.android.metalonly.player.core.Song;
import com.codingspezis.android.metalonly.player.utils.ImageLoader;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * adapter for displaying a 3 row list view
 */
public class SongAdapter extends BaseAdapter {

    private final Activity activity;
    private final ArrayList<Song> data;
    private final LayoutInflater inflater;
    private final ImageLoader imageLoader;

    public SongAdapter(Activity a, ArrayList<Song> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(a.getApplicationContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.view_list_item_song_hist, null);
        }

        Song song = data.get(position);

        final TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        final TextView txtArtist = (TextView) view.findViewById(R.id.txtArtist);
        final TextView txtTime = (TextView) view.findViewById(R.id.txtTime);
        final TextView txtDate = (TextView) view.findViewById(R.id.txtDate);

        txtTitle.setText(song.getTitle());
        txtArtist.setText(song.getInterpret());
        try {
            final Date dateAsDate = new Date(song.getDate());
            String day = DateFormat.getDateInstance(DateFormat.SHORT,
                    Locale.GERMAN).format(dateAsDate);
            String time = DateFormat.getTimeInstance(DateFormat.SHORT,
                    Locale.GERMAN).format(dateAsDate);

            txtDate.setText(day);
            txtTime.setText(time);
        } catch (Exception e) {
            txtDate.setText("");
            txtTime.setText("");
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.modImage);
        String moderator = song.getThumb();
        imageLoader.loadImage(moderator, imageView);

        return view;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int pos) {
        return data.get(pos);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }
}