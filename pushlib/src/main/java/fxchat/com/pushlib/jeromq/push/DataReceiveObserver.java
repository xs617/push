package fxchat.com.pushlib.jeromq.push;

import java.util.List;

/**
 * Created by wenjiarong on 2018/12/26 0026.
 */
public interface DataReceiveObserver {
    void onDataReceive(List<String> datas);
}
