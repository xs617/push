package fxchat.com.pushlib.jeromq.selfinspection;

/**
 * Created by wenjiarong on 2018/12/26 0026.
 */
public interface ISelfInspectionHelper {

    void registerSelfInspectionObserver(SelfInspectionObserver selfInspectionObserver);

    void unregisterSelfInspectionObserver(SelfInspectionObserver selfInspectionObserver);

    void executeSelfInspection();
}
