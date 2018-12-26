package fxchat.com.pushlib.jeromq.push;

import java.util.List;

/**
 * Created by wenjiarong on 2018/11/9 0009.
 * 推送的对外接口，除非万不得已，请勿修改
 */
public interface IPush {

    /**
     * 启动连接
     */
    void start();

    /**
     * 断开连接
     */
    void stop();

    /**
     * 订阅
     * @param datas
     */
    void subscribe(List<String> datas);

    /**
     * 取消订阅
     * @param datas
     */
    void unsubscribe(List<String> datas);

    /**
     * 添加自定义数据接收者
     */
    void addDataReceiveObserver(DataReceiveObserver dataReceiveObserver);
}
