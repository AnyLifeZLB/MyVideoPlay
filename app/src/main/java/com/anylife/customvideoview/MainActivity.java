package com.anylife.customvideoview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.anylife.customvideoview.VideoView.CustomVideoView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LiveVideoView liveVideoView;
    private CustomVideoView customVideoView;
    private String path1="http://web-cache.stream.ne.jp/www11/nhkworld-tv/dwstv/222468/live.m3u8";
    private String path2="http://chyd-wsvod.wasu.tv/data13/ott/344/2015-05/28/1432782476341_377935/playlist.m3u8";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this,"NHK Playing ...",Toast.LENGTH_LONG).show();

//        VideoView vv = (VideoView)findViewById(R.id.videoView1);
//        vv.setVideoPath(path2);
//        //设置媒体控制条`
//        vv.setMediaController(new MediaController(this));
//        vv.start();
//
//        //================================================================================================================
//
//        liveVideoView=(LiveVideoView)findViewById(R.id.videoView);
//        List<String> path=new ArrayList<>();
//        path.add("http://chyd-wsvod.wasu.tv/data13/ott/344/2015-05/28/1432782476341_377935/playlist.m3u8");
//        liveVideoView.setVideoPaths(path);
//        liveVideoView.setVideoPath(path.get(0));

        //===================================================================================================================
        customVideoView=(CustomVideoView)findViewById(R.id.custom_videoView);
        customVideoView.setVideoPath(path2);
        customVideoView.start();

    }
}
