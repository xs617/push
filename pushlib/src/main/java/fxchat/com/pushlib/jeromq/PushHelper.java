package fxchat.com.pushlib.jeromq;

import android.app.Application;
import android.content.Context;

/**
 * Created by wenjiarong on 2018/12/26 0026.
 */
public class PushHelper {
    /**
     * application的上下文
     */
    private static Context mApplicationContext;

    public static Context getApplicationContext() {
        return mApplicationContext;
    }

    public static void init(Application application) {
        PushHelper.mApplicationContext = application.getApplicationContext();
    }
}
