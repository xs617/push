package fxchat.com.pushlib.jeromq.push;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fxchat.com.pushlib.jeromq.ZeroMQBroker;
import fxchat.com.pushlib.jeromq.tools.SimpleThreadFactory;

/**
 * Created by wenjiarong on 2018/11/9 0009.
 */
public abstract class BaseZeroMQ implements IPush {

    //region 日志标签

    protected final String TAG = "ZMQ :" + this.getClass().getSimpleName();
    protected final String TAG_SUB = "ZMQ_SUB :" + this.getClass().getSimpleName();
    protected final String TAG_REC = "ZMQ_REC :" + this.getClass().getSimpleName();
    protected final String TAG_MONITOR = "ZMQ_MONITOR :" + this.getClass().getSimpleName();
    protected final String TAG_QUEUE = "ZMQ_QUEUE :" + this.getClass().getSimpleName();

    //endregion

    //region 内部成员

    private ZMQ.Context mZMQContext;
    /**
     * 与服务器交互的socket
     */
    private ZMQ.Socket mCaller;
    /**
     * 监听caller状态变化的socket
     */
    private ZMQ.Socket mMonitor;
    /**
     * 当前使用的数据推送地址
     */
    private String currentHost = "";
    /**
     * 当前使用的监听地址
     */
    private String currentMonitorHost = "";

    /**
     * 接收数据线程池，暂定两个线程，使用缓存队列
     */
    private ThreadPoolExecutor mReceivePool;
    /**
     * 监听状态线程池，暂定两个线程，使用缓存队列
     */
    private ThreadPoolExecutor mMonitorPool;
    /**
     * 接收数据线程池缓存队列，作为变量主要方便调试
     */
    private BlockingQueue<Runnable> receiveBlockingDeque = new LinkedBlockingDeque<Runnable>();
    /**
     * 监听状态线程池缓存队列，作为变量主要方便调试
     */
    private LinkedBlockingDeque<Runnable> monitorBlockingDeque = new LinkedBlockingDeque<Runnable>();

    /**
     * 启动一个线程承载对ZMQ操作
     */
    private HandlerThread handlerThread;
    /**
     * 所有对zmq操作都在同一个handle中进行，确保串行
     */
    private ZMQHandler zmqHandler;
    /**
     *  数据接收观察者
     */
    private List<DataReceiveObserver> dataReceiveObservers = new ArrayList<>();
    /**
     * 启动ZMQ
     */
    private static final int START = 1;
    /**
     * 停止ZMQ
     */
    private static final int STOP = 2;
    /**
     * 订阅ZMQ
     */
    private static final int SUB = 3;
    /**
     * 取消订阅ZMQ
     */
    private static final int UNSUB = 4;

    //endregion

    @Override
    public void start() {
        Log.e(TAG, "invoke start");
        if (handlerThread != null && handlerThread.isAlive()) {
            handlerThread.quit();
        }
        handlerThread = new HandlerThread(TAG_QUEUE);
        handlerThread.start();
        zmqHandler = new ZMQHandler(handlerThread.getLooper(), this);

        if (mReceivePool == null) {
            synchronized (this) {
                if (mReceivePool == null) {
                    mReceivePool = new ThreadPoolExecutor(
                            2,
                            2,
                            10,
                            TimeUnit.SECONDS,
                            receiveBlockingDeque,
                            new SimpleThreadFactory.Builder().setExtraPrefixName(TAG_REC));
                    mReceivePool.allowCoreThreadTimeOut(true);
                }
            }
        }

        if (mMonitorPool == null) {
            synchronized (this) {
                if (mMonitorPool == null) {
                    mMonitorPool = new ThreadPoolExecutor(
                            2,
                            2,
                            10,
                            TimeUnit.SECONDS,
                            monitorBlockingDeque,
                            new SimpleThreadFactory.Builder().setExtraPrefixName(TAG_MONITOR));
                    mMonitorPool.allowCoreThreadTimeOut(true);
                }
            }
        }
        zmqHandler.sendEmptyMessage(START);
    }

    @Override
    public void stop() {
        Log.e(TAG, "invoke stop");
        //为了保证start和stop是串行执行的，所以在同一个handler中处理
        zmqHandler.sendEmptyMessage(STOP);
    }


    @Override
    public void subscribe(final List<String> symbols) {
        Message message = Message.obtain();
        message.what = SUB;
        message.obj = symbols;
        zmqHandler.sendMessage(message);
    }

