package com.cylim.saferide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by marco on 18/4/15.
 */
public class CommentCustomListAdapter extends ArrayAdapter<String> {

    Context context;
    List<String> commentContent;
    List<String> commentAuthor;

    public CommentCustomListAdapter(Context context, int resource, List<String> comment,
                                    List<String> author) {
        super(context, resource, comment);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.commentContent = comment;
        this.commentAuthor = author;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View CommentView = inflater
                .inflate(R.layout.custom_comment_list_item, parent, false);
        TextView tvAuthor = (TextView) CommentView.findViewById(R.id.tvCCLAuthor);
        TextView tvContent = (TextView) CommentView
                .findViewById(R.id.tvCCLComment);

        tvAuthor.setText(commentAuthor.get(position));
        tvContent.setText(commentContent.get(position));
        return CommentView;
    }
}
