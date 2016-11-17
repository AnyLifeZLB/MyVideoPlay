package com.anylife.customvideoview;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController.MediaPlayerControl;
import java.io.IOException;
import java.util.List;
import java.util.Vector;


/**
 * 可以通用于播放HTTP、HLS、RTSP、Local File。如果部分设备不支持，可以使用软解（软解库将使用FFPEG）。
 *
 * @author liubao.zeng
 * @version 2013-1-2 创建时间
 */

public class LiveVideoView extends SurfaceView implements MediaPlayerControl {
    private String TAG = "VideoView";
    private String TAG2 = "VideoViewDebug";

    private Context mContext;
    private MediaPlayer mMediaPlayer = null;
    private SurfaceHolder mSurfaceHolder = null;

    private int mCurrentBufferPercentage;
    private boolean mStartWhenPrepared;
    private int mSeekWhenPrepared;
    private boolean mIsPrepared;

    private Uri mUri;
    private int mDuration;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;

    private Vector<String> mPlayList = new Vector<String>();
    private int mPlayListLength = -1;
    private int mCurrentPlay = -1;
    private long playtime = 0;
    private MySizeChangeLinstener mMyChangeLinstener;
    private Handler UiMangerHandler = null;


    /**
     * 构造方法1   for SmartLiveTV
     *
     * @param context
     */
    public LiveVideoView(Context context, Handler UiMangerHandler) {
        super(context);
        mContext = context;
        this.UiMangerHandler = UiMangerHandler;
        initVideoView();
        mPlayListLength = mPlayList.size();
    }


    /**
     * 在信息发布系统中无用
     *
     * @param context
     * @param attrs   从XML配置文件定义本对象使用
     */
    public LiveVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
        mPlayListLength = mPlayList.size();
    }

    /**
     * 在信息发布系统中无用
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public LiveVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }


    /**
     * 视频的初始化设置
     */
    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        this.setBackgroundColor(0x66000000);
    }


    /**
     * 通用资源标志符（Universal Resource Identifier, 简称"URI"）
     * path(String)  to Uri  以适应mMediaPlayer.setDataSource(context,Uri)
     *
     * @param realPath 要播放视频的路径
     */
    public void setVideoPath(String realPath) {          //设置视屏播放的路径
        if (realPath != null) {                          //最好判断本路径下的文件是否真的在，不在
            mUri = Uri.parse(realPath);
            mStartWhenPrepared = false;
            mSeekWhenPrepared = 0;
            openVideo();                                  //打开视屏
        }
    }

    /**
     * 通用资源标志符（Universal Resource Identifier, 简称"URI"）
     * path(String)  to Uri  以适应mMediaPlayer.setDataSource(context,Uri)
     *
     * @param path 要播放视频的路径
     */
    public void setVideoPaths(List<String> path) {          //设置视屏播放的路径
        if (path != null&&path.size()!=0) {
            mPlayList.clear();
            mCurrentPlay = 0;
            mPlayListLength = path.size();
            mPlayList.addAll(path);
            setVideoPath(mPlayList.get(0));
//            mUri = Uri.parse(mPlayList.get(0));
//            mStartWhenPrepared = false;
//            mSeekWhenPrepared = 0;
//            openVideo();                                           //打开视屏
//            requestLayout();                                       //请求对应的布局
//            postInvalidate();
        }
    }

    /**
     * 获取当前播放路径
     *
     * @return 当前播放路径
     */
    public String getVideoPtah() {
        Log.d(TAG, "mCurrentPlay" + mCurrentPlay + "=====mPlayListLength" + mPlayListLength);
        if (mCurrentPlay > -1 && mCurrentPlay < mPlayListLength)
            return mPlayList.get(mCurrentPlay);
        else
            return "unknow video";
    }

    /**
     * 停止视频的播放
     */
    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * 开始视频的播放
     */
    private void openVideo() {
        Log.d(TAG, mCurrentPlay + "当前播放的视频是：" + mUri);

        if (mUri == null || mSurfaceHolder == null) {
            Log.d(TAG, mUri + "(mUri == null ?|| mSurfaceHolder == null)!");
            return;
        }
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);      //musicservice 广播
        if (mMediaPlayer != null) {     //播放完成后，释放对象占有的资源            
            mMediaPlayer.reset();       //恢复到IDLE状态
            mMediaPlayer.release();     //处于End状态
            mMediaPlayer = null;
        }

        if (UiMangerHandler != null) {
            Message msg = new Message();
            msg.arg1 = mCurrentPlay + 1;
            msg.arg2 = mPlayListLength;
//            msg.what = LiveTVMainActivity.UPDATE_TVTIPS_SOURCE_DATA;
            UiMangerHandler.sendMessage(msg);
            Log.e(TAG, "那个谁，显示提示信息");
        }

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mIsPrepared = false;
            mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mContext, mUri);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            return;
        }
    }

    /**
     * Surface的状态控制
     * 视频输出区域的状态检测及处理，在创建、更改、销毁的时候会回调相应的处理方法
     *
     * @author liubao.zeng
     */
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        /**
         * 视频显示区域的初始化
         * 在本方法自动开始视频的播放
         *
         */
        public void surfaceCreated(SurfaceHolder holder)  //第一次创建的时候
        {
            mSurfaceHolder = holder;
            openVideo();
            playNextVideo();
        }

        /**
         * 视频播放区域有改变的时候会监听
         *
         */
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            //当surface有改变的时候回调此函数。 重新选择另外的视屏的时候调用此方法
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            if (mMediaPlayer != null && mIsPrepared && mVideoWidth == w && mVideoHeight == h) {
                if (mSeekWhenPrepared != 0) {
                    mSeekWhenPrepared = 0;
                    mSeekWhenPrepared = 0;
                }
                mMediaPlayer.start();
                Log.d(TAG, "Video surfacechange");
            }
            Log.d(TAG, "mSurfaceWidth 改变" + mSurfaceWidth);   //重新选择另外的视屏的时候调用此方法
        }

        /**
         * 注意资源的释放
         * 本方法执行后不可以再用本对象的任何操作
         */
        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
                Log.d(TAG, "视频 销毁mMediaPlayer");
            }
            Log.d(TAG, "video-is destroy");
        }
    };

    /*    
     *    缓冲区更新监听 ,网络电视预留接口
     */
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    /**
     * 注册在设置或播放过程中发生错误时调用的回调函数。如果未指定回调函数，
     * 或回调函数返回假，VideoView 会通知用户发生了错误。
     *
     * @param l 要执行的回调函数。
     */
    public void setOnInfoListener(OnInfoListener l)     //容错处理
    {
        mOnInfoListener = l;
    }

    /**
     * 状态监听，时刻获取动态信息
     */
    private OnInfoListener mInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
