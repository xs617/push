package fxchat.com.pushlib.jeromq;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fxchat.com.common.StringUtil;
import fxchat.com.pushlib.jeromq.push.BaseZeroMQ;
import fxchat.com.pushlib.jeromq.push.IPush;

/**
 * Created by wenjiarong on 2018/11/16 0016.
 */
public class ZeroMQBroker extends BaseZeroMQ {
    private List<String> oldData = new ArrayList<>();
    private static ZeroMQBroker instance;

    private ZeroMQBroker() {

    }

    public static IPush getInstance() {
        if (instance == null) {
            synchronized (ZeroMQBroker.class) {
                if (instance == null) {
                    instance = new ZeroMQBroker();
                }
            }
        }
        return instance;
    }

    @Override
    protected String distributeHost() {
        return "tcp://10.0.0.33:5558";
    }

    @Override
    protected String distributeMonitorHost() {
        return "inproc://reqmoniter_broker_" + StringUtil.getMD5(getCurrentHost());
    }

    @Override
    protected void onDataReceive(List<String> datas) {
        super.onDataReceive(datas);
        if (datas.size() != 2) {
            return;
        }
        String newReceiveSub = datas.get(0);
        String newReceiveData = datas.get(1);
        if (oldData.size() == 2
                && oldData.get(0).equals(newReceiveSub)
                && oldData.get(1).equals(newReceiveData)) {
            Log.e(TAG_REC, "相同数据 ");
            return;
        }
        oldData.clear();
        oldData.addAll(datas);
        //实时推送数据处理
//        EventBus.getDefault().post(new Gson().fromJson(newReceiveData,BrokerRealDataModelEvent.class));
    }


}
