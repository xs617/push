package fxchat.com.pushlib.jeromq.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wenjiarong on 2018/11/12 0012.
 */
public class SimpleThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    private String extraPrefixName = "";

    private SimpleThreadFactory() {
        SecurityManager var1 = System.getSecurityManager();
        this.group = var1 != null ? var1.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable var1) {
        Thread var2 = new Thread(this.group, var1, extraPrefixName + " - " + this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
        if (var2.isDaemon()) {
            var2.setDaemon(false);
        }

        if (var2.getPriority() != 5) {
            var2.setPriority(5);
        }

        return var2;
    }

    public static class Builder {
        SimpleThreadFactory simpleThreadFactory;

        public Builder() {
            simpleThreadFactory = new SimpleThreadFactory();
        }

        public SimpleThreadFactory setExtraPrefixName(String extraPrefixName) {
            simpleThreadFactory.extraPrefixName = extraPrefixName;
            return simpleThreadFactory;
        }
    }
}
