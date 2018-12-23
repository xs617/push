package fxchat.com.pushlib.jeromq.push;

import java.util.List;

import fxchat.com.pushlib.jeromq.ZeroMQBroker;

/**
 * Created by wenjiarong on 2018/11/9 0009.
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
    void addDataReceiveObserver(ZeroMQBroker.DataReceiveObserver dataReceiveObserver);
}
