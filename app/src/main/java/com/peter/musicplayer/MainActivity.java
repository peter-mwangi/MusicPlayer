package com.peter.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    //Defining the permissions we need.
    private static final String[] PERMISSIONS ={
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    //Defining the request code.
    private static final int REQUEST_PERMISSIONS = 1234;
    //Defining a variable to store the number of permissions granted.
    private static final int PERMISSIONS_COUNT = 1;

    //Check if permissions are allowed.

    @SuppressLint("NewApi")
    private boolean arePermissionsDenied()
    {
        for (int i =0; i<PERMISSIONS_COUNT; i++)
        {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (arePermissionsDenied())
        {
            ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate();
        }
        else
        {
            onResume();

        }
    }

    private boolean isMusicPlayerInit;
    private List<String> musicFilesList;

    @Override
    protected void onResume()
    {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied())
        {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }

        if (!isMusicPlayerInit)
        {
            final ListView musicFiles = findViewById(R.id.files_list_view);

            final TextAdapter textAdapter = new TextAdapter();
            musicFilesList = new ArrayList<>();

            isMusicPlayerInit = true;
        }
    }

    class TextAdapter extends BaseAdapter
    {
        private List<String> data = new ArrayList<>();

        void setData(List<String> mData)
        {
            data.clear();
            data.addAll(mData);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null)
            {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
                view.setTag(new ViewHolder((TextView) view.findViewById(R.id.myTextView)));
            }

            ViewHolder holder= (ViewHolder) view.getTag();
            final String item =data.get(i);
            holder.musicName.setText(item.substring(item.lastIndexOf('/')+1));
            return view;
        }
    }
    class ViewHolder
    {
        TextView musicName;
        ViewHolder(TextView myMusicName)
        {
            musicName = myMusicName;
        }
    }
}
