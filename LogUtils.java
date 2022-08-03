package com.mm.common.utils;

import android.os.Debug;
import android.util.Log;

import com.mm.base.base.BaseApplication;
import com.mm.common.BuildConfig;

import java.io.File;


/**
 * Created by mmb on 02/09/16.
 * https://www.cnblogs.com/Westfalen/p/4455565.html
 */
public class LogUtils {
    public static boolean isDebug = BuildConfig.DEBUG;
    private static final String TAG = "mmb";
    private static final String TAG2 = "fung";
    static public void println(String str) {
        e(str);
    }
    static public void fungLog(String str) {
        if (isDebug) {
            Log.e(TAG2, getMsgFormat(str));
        }
    }

    /**
     * 获取相关数据:类名,方法名,行号等.用来定位行<br>
     * at cn.utils.MainActivity.onCreate(MainActivity.java:17) 就是用來定位行的代碼<br>
     *
     * @return [ Thread:main, at
     * cn.utils.MainActivity.onCreate(MainActivity.java:17)]
     */
    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts != null) {
            for (StackTraceElement st : sts) {
                if (st.isNativeMethod()) {
                    continue;
                }
                if (st.getClassName().equals(Thread.class.getName())) {
                    continue;
                }
                if (st.getClassName().equals(LogUtils.class.getName())) {
                    continue;
                }
                return "at " + st.getClassName() + "." + st.getMethodName()
                        + "(" + st.getFileName() + ":" + st.getLineNumber() + ")";
            }
        }
        return null;
    }

    public static void e(String msg) {
        if (isDebug) {
            Log.e(TAG, getMsgFormat(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, getMsgFormat(msg));
        }
    }

    /**
     * 输出格式定义
     */
    private static String getMsgFormat(String msg) {
        return getFunctionName() + ": " + msg;
    }

    public static void startTrace() {
        File file = new File(BaseApplication.getContext().getExternalFilesDir("android"), "bbbmethods.trace");
        Debug.startMethodTracing(file.getAbsolutePath(), 100 * 1024 * 1024);
    }
    public static void stopTrace() {
        Debug.stopMethodTracing();
    }
}