//       Log.e("onInfo_video", "what:"+what+"  extra:"+extra);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            } else if (mMediaPlayer != null) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {//你不要弄反了
                    mMediaPlayer.pause();//In order to buffer more data to play
                    Log.e(TAG, "准备数据缓存，暂停播放！");
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    Log.e(TAG, "数据缓存完毕，准备播放！");
                    mMediaPlayer.start();
                }
            }
            return true;
        }
    };


    /**
     * 错误处理
     */
    private void mediaErrorDispose(int framework_err, int impl_err) {
        switch (framework_err) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                // 视频不可以回退
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
//        Toast.makeText(mContext, "流媒体服务器端异常！", 3000);
                Log.d(TAG, "流媒体服务器端异常!");
                playNextVideo();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d(TAG, "不能播放的视频文件！");
//                playNextVideo();
                break;
            default:              //不知道的错误类型不知道怎样处理
                playNextVideo();
                break;
        }
    }

    /**
     * 注册在设置或播放过程中发生错误时调用的回调函数。如果未指定回调函数，
     * 或回调函数返回假，VideoView 会通知用户发生了错误。
     *
     * @param l 要执行的回调函数。
     */
    public void setOnErrorListener(OnErrorListener l)     //容错处理
    {
        mOnErrorListener = l;
    }

    /**
     * 错误监听，错误不管在什么时候都会发生，发生了就要报告
     * 完善此方法
     */
    private OnErrorListener mErrorListener =
            new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    Log.e(TAG2, "有 错误发生：Error: " + framework_err + "," + impl_err);
//                    mediaErrorDispose(framework_err, impl_err);

                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    /**
                     * 异常的处理，如果出现了异常就处理，稳定性没有测试过，这个真的太难测试了
                     */
