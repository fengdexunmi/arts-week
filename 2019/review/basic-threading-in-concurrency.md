# Concurrency - Basic Threading

本节内容来自《Thinking-in-Java》的Concurrency章节。


## Defining tasks

```Java

/**
 * LiftOff
 */
public class LiftOff implements Runnable {
    protected int countDown = 10; // Default
    private static int taskCount = 0;
    private final int id = taskCount++;
    
    public LiftOff() {}

    public LiftOff(int countDown) {
        this.countDown = countDown;
    }

    public String status() {
        return "#" + id + "(" + (countDown > 0 ? countDown : "LiftOff!") + "), ";
    }

    @Override
    public void run() {
        while (countDown-- > 0) {
            System.out.print(status());
            Thread.yield();
        }
    }
}

```

衍生于Runnable的类必须重写`run()`方法。

`Thread.yield()`可翻译为线程让步，使得当前线程从执行状态变为可执行状态，让出CPU时间，使得CPU切换到其他线程（或者自己）。

接下来，我们让**LiftOff**运行起来。

```Java
/**
 * MainThread
 */
public class MainThread {
    public static void main(String[] args) {
        LiftOff launch = new LiftOff();
        launch.run();
    }
}
```
```
// 输出结果
#0(9), #0(8), #0(7), #0(6), #0(5), #0(4), #0(3), #0(2), #0(1), #0(LiftOff!), 
```

## The Thread class

通过将`Runnable`传递到`Thread`的构造函数，可以将Runnable运行起来。

```Java
/**
 * BasicThreads
 */
public class BasicThreads {
    public static void main(String[] args) {
        Thread t = new Thread(new LiftOff());
        t.start();
        System.out.println("Waiting for LiftOff");
    }
}

```
```
// 输出结果
Waiting for LiftOff
#0(9), #0(8), #0(7), #0(6), #0(5), #0(4), #0(3), #0(2), #0(1), #0(LiftOff!),
```
调用`Thread`的`start()`方法会执行必要的线程初始化，然后调用`Runnable`的`run()`方法在新线程中执行任务。

可以看到先输出的是"Waiting for LiftOff"，这里从主线程中启动了一个新的线程，当主线程执行完毕后，再执行其他任务。当然存在多个CPU时两个线程是可以同时进行的。

我们对上述列子进行一个改造。
```Java
/**
 * BasicThreads
 */
public class BasicThreads {
    public static void main(String[] args) {
        Thread t = new Thread(new LiftOff());
        t.start();
        for (int i = 0; i < 100; i++) {
            System.out.println(i + " Waiting for LiftOff");
        }
    }
}

```
```
// 部分输出结果
...
96 Waiting for LiftOff
97 Waiting for LiftOff
98 Waiting for LiftOff
99 Waiting for LiftOff
#0(8), #0(7), #0(6), #0(5), #0(4), #0(3), #0(2), #0(1), #0(LiftOff!), 
```
仍然是先执行完`main()`线程，再执行新的线程任务。我们将i的最大值调到999，再看看输出结果。
```Java
/**
 * BasicThreads
 */
public class BasicThreads {
    public static void main(String[] args) {
        Thread t = new Thread(new LiftOff());
        t.start();
        for (int i = 0; i < 100; i++) {
            System.out.println(i + " Waiting for LiftOff");
        }
    }
}

```
```
// 部分输出结果
0 Waiting for LiftOff
1 Waiting for LiftOff
2 Waiting for LiftOff
#0(9), 3 Waiting for LiftOff
4 Waiting for LiftOff
5 Waiting for LiftOff
6 Waiting for LiftOff
#0(8), 7 Waiting for LiftOff
#0(7), 8 Waiting for LiftOff
...
996 Waiting for LiftOff
997 Waiting for LiftOff
998 Waiting for LiftOff
999 Waiting for LiftOff
#0(5), #0(4), #0(3), #0(2), #0(1), #0(LiftOff!), 

```
多个CPU下，执行顺序是交替的，或者是同时的。

接下来，我们在`main()`线程中启动多个线程。

```Java
/**
 * MoreBasicThreads
 */
public class MoreBasicThreads {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            new Thread(new LiftOff()).start();
        }
        System.out.println("Waiting for LiftOff");
    }
}

```
```
// 输出结果
#1(9), #0(9), #2(9), #3(9), Waiting for LiftOff
#0(8), #4(9), #2(8), #4(8), #1(8), #4(7), #2(7), #3(8), #0(7), #3(7), #2(6), #4(6), #2(5), #1(7), #2(4), #3(6), #0(6), #3(5), #1(6), #2(3), #4(5), #1(5), #3(4), #0(5), #2(2), #4(4), #1(4), #3(3), #0(4), #2(1), #4(3), #1(3), #3(2), #0(3), #2(LiftOff!), #4(2), #1(2), #0(2), #3(1), #4(1), #1(1), #3(LiftOff!), #0(1), #4(LiftOff!), #1(LiftOff!), #0(LiftOff!),

```

## Using Executors

`Executors`管理异步任务的执行，不需要显式创建`Thread`对象和管理Thread的生命周期。接下来我们以`ExecutorService`(an Executor with a service liftcycle)。

```Java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CachedThreadPool
 */
public class CachedThreadPool {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            exec.execute(new LiftOff());
        }
        exec.shutdown();
    }
}

```

```
// 输出结果
#3(9), #0(9), #1(9), #3(8), #4(9), #2(9), #4(8), #3(7), #1(8), #3(6), #0(8), #1(7), #4(7), #2(8), #3(5), #0(7), #1(6), #4(6), #2(7), #3(4), #1(5), #0(6), #2(6), #4(5), #1(4), #0(5), #3(3), #0(4), #2(5), #1(3), #4(4), #0(3), #3(2), #2(4), #1(2), #4(3), #0(2), #3(1), #2(3), #1(1), #4(2), #0(1), #3(LiftOff!), #2(2), #1(LiftOff!), #4(1), #0(LiftOff!), #2(1), #4(LiftOff!), #2(LiftOff!),

```

`newCachedThreadPool()`，它是一种用来处理大量短时间工作任务的线程池，具有几个鲜明特点：它会试图缓存线程并重用，当无缓存线程可用时，就会创建新的线程；如果线程闲置的时间超过60秒，则会被终止并移出缓存；长时间闲置时，这种线程池，不会消耗什么资源。其内部使用SynchronousQueue作为工作队列。

把`newCachedThreadPool()`换成`newFixedThreadPool()`，再看下输出结果。

```Java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FixedThreadPool
 */
public class FixedThreadPool {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            exec.execute(new LiftOff());
        }
        exec.shutdown();
    }
}

```

```
// 输出结果
#0(9), #1(9), #4(9), #2(9), #0(8), #3(9), #2(8), #0(7), #4(8), #1(8), #4(7), #0(6), #2(7), #0(5), #0(4), #3(8), #0(3), #2(6), #4(6), #1(7), #2(5), #4(5), #2(4), #0(2), #4(4), #3(7), #4(3), #0(1), #2(3), #1(6), #2(2), #0(LiftOff!), #4(2), #3(6), #2(1), #1(5), #4(1), #3(5), #2(LiftOff!), #1(4), #4(LiftOff!), #3(4), #1(3), #1(2), #3(3), #3(2), #1(1), #3(1), #1(LiftOff!), #3(LiftOff!), 

```

`newFixedThreadPool(int nThreads)`，重用指定数目（nThreads）的线程，其背后使用的无界的工作队列，任何时候最多有nThreads个工作线程是活动的。这意味着，如果任务数量超过了指定数目，将在工作队列中等待空闲线程出现，如果有工作线程退出，将会有新的工作线程被创建，以补足指定的数目nThreads。

再使用`newSingleThreadExecutor()`代替`newFixedThreadPool(int nThreads)`，看看输出结果。

```Java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SingleThreadExecutor
 */
public class SingleThreadExecutor {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 5; i++) {
            exec.execute(new LiftOff());
        }
        exec.shutdown();
    }
}

```

```
// 输出结果
#0(9), #0(8), #0(7), #0(6), #0(5), #0(4), #0(3), #0(2), #0(1), #0(LiftOff!), #1(9), #1(8), #1(7), #1(6), #1(5), #1(4), #1(3), #1(2), #1(1), #1(LiftOff!), #2(9), #2(8), #2(7), #2(6), #2(5), #2(4), #2(3), #2(2), #2(1), #2(LiftOff!), #3(9), #3(8), #3(7), #3(6), #3(5), #3(4), #3(3), #3(2), #3(1), #3(LiftOff!), #4(9), #4(8), #4(7), #4(6), #4(5), #4(4), #4(3), #4(2), #4(1), #4(LiftOff!), 

```

`newSingleThreadExecutor()`，它的特点在于工作线程数目被限制为1，操作一个无界的工作队列，所以它保证了所有任务都是被顺序执行，最多有一个任务处于活动状态。

## Producing return values from tasks

`Runnable`执行完任务后不会返回结果，如果你想在任务完成后返回结果可以使用`Callable`。代码如下：

```Java
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * CallableDemo
 */
public class CallableDemo {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        ArrayList<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            results.add(exec.submit(new TaskWithResult(i)));
        }

        for (Future<String> fs : results) {
            try {
                System.out.println(fs.get());
            } catch (InterruptedException e) {
                //TODO: handle exception
            } catch (ExecutionException e) {

            } finally {
                exec.shutdown();
            }

        }
    }
}

class TaskWithResult implements Callable<String> {

    private int id;

    public TaskWithResult(int id) {
        this.id = id;
    }

    @Override
    public String call() throws Exception {
        return "result of TaskWithResult " + id;
    }
}

```

```
// 输出结果
result of TaskWithResult 0
result of TaskWithResult 1
result of TaskWithResult 2
result of TaskWithResult 3
result of TaskWithResult 4
result of TaskWithResult 5
result of TaskWithResult 6
result of TaskWithResult 7
result of TaskWithResult 8
result of TaskWithResult 9

```

`submit()`方法可以生成一个`Future`对象，`get()`方法会阻塞当前线程直到结果返回，可以给get()设置一个timeout。
