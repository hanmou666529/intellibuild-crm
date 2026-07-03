package com.ruoyi.framework.manager;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.ruoyi.common.utils.Threads;
import com.ruoyi.common.utils.spring.SpringUtils;

public class AsyncManager
{
    private final int OPERATE_DELAY_TIME = 10;

    private volatile ScheduledExecutorService executor;

    private AsyncManager() {}

    private static class Holder
    {
        private static final AsyncManager INSTANCE = new AsyncManager();
    }

    public static AsyncManager me()
    {
        return Holder.INSTANCE;
    }

    private ScheduledExecutorService getExecutor()
    {
        if (executor == null)
        {
            synchronized (this)
            {
                if (executor == null)
                {
                    executor = SpringUtils.getBean("scheduledExecutorService");
                }
            }
        }
        return executor;
    }

    public void execute(TimerTask task)
    {
        getExecutor().schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    public void shutdown()
    {
        Threads.shutdownAndAwaitTermination(getExecutor());
    }
}
