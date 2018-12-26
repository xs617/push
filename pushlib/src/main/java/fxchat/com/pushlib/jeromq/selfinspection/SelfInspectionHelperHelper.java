package fxchat.com.pushlib.jeromq.selfinspection;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import fxchat.com.pushlib.jeromq.ZeroMQBroker;
import fxchat.com.pushlib.jeromq.PushHelper;

/**
 * Created by wenjiarong on 2018/12/26 0026.
 * 自检操作帮助类
 */
public class SelfInspectionHelperHelper implements ISelfInspectionHelper {
    private static int JOB_ID = 110;
    private static ISelfInspectionHelper instance;
    private List<SelfInspectionObserver> selfInspectionObservers = new ArrayList<>();

    private SelfInspectionHelperHelper() {

    }

    public static ISelfInspectionHelper getInstance() {
        if (instance == null) {
            synchronized (ZeroMQBroker.class) {
                if (instance == null) {
                    instance = new SelfInspectionHelperHelper();
                }
            }
        }
        return instance;
    }

    public void start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Context context = PushHelper.getApplicationContext();
            if (context != null) {
                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, SelfInspectionService.class)).setPeriodic(3000).build();
                jobScheduler.schedule(jobInfo);
            }
        }
    }

    public void stop() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Context context = PushHelper.getApplicationContext();
            if (context != null) {
                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                //判断是否所有还没有结束的推送
                jobScheduler.cancel(JOB_ID);
            }
        }
    }

    @Override
    public void registerSelfInspectionObserver(SelfInspectionObserver selfInspectionObserver) {
        this.selfInspectionObservers.add(selfInspectionObserver);
        if (!selfInspectionObservers.isEmpty()) {
            start();
        }
    }

    @Override
    public void unregisterSelfInspectionObserver(SelfInspectionObserver selfInspectionObserver) {
        this.selfInspectionObservers.remove(selfInspectionObserver);
        if (selfInspectionObservers.isEmpty()) {
            stop();
        }
    }

    @Override
    public void executeSelfInspection() {
        for (SelfInspectionObserver selfInspectionObserver : selfInspectionObservers) {
            selfInspectionObserver.onSelfInspection();
        }
    }
}
