package com.peter.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private SeekBar seekBar;
    private TextView songPosition, songDuration;

    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songPosition = findViewById(R.id.current_position);
        songDuration = findViewById(R.id.song_duration);
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

    private void addMusicFilesFrom(String dirPath)
    {
        final File musicDir = new File(dirPath);
        if (!musicDir.exists())
        {
            musicDir.mkdir();
            return;
        }
        final File[] files = musicDir.listFiles();
        for (File file: files)
        {
            final String path = file.getAbsolutePath();
            if (path.endsWith(".mp3"))
            {
                musicFilesList.add(path);
            }
        }
    }

    private void fillMusicList()
    {
        musicFilesList.clear();
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
    }
    private MediaPlayer mediaPlayer;

    private int playMusicFile(String path)
    {
        mediaPlayer = new MediaPlayer();

        try
        {
           mediaPlayer.setDataSource(path);
           mediaPlayer.prepare();
           mediaPlayer.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return mediaPlayer.getDuration();
    }



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
            fillMusicList();
            textAdapter.setData(musicFilesList);
            musicFiles.setAdapter(textAdapter);

            seekBar = findViewById(R.id.seek_bar);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                int songProgress;

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b)
                {
                    songProgress = i;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                    mediaPlayer.seekTo(songProgress);
                }
            });

            musicFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final String musicFilePath = musicFilesList.get(i);
                    final long mSongDuration = playMusicFile(musicFilePath);
                    seekBar.setVisibility(View.VISIBLE);
                    songPosition.setVisibility(View.VISIBLE);
                    songDuration.setVisibility(View.VISIBLE);
                    getDurationTimer(mSongDuration);
                    getSeekBarStatus();

                }
            });

            isMusicPlayerInit = true;
        }
    }
    private void getDurationTimer(long mSongDuration)
    {
        final long minutes=(mSongDuration/1000)/60;
        final int seconds= (int) ((mSongDuration/1000)%60);
       songDuration.setText(minutes+ ":"+seconds);
    }
    private void getSeekBarStatus()
    {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // mp is your MediaPlayer
                // progress is your ProgressBar
                int total = mediaPlayer.getDuration();
                seekBar.setMax(total);
//                songDuration.setText(String.valueOf(total));
                while (mediaPlayer != null && currentPosition < total) {
                    try {
                        Thread.sleep(1000);
                        currentPosition = mediaPlayer.getCurrentPosition();
                    } catch (InterruptedException e) {
                        return;
                    }
                    seekBar.setProgress(currentPosition);

                }
            }
        }).start();

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
