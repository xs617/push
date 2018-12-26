package fxchat.com.pushlib.jeromq.selfinspection;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by wenjiarong on 2018/12/26 0026.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SelfInspectionService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        SelfInspectionHelperHelper.getInstance().executeSelfInspection();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}