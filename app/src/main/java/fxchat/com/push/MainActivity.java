package fxchat.com.push;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import fxchat.com.pushlib.jeromq.ZeroMQBroker;
import fxchat.com.pushlib.jeromq.PushHelper;

public class MainActivity extends AppCompatActivity {
    List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list.add("Dow 30");

        PushHelper.init(getApplication());

        ZeroMQBroker.getInstance().start();

        ZeroMQBroker.getInstance().subscribe(list);
    }
}
