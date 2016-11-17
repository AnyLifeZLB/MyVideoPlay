package com.anylife.customvideoview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.anylife.customvideoview.VideoView.CustomVideoView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LiveVideoView liveVideoView;
    private CustomVideoView customVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        liveVideoView=(LiveVideoView)findViewById(R.id.videoView);
//        List<String> path=new ArrayList<>();
//        path.add("http://web-cache.stream.ne.jp/www11/nhkworld-tv/dwstv/222468/live.m3u8");
//        liveVideoView.setVideoPaths(path);

//        liveVideoView.setVideoPath(path.get(0));


        customVideoView=(CustomVideoView)findViewById(R.id.custom_videoView);

        customVideoView.setVideoPath("http://web-cache.stream.ne.jp/www11/nhkworld-tv/dwstv/222468/live.m3u8");


    }
}