//            playNextVideo();
//            mediaErrorDispose(framework_err, impl_err);
                    return true;
                }
            };


    /**
     * 注册在媒体文件加载完毕，可以播放时调用的回调函数。
     * @param l 要执行的回调函数。
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * 和prepareAsync()配合，如果异步准备完成，会触发OnPreparedListener.onPrepared()，进而进入Prepared状态。
     *
     * MediaPlayer一旦准备好，就可以调用start()方法，这样MediaPlayer就处于Started状态
     */
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            Log.e(TAG, "准备播放视频");
            mIsPrepared = true;
            if (mOnPreparedListener != null) {  //让mMediaPlayer转移到Prepared状态
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            if (mVideoWidth != 0 && mVideoHeight != 0) {       //视频源没有问题
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                Log.d(TAG, "视频的宽高： " + mVideoWidth + "====" + mVideoHeight);
//                mMediaPlayer.start();   //单曲循环嘛，什么东西嘛！！！！！！真的不知道为什么了
                start();
//                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            } else {

                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    Log.d(TAG, "mSeekWhenPrepared != 0");
                    mSeekWhenPrepared = 0;
                }
                if (mStartWhenPrepared) {
                    Log.d(TAG, "mStartWhenPrepared is ok");
                    start();
                    mStartWhenPrepared = false;
                }
            }
        }
    };

    /**
     * 注册在媒体文件播放完毕时调用的回调函数。
     *
     * @param l 要执行的回调函数。
     */
    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * 播放完成后
     */
    private OnCompletionListener mCompletionListener =
            new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                        playtime++;
                    }
                    playNextVideo();
                    Log.e(TAG, "准备播放完成");
                }
            };

    /**
     * 控制播放模式
     * 流媒体也在这里播放
     */
    public void playNextVideo() {
        if (mPlayListLength > 0) {
            mCurrentPlay = (mCurrentPlay + 1) % mPlayListLength;
            if (mCurrentPlay > -1 && mCurrentPlay < mPlayListLength) {
                setVideoPath(mPlayList.get(mCurrentPlay));
            }
            Log.e(TAG, mCurrentPlay + "播放下一个视频:" + mPlayList.get(mCurrentPlay));
        }
    }

    /**
     * @author liubao.zeng
     */
    public interface MySizeChangeLinstener {                    //空方法
        public void doMyThings();                              //其他的模块停下来
    }

    /**
     * @param l
     */
    public void setMySizeChangeLinstener(MySizeChangeLinstener l) {
        mMyChangeLinstener = l;
    }

    /**
     * 改变了又会怎样
     * 暂时无用 暂时无用 暂时无用 暂时无用 暂时无用 暂时无用 暂时无用 暂时无用
     */
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    if (mMyChangeLinstener != null) {
                        mMyChangeLinstener.doMyThings();
                    }
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                    }
                }
            };

    /**
     * 转换状态机，开始视频的播放
     */
    public void start() {
        Log.d(TAG, "视频开始播放");
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.start();
//            UiMangerHandler.sendEmptyMessage(LiveTVMainActivity.SHOW_AND_HIDE_POPWINDOW_XX_LATER);
            mStartWhenPrepared = false;
        } else {
            mStartWhenPrepared = true;
        }
    }

    /**
     * 转换状态机，暂停视频的播放
     */
    public void pause() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        mStartWhenPrepared = false;
    }

    /**
     * `
     * 获得所播放视频的总时间。
     */
    public int getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    /**
     * 获取当前的视频播放列表的索引
     */
    public int getCurrentIndex() {
        return mCurrentPlay;
    }

    /**
     * @param index
     */
    public void setCurenntPlay(int index) {
        if (index > 0 && index < mPlayList.size())
            setVideoPath(mPlayList.get(index));
    }

    /**
     * 获取当前的视频播放的位置
     */
    public int getCurrentPosition() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 快进视频播放
     */
    public void seekTo(int msec) {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(msec);
            Log.d(TAG, "video Seek to: " + msec);
        } else {
            mSeekWhenPrepared = msec;              //
            Log.d(TAG, "SEEK--asigned");
        }
    }

    /**
     * 判断视频的是否播放
     */
    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    /**
     * 获取缓冲区的百分比
     */
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    /**
     * 判断当前状态是否可以暂停
     */
    @Override
    public boolean canPause() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * 判断当前状态是否可以快退
     */
    @Override
    public boolean canSeekBackward() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * 判断当前状态是否可以快进
     */
    @Override
    public boolean canSeekForward() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getAudioSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }
}