package com.example.musicplayer.activity;

import static com.example.musicplayer.activity.MainActivity.addSongToQueue;
import static com.example.musicplayer.activity.MainActivity.currPlayedPlaylistID;
import static com.example.musicplayer.activity.MainActivity.currPlayedSong;
import static com.example.musicplayer.activity.MainActivity.getAllSongs;
import static com.example.musicplayer.activity.MainActivity.getQueuePlaying;
import static com.example.musicplayer.activity.MainActivity.getSongByPath;
//import static com.example.musicplayer.activity.MainActivity.prevPosition;
import static com.example.musicplayer.activity.MainActivity.queuePlaying;
import static com.example.musicplayer.activity.MainActivity.setQueuePlaying;
import static com.example.musicplayer.activity.MainActivity.songList;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicplayer.R;
import com.example.musicplayer.model.SongModel;
import com.example.musicplayer.tool.GetDominantColor;

import java.util.ArrayList;
import java.util.Random;



public class PlayingActivity extends AppCompatActivity {
    TextView artist_name, duration_played, duration_total, title;
    ImageView cover_img, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn, playPauseBtn, img_queue;
    SeekBar seekBar;
    public static int position = -1;
    static ArrayList<SongModel> listSongs;
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, nextThread, prevThread;
    static Boolean shuffleBoolean = false;
    static int repeat = 0;
    public static TextView song_name;
    public static byte[] img_status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playing);
        initViews();
        getIntentData();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress*1000);
                }
                if (seekBar.getProgress() == seekBar.getMax()) {
                    if (repeat == 2) {
                        repeatPlay();
                    }
                    else {
                        nextBtnClicked();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffleBoolean = !shuffleBoolean;
                if (shuffleBoolean) {
                    shuffleBtn.setImageResource(R.drawable.iconshuffled);
                    nextBtn.setImageResource(R.drawable.iconnext);
                    prevBtn.setImageResource(R.drawable.iconprevious);
                }
                else {
                    if (position == listSongs.size() - 1) nextBtn.setImageResource(R.drawable.iconnextnull);
                    else nextBtn.setImageResource(R.drawable.iconnext);
                    if (position == 0) prevBtn.setImageResource(R.drawable.iconpreviousnull);
                    else prevBtn.setImageResource(R.drawable.iconprevious);
                    shuffleBtn.setImageResource(R.drawable.iconshuffle);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeat = (repeat+1) % 3;
                if (repeat == 0) {
                    if (position == listSongs.size() - 1) nextBtn.setImageResource(R.drawable.iconnextnull);
                    else nextBtn.setImageResource(R.drawable.iconnext);
                    if (position == 0) prevBtn.setImageResource(R.drawable.iconpreviousnull);
                    else prevBtn.setImageResource(R.drawable.iconprevious);
                    repeatBtn.setImageResource(R.drawable.iconrepeat);
                }

                if (repeat == 1) {
                    nextBtn.setImageResource(R.drawable.iconnext);
                    prevBtn.setImageResource(R.drawable.iconprevious);
                    repeatBtn.setImageResource(R.drawable.iconrepeated);
                }

                if (repeat == 2) {
                    if (position == listSongs.size() - 1) nextBtn.setImageResource(R.drawable.iconnextnull);
                    else nextBtn.setImageResource(R.drawable.iconnext);
                    if (position == 0) prevBtn.setImageResource(R.drawable.iconpreviousnull);
                    else prevBtn.setImageResource(R.drawable.iconprevious);
                    repeatBtn.setImageResource(R.drawable.iconrepeat_one);
                }
            }
        });
        img_queue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayingActivity.this, QueuePlayingActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    private void repeatPlay() {
        uri = Uri.parse(listSongs.get(position).getPath());
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration()/1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        title.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
    }

    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition%60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" +seconds;
        totalNew = minutes +":" +"0" +seconds;
        if (seconds.length() == 1) {
            return totalNew;
        }
        else {
            return totalout;
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null){
            if (intent.hasExtra("playlistID")){
                String playlistID = getIntent().getStringExtra("playlistID");
                if(currPlayedPlaylistID==null || !currPlayedPlaylistID.equals(playlistID)){
                    currPlayedPlaylistID = playlistID;
                    position=0;
                    currPlayedSong = getQueuePlaying().get(position);
                    uri = Uri.parse(currPlayedSong.getPath());
                    setMediaPlayer(uri);
                    listSongs = getQueuePlaying();
                    setDataView(uri);
                }
            }

            if (intent.hasExtra("songPath")){
                String songPath = getIntent().getStringExtra("songPath");
                uri = Uri.parse(songPath);
                position = MainActivity.getSongPositonByPath(getQueuePlaying(), songPath);
                if(position==-1) {
                    SongModel song = getSongByPath(songList, songPath);
                    currPlayedPlaylistID=null;
                    ArrayList<SongModel> newQueue = new ArrayList<>();
                    newQueue.add(song);
                    setQueuePlaying(newQueue);
                    position = MainActivity.getSongPositonByPath(getQueuePlaying(), songPath);
                    currPlayedSong = getQueuePlaying().get(position);
                    setMediaPlayer(uri);
                }

                listSongs = getQueuePlaying();
                setDataView(uri);
            }
        }
    }


    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        song_name = findViewById(R.id.song_name);
        title = findViewById(R.id.toolbar_title);
        artist_name = findViewById(R.id.artist_name);
        duration_played = findViewById(R.id.duration_played);
        duration_total = findViewById(R.id.duration_total);
        cover_img = findViewById(R.id.cover_img);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn = findViewById(R.id.prevBtn);
        shuffleBtn = findViewById(R.id.shuffleBtn);
        repeatBtn = findViewById(R.id.repeatBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        seekBar = findViewById(R.id.seekBar);
        img_queue = findViewById(R.id.img_queue);
        overridePendingTransition(R.anim.anim_intent_in, R.anim.anim_intent_out);
    }

    public void setMediaPlayer(Uri uri){
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
            } else {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
            }
            if (mediaPlayer.isPlaying()) playPauseBtn.setImageResource(R.drawable.nutplay);
            else playPauseBtn.setImageResource(R.drawable.nutpause);
    }

    public void setDataView(Uri uri){
        metaData(uri);
        song_name.setText(currPlayedSong.getTitle());
        title.setText(currPlayedSong.getTitle());
        artist_name.setText(currPlayedSong.getArtist());
        int durationToTal = Integer.parseInt(currPlayedSong.getDuration()) / 1000;
        seekBar.setMax(durationToTal);
        duration_total.setText(formattedTime(durationToTal));
    }
    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] img = retriever.getEmbeddedPicture();
        img_status = img;
        if (img != null) {
            Glide.with(this).asBitmap().load(img).apply(RequestOptions.bitmapTransform(new RoundedCorners(50))).into(cover_img);
        }
        else {
            Glide.with(this).asBitmap().load(R.drawable.imgitem).into(cover_img);
        }
        if ((position == listSongs.size() - 1) && repeat != 1 && !shuffleBoolean) nextBtn.setImageResource(R.drawable.iconnextnull);
        else nextBtn.setImageResource(R.drawable.iconnext);
        if (position == 0 && repeat != 1 && !shuffleBoolean) prevBtn.setImageResource(R.drawable.iconpreviousnull);
        else prevBtn.setImageResource(R.drawable.iconprevious);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        int darkColor = android.graphics.Color.parseColor("#121212");
        gradientDrawable.setColors(new int[]{GetDominantColor.getDominantColor(img), darkColor});
        gradientDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
        gradientDrawable.setCornerRadius(10);
        RelativeLayout relativeLayout = findViewById(R.id.mContainer);
        relativeLayout.setBackground(gradientDrawable);
    }

    @Override
    protected void onResume() {
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!(position == 0 && repeat != 1 && !shuffleBoolean))
                            prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    private void prevBtnClicked() {
        if (repeat == 2) {
            repeatBtn.setImageResource(R.drawable.iconrepeated);
            repeat = 1;
            return;
        }
        if (repeat != 1 && !shuffleBoolean) {
            if (position == 0) return;
            position -= 1;
        }
        if (repeat == 1) {
            if (position == 0) position = listSongs.size();
            position -= 1;
        }
        if (shuffleBoolean) {
            int positionRandom = getRandom(listSongs.size());
            while (position == positionRandom)
            {
                positionRandom = getRandom(listSongs.size());
            }
            position = positionRandom;
        }
        playPauseBtn.setImageResource(R.drawable.nutplay);
        uri = Uri.parse(listSongs.get(position).getPath());
        currPlayedSong = listSongs.get(position);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration()/1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        title.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!((position == listSongs.size() - 1) && repeat != 1 && !shuffleBoolean)){
                            nextBtnClicked();
                        }
                    }
                });
            }
        };
        nextThread.start();
    }

    private void nextBtnClicked() {
        if (repeat == 2) {
            repeatBtn.setImageResource(R.drawable.iconrepeated);
            repeat = 1;
            return;
        }
        if (repeat != 1 && !shuffleBoolean) {
            if (position == listSongs.size()-1){
                    getSongRandom();
                return;
            }
            position += 1;
        }

        if (repeat == 1) {
            position = (position + 1) % listSongs.size();
        }
        if (shuffleBoolean) {
            int positionRandom = getRandom(listSongs.size());
            while (position == positionRandom)
            {
                positionRandom = getRandom(listSongs.size());
            }
            position = positionRandom;
        }
        if (listSongs.size() > position) {
            playPauseBtn.setImageResource(R.drawable.nutplay);
            uri = Uri.parse(listSongs.get(position).getPath());
            currPlayedSong = listSongs.get(position);
        }
        else {
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration()/1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        title.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
    }


    private int getRandom(int size) {
        Random random = new Random();
        return random.nextInt(size);
    }

    public void getSongRandom(){
            int positionRandom = getRandom(songList.size());
            int posInSongList = MainActivity.getSongPositonByPath(songList, currPlayedSong.getPath());
            while (posInSongList == positionRandom) {
                positionRandom = getRandom(songList.size());
            }
            SongModel song = songList.get(positionRandom);
            ArrayList<SongModel> newQueue = new ArrayList<>();
            newQueue.add(song);
            setQueuePlaying(newQueue);
            listSongs = getQueuePlaying();
            position = listSongs.size()-1;
            currPlayedSong = listSongs.get(position);
            uri = Uri.parse(currPlayedSong.getPath());
            setMediaPlayer(uri);
            metaData(uri);
            setDataView(uri);
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.nutpause);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        duration_played.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
        else {
            playPauseBtn.setImageResource(R.drawable.nutplay);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        duration_played.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }
}