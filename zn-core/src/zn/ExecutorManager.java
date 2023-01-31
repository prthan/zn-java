package zn;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import zn.model.ExecutorConfig;

public class ExecutorManager 
{
  private static final Logger LOG=Logger.get(ExecutorManager.class);

  private BlockingQueue<Runnable> executionQueue;
  private ThreadPoolExecutor tpe;
  private ThreadFactory threadFactory;
  private int threadCounter=0;
  
  public ExecutorManager(String configName)
  {
    Configuration config=Configuration.getInstance();
    ExecutorConfig executorConfig=config.$(configName, ExecutorConfig.class);

    threadFactory=new ThreadFactory() 
    {
      public Thread newThread(Runnable runnable) 
      {
        String threadName="executor-thread-"+threadCounter++;
        LOG.debug("creating execution thread "+threadName);
        Thread t=new Thread(runnable);
        t.setPriority(Thread.MIN_PRIORITY);
        t.setName(threadName);
        return t;
      }
    };
    executionQueue=new LinkedBlockingQueue<Runnable>(executorConfig.getDepth());
    RejectedExecutionHandler rejHandler=new RejectedExecutionHandler()
    {
      public void rejectedExecution(Runnable runnable, ThreadPoolExecutor tpe) 
      {
        LOG.error("execution rejected", runnable);
      }
    };
    
    tpe=new ThreadPoolExecutor(executorConfig.getMin(), 
                                           executorConfig.getMax(), 
                                           executorConfig.getIdleTime(), 
                                           TimeUnit.SECONDS, 
                                           executionQueue,
                                           threadFactory);
    // tpe=(ThreadPoolExecutor)Executors.newFixedThreadPool(2);
    tpe.setRejectedExecutionHandler(rejHandler);
  }

  public void submit(Runnable runnable)
  {
    LOG.debug("submiting runnable to thread pool");
    tpe.execute(runnable);
  }

  public void submit(Callable<?> callable)
  {
    LOG.debug("submiting callable to thread pool");
    tpe.submit(callable);
  }

  public void start()
  {
    
  }

  public void stop()
  {
    tpe.shutdownNow();
  }
}
