package fxchat.com.pushlib.jeromq.push;

import android.util.Log;

import org.zeromq.ZMQ;

import java.lang.ref.WeakReference;

/**
 * Created by wenjiarong on 2018/12/23 0023.
 */
public class MonitorRunnable implements Runnable {
    WeakReference<ZMQ.Socket> socketWeakReference;
    final String TAG_MONITOR;
    final String monitorHost;

    public MonitorRunnable(ZMQ.Socket monitor, String monitorHost, String tag) {
        this.socketWeakReference = new WeakReference<>(monitor);
        this.monitorHost = monitorHost;
        TAG_MONITOR = tag;
    }

    @Override
    public void run() {
        Log.e(TAG_MONITOR, "start monitor :" + this.toString());
        try {

            //连接当前socket的监听
            socketWeakReference.get().connect(monitorHost);
            while (!Thread.currentThread().isInterrupted()) {
                Log.e(TAG_MONITOR, "monitor :" + this.toString());

                //从当前monitor里面读取event
                zmq.ZMQ.Event event = zmq.ZMQ.Event.read(socketWeakReference.get().base());
                if (event == null) {
                    Log.e(TAG_MONITOR, "monitor break:" + this.toString());
                    break;
                }
                String log;
                switch (event.event) {
                    case zmq.ZMQ.ZMQ_EVENT_CONNECTED:
                        log = "链接已建立";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_CONNECT_DELAYED:
                        log = "同步连接失败，仍在进行重试";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_CONNECT_RETRIED:
                        log = "尝试异步连接/重连";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_LISTENING:
                        log = "已经绑定了某个地址，准备好接受连接请求";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_BIND_FAILED:
                        log = "无法绑定在这个地址上";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_ACCEPTED:
                        log = "连接请求被接受";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_ACCEPT_FAILED:
                        log = "无法接受客户端的连接请求";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_CLOSED:
                        log = "连接关闭";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_CLOSE_FAILED:
                        log = "连接无法被关闭";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_DISCONNECTED:
                        log = "会话被破坏";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_MONITOR_STOPPED:
                        log = "监视结束";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_HANDSHAKE_PROTOCOL:
                        log = "握手协议";
                        break;
                    case zmq.ZMQ.ZMQ_EVENT_ALL:
                        log = "ZMQ_EVENT_ALL";
                        break;
                    default:
                        log = "undefine";
                        break;
                }
                Log.e(TAG_MONITOR, Thread.currentThread() + "  log:" + log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.e(TAG_MONITOR, "end monitor :" + this.toString());
            ZMQ.Socket monitor = socketWeakReference.get();
            if (monitor != null) {
                socketWeakReference.get().disconnect(monitorHost);
                socketWeakReference.get().close();
            }
        }
    }
}