    @Override
    public void unsubscribe(final List<String> symbols) {
        Message message = Message.obtain();
        message.what = UNSUB;
        message.obj = symbols;
        zmqHandler.sendMessage(message);
    }

    @Override
    public void addDataReceiveObserver(ZeroMQBroker.DataReceiveObserver dataReceiveObserver) {
         dataReceiveObservers.add(dataReceiveObserver);
    }

    static class ZMQHandler extends Handler {
        WeakReference<BaseZeroMQ> baseZeroMQWeakReference;

        public ZMQHandler(Looper threadLooper, BaseZeroMQ baseBaseZeroMQ) {
            super(threadLooper);
            baseZeroMQWeakReference = new WeakReference<>(baseBaseZeroMQ);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseZeroMQ baseBaseZeroMQ = baseZeroMQWeakReference.get();
            if (baseBaseZeroMQ == null) {
                return;
            }
            switch (msg.what) {
                case START:
                    baseBaseZeroMQ.handlerStart();
                    break;
                case STOP:
                    baseBaseZeroMQ.handleStop();
                    break;
                case SUB:
                    try {
                        baseBaseZeroMQ.handleSub((List<String>) msg.obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case UNSUB:
                    try {
                        baseBaseZeroMQ.handleUnSub((List<String>) msg.obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                default:
                    break;
            }
        }
    }

    private void handlerStart() {
        Log.e(TAG, "execute start");
        //启动一个新的时候先把stop都清掉，避免乱调用stop导致启动了又stop
        zmqHandler.removeMessages(STOP);

        currentHost = distributeHost();
        currentMonitorHost = distributeMonitorHost();
        mZMQContext = ZMQ.context(1);
        mCaller = mZMQContext.socket(SocketType.SUB);
        //设置连接保持
        mCaller.setTCPKeepAlive(1);
        //TCP心跳包时间间隔
        mCaller.setTCPKeepAliveInterval(60);
        //TCP心跳包在空闲时的时间间隔
        mCaller.setTCPKeepAliveIdle(-1);
        //接收缓存大小,设置底层传输Socket的接收缓存大小,初始为0
        mCaller.setReceiveBufferSize(1024);
        //创建接收连接数据的线程
        mReceivePool.execute(new ReceiveRunnable(this, mCaller, getCurrentHost(), TAG_REC));
        mCaller.monitor(getCurrentMonitorHost(), ZMQ.EVENT_ALL);
        //这里创建一个pair类型的socket，用于与上面建立的moniter建立连接
        mMonitor = mZMQContext.socket(SocketType.PAIR);
        //创建监控状态的线程
        mMonitorPool.execute(new MonitorRunnable(mMonitor, getCurrentMonitorHost(), TAG_MONITOR));
    }

    private void handleStop() {
        Log.e(TAG, "execute stop" + "Thread  " + Thread.currentThread());
        handlerThread.quit();
        mZMQContext.close();
        mZMQContext = null;
    }

    private void handleSub(final List<String> symbols) {
        //TODO caller没准备好就订阅的情况
        Log.e(TAG, "execute Sub");
        try {
            for (String aSymbol : symbols) {
                //TODO 如果取消订阅失败了咋办？？
                boolean isUnSub = mCaller.unsubscribe(aSymbol.getBytes());
                Log.e(TAG_SUB, "UnSub : " + aSymbol + ",result----" + isUnSub);
                boolean isSub = mCaller.subscribe(aSymbol.getBytes());
                Log.e(TAG_SUB, "Sub : " + aSymbol + ",result----" + isSub);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUnSub(List<String> symbols) {
        try {
            for (String aSymbol : symbols) {
                //TODO　订阅失败怎么办？
                boolean isUnSub = mCaller.unsubscribe(aSymbol.getBytes());
                Log.e(TAG_SUB, "unSub : " + aSymbol + ",result----" + isUnSub);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected String getCurrentHost() {
        return currentHost;
    }

    protected String getCurrentMonitorHost() {
        return currentMonitorHost;
    }

    /**
     * 接收数据
     *
     * @param receiveData
     */
    protected void onDataReceive(List<String> receiveData){
        for (DataReceiveObserver dataReceiveObserver : dataReceiveObservers) {
            dataReceiveObserver.onDataReceive(receiveData);
        }
    }

    /**
     * 分配host连接地址
     *
     * @return
     */
    protected abstract String distributeHost();

    /**
     * 分配monitor地址
     *
     * @return
     */
    protected abstract String distributeMonitorHost();

    public interface DataReceiveObserver {
        void onDataReceive(List<String> datas);
    }
}
