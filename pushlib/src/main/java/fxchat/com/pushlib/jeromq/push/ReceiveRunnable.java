package fxchat.com.pushlib.jeromq.push;

import android.util.Log;

import org.zeromq.ZMQ;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenjiarong on 2018/12/23 0023.
 */
public class ReceiveRunnable implements Runnable {

    final WeakReference<ZMQ.Socket> socketWeakReference;
    final WeakReference<BaseZeroMQ> zeroMQWeakReference;
    final String host;
    final String TAG_REC;

    public ReceiveRunnable(BaseZeroMQ zeroMQ, ZMQ.Socket caller, String host, String tag) {
        this.socketWeakReference = new WeakReference<>(caller);
        this.zeroMQWeakReference = new WeakReference<>(zeroMQ);
        this.host = host;
        this.TAG_REC = tag;
    }

    @Override
    public void run() {
        Log.e(TAG_REC, "start rec :" + this.toString());
        try {
            if (socketWeakReference.get().connect(host)) {
                Log.e(TAG_REC, "conn success" + host);

                List<String> receiveDataList = new ArrayList<>();
                while (!Thread.currentThread().isInterrupted()) {
                    receiveDataList.clear();
                    do {
                        String recData = socketWeakReference.get().recvStr();
                        receiveDataList.add(recData);
                        Log.e(TAG_REC, recData);
                    } while (socketWeakReference.get().hasReceiveMore());

                    zeroMQWeakReference.get().onDataReceive(receiveDataList);
                }

                Log.e(TAG_REC, "conn thread end" + host);
            } else {
                //TODO 连接失败干啥？
                Log.e(TAG_REC, "conn fail" + host);
            }
        } catch (Exception e) {
            //TODO 连接也可能抛异常，接收数据也可能抛异常，连接的异常是否需要额外处理？
            //断开连接时接收数据失败，结束循环
            e.printStackTrace();
        } finally {
            Log.e(TAG_REC, "end rec :" + this.toString());
            ZMQ.Socket caller = socketWeakReference.get();
            if (caller != null) {
                caller.disconnect(host);
                caller.close();
            }
        }
    }
}
