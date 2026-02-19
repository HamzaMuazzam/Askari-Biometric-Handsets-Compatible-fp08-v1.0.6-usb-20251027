package com.eyecool.iris;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *      封装一个适用全局的音频播放工具类
 *
 * 创建人：linmutang
 * 创建时间：9/15/21
 */
public class SoundPlayUtils {

    private static SoundPool sSoundPool = null;
    private static Context context;
    private static Map<String,Sound> sMap = new HashMap<>();


    /**
     *      所有的音频资源都在这里加载
     */
    public static void init(Context ctx) {
        context = ctx;
        sSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        addSound(R.raw.enrollment_succeeded);
        addSound(R.raw.enrollment_failed);
        addSound(R.raw.failed_to_get_image);
        addSound(R.raw.get_closer);
        addSound(R.raw.move_backwards);
        addSound(R.raw.no_iris_detected);
        addSound(R.raw.no_iris_please_enroll);
        addSound(R.raw.please_open_eyes_wide);
        addSound(R.raw.too_far);
        addSound(R.raw.verification_failed);
        addSound(R.raw.verification_succeeded);
        //addSound(R.raw.music_setting_bg);
        //addSound(R.raw.music_setting_sound);
    }

    /**
     *      添加新的音频
     *
     * @param resId
     */
    private static void addSound(int resId) {
        Sound sound = new Sound();
        String id = String.valueOf(resId);
        sound.soundId = sSoundPool.load(context, resId, 0);
        sMap.put(id, sound);
    }


    /**
     *      播放歌曲
     * @param resId     R.raw.  资源
     * @param volume    音量 0-1
     * @param loop      循环次数  -1为无限循环、1为循环一次即播放两次
     */
    public static void loadAndPlay(int resId, float volume, int loop) {
        String id = String.valueOf(resId);
        if (!sMap.containsKey(id)) {
            return;
        }
        Sound sound = sMap.get(id);
        Log.d("hello",""+id);
        sound.streamId = sSoundPool.play(sound.soundId, volume, volume, 0, loop, 1);
        sound.volume = volume;
        sound.loop = loop;
    }

    /**
     *      设置音量
     * @param resId     R.raw.  资源
     * @param volume    音量 0-1
     */
    public static void setVolume(int resId, float volume) {
        String id = String.valueOf(resId);
        Sound sound = sMap.get(id);
        if (sound == null || sound.streamId == -1) {
            return;
        }
        sSoundPool.setVolume(sound.streamId, volume, volume);
        sound.volume = volume;
    }


    /**
     *      停止
     * @param resId
     */
    public static void setStop(int resId) {
        String id = String.valueOf(resId);
        Sound sound = sMap.get(id);
        if (sound == null || sound.streamId == -1) {
            return;
        }
        sSoundPool.stop(sound.streamId);
    }

    /**
     *      设置循环次数
     * @param resId     R.raw.  资源
     * @param loop      音量 0-1
     */
    public static void setLoop(int resId, int loop) {
        String id = String.valueOf(resId);
        Sound sound = sMap.get(id);
        if (sound == null || sound.streamId == -1) {
            return;
        }
        sSoundPool.pause(sound.streamId);
        sSoundPool.setLoop(sound.streamId, loop);
        sSoundPool.resume(sound.streamId);
    }


    /**
     *      释放资源
     */
    public static void releaseAll() {
        sMap.clear();
        if (sSoundPool == null) {
            return;
        }
        sSoundPool.release();
        sSoundPool = null;
    }

    static class Sound{
        public int soundId = -1;
        public int streamId = -1;
        public int loop = 0;
        public float volume = 1f;
    }



}
