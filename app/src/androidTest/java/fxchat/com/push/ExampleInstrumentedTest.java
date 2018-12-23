package fxchat.com.push;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import fxchat.com.pushlib.jeromq.ZeroMQBroker;
import fxchat.com.pushlib.jeromq.push.BaseZeroMQ;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    List<String> list = new ArrayList<>();
    boolean isCheck = false;

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("fxchat.com.push", appContext.getPackageName());


    }

    @Test
    public void checkPushKeepLive() {
        list.add("Dow 30");
        ZeroMQBroker.getInstance().start();
        ZeroMQBroker.getInstance().subscribe(list);
        CountDownLatch latch = new CountDownLatch(1);
        ZeroMQBroker.getInstance().addDataReceiveObserver(new BaseZeroMQ.DataReceiveObserver() {
            @Override
            public void onDataReceive(List<String> datas) {
                if (isCheck){
                    Assert.assertTrue(datas != null && !datas.isEmpty() && datas.get(0).contains("Dow 30"));
                }
            }
        });
        Thread thread = new Thread(new Runnable() {
            long time = 5000000;
            long waitTime = 100;
            double multiple = 1;

            @Override
            public void run() {
                while (time > 0) {
                    multiple = Math.pow(multiple, 1.1);
                    int realWaitTime = (int) (waitTime * multiple);
                    synchronized (this) {
                        try {
                            this.wait(realWaitTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        time -= realWaitTime;
                    }
                    isCheck = true;
                }
                latch.countDown();
            }
        });
        thread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
