package com.ch.mhy.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager;

public class ScreenBrightnessTool {
    /**
     * Activty自动调节亮度模式
     */
    public static final int ACTIVITY_BRIGHTNESS_AUTOMATIC = -1;
    /**
     * 自动调节模式
     */
    public static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
    /**
     * 手动调节模式
     */
    public static final int SCREEN_BRIGHTNESS_MODE_MANUAL = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
    /**
     * 默认亮度
     */
    public static final int SCREEN_BRIGHTNESS_DEFAULT = 75;
    /**
     * 最大亮度
     */
    public static final int MAX_BRIGHTNESS = 100;
    /**
     * 最小亮度
     */
    public static final int MIN_BRIGHTNESS = 0;

    private static final int mMaxBrighrness = 255;
    private static final int mMinBrighrness = 120;

    // 当前系统调节模式
    private boolean sysAutomaticMode;
    // 当前系统亮度值
    private int sysBrightness;

    private Context context;

    private ScreenBrightnessTool(Context context, int sysBrightness,
                                 boolean sysAutomaticMode) {
        this.context = context;
        this.sysBrightness = sysBrightness;
        this.sysAutomaticMode = sysAutomaticMode;
    }

    /**
     * 创建屏幕亮度工具
     *
     * @param context
     * @return
     */
    public static ScreenBrightnessTool Builder(Context context) {
        int brightness;
        boolean automaticMode;
        try {
            // 获取当前系统亮度值
            brightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            // 获取当前系统调节模式
            automaticMode = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            return null;
        }

        return new ScreenBrightnessTool(context, brightness, automaticMode);
    }

    /**
     * 返回当前系统亮度调节模式
     *
     * @return
     */
    public boolean getSystemAutomaticMode() {
        return sysAutomaticMode;
    }

    /**
     * 返回当前系统亮度值
     *
     * @return
     */
    public int getSystemBrightness() {
        return sysBrightness;
    }

    /**
     * 设置调节模式
     *
     * @param mode 调节模式
     */
    public void setMode(int mode) {
        if (mode != SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                && mode != SCREEN_BRIGHTNESS_MODE_MANUAL)
            return;

        sysAutomaticMode = mode == SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    /**
     * 设置屏幕亮度
     *
     * @param brightness 亮度值,值为0至100
     */
    public void setBrightness(int brightness) {
        int mid = mMaxBrighrness - mMinBrighrness;
        int bri = (int) (mMinBrighrness + mid * ((float) brightness)
                / MAX_BRIGHTNESS);

        ContentResolver resolver = context.getContentResolver();
        Settings.System
                .putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, bri);
    }

    /**
     * 亮度预览
     *
     * @param activity   预览activity
     * @param brightness 亮度值（0.47~1）
     */
    public static void brightnessPreview(Activity activity, float brightness) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness;
        window.setAttributes(lp);
    }

    /**
     * 亮度预览
     *
     * @param activity 预览activity
     * @param percent  百分比（0.0~1.00）
     */
    public static void brightnessPreviewFromPercent(Activity activity,
                                                    float percent) {
        float brightness = percent + (1.0f - percent)
                * (((float) mMinBrighrness) / mMaxBrighrness);
        brightnessPreview(activity, brightness);
    }


//	有用 的

    /**
     * 判断是否开启了自动亮度调节
     *
     * @param aContext
     * @return
     */
    public static boolean isAutoBrightness(ContentResolver aContentResolver) {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    /**
     * 获取屏幕的亮度
     *
     * @param activity
     * @return
     */
    public static int getScreenBrightness(Activity activity) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = activity.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(
                    resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    /**
     * 设置亮度
     *
     * @param activity
     * @param brightness
     */
    public static void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 停止自动亮度调节
     *
     * @param activity
     */
    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 开启亮度自动调节
     *
     * @param activity
     */
    public static void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     * 保存亮度设置状态
     *
     * @param resolver
     * @param brightness
     */
    public static void saveBrightness(ContentResolver resolver, int brightness) {
        Uri uri = android.provider.Settings.System
                .getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(resolver, "screen_brightness",
                brightness);
        resolver.notifyChange(uri, null);
    }

}
