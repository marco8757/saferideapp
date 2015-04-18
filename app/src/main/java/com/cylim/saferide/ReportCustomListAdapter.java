package com.cylim.saferide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by marco on 18/4/15.
 */
public class ReportCustomListAdapter extends ArrayAdapter<String> {

    Context context;
    String[] image;
    String[] lat;
    String[] lng;
    String[] author;

    public ReportCustomListAdapter(Context context, int resource, String[] image,
                                   String[] lat, String[] lng, String[] author) {
        super(context, resource, image);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.image = image;
        this.lat = lat;
        this.lng = lng;
        this.author = author;
    }

    public static Bitmap bitmapFromURL(String src) {
        try {
            Log.e("src", src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ReportView = inflater
                .inflate(R.layout.custom_report_list_item, parent, false);
        TextView tvAuthor = (TextView) ReportView.findViewById(R.id.tvCRLBy);
        TextView tvCoordinate = (TextView) ReportView
                .findViewById(R.id.tvCRLcoor);
        ImageView ivPicture = (ImageView) ReportView.findViewById(R.id.ivCRL);

        tvAuthor.setText(author[position]);
        tvCoordinate.setText("At: " + lat[position] + ", " + lng[position]);
        ivPicture.setImageBitmap(bitmapFromURL(image[position]));


        return ReportView;
    }
}
