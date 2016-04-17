package com.myapp.sg1907.contactmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sg1907 on 17.04.2016.
 */
public class MyAdapter<Object> extends ArrayAdapter<Contact> {
    private final Context context;
    private final ArrayList<Contact> contactList;
    private final int listLayout;
    private final int nameTextViewID;
    private final int phoneNumberTextViewID;
    private final int photoImageViewID;

    public MyAdapter(Context context, int listLayout, int nameTextViewID, int phoneNumberTextViewID, int photoImageViewID, ArrayList<Contact> contactList){
        super(context,listLayout, contactList);
        this.context = context;
        this.listLayout = listLayout;
        this.nameTextViewID = nameTextViewID;
        this.phoneNumberTextViewID = phoneNumberTextViewID;
        this.photoImageViewID = photoImageViewID;
        this.contactList = contactList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(listLayout, parent, false);
        TextView nameTextView = (TextView) view.findViewById(nameTextViewID);
        TextView phoneNumberTextView = (TextView) view.findViewById(phoneNumberTextViewID);
        ImageView photoImageView = (ImageView) view.findViewById(photoImageViewID);

        nameTextView.setText(contactList.get(position).getName());
        phoneNumberTextView.setText(contactList.get(position).getPhoneNumber());
        byte[] photo = contactList.get(position).getPhoto();

        if(photo == null){
            photoImageView.setImageResource(R.mipmap.default_photo);
        }
        else {
            Bitmap bmp = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            photoImageView.setImageBitmap(bmp);
        }
        return view;
    }
}
