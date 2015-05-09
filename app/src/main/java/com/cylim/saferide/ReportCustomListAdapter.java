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

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 18/4/15.
 */
public class ReportCustomListAdapter extends ArrayAdapter<String> {

    Context context;
    List<String> reportImage;
    List<String> reportLat;
    List<String> reportLng;
    List<String> reportAuthor;

    public ReportCustomListAdapter(Context context, int resource, List<String> image,
                                   List<String> lat, List<String> lng, List<String> author) {
        super(context, resource, image);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.reportImage = image;
        this.reportLat = lat;
        this.reportLng = lng;
        this.reportAuthor = author;
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

        if (!reportImage.get(position).equals(null)) {
            Picasso.with(context).load(reportImage.get(position)).into(ivPicture);
        }
        tvCoordinate.setText("At: " + reportLat.get(position) + ", " + reportLng.get(position));
        tvAuthor.setText("Reported by: " + reportAuthor.get(position));

        return ReportView;
    }
}
