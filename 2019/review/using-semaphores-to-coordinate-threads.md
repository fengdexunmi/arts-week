# Using Binary Semaphores to Coordinate Threads

原文链接： https://android.jlelse.eu/using-semaphore-to-coordinate-threads-3e8c641ffbd9

使用`Semaphores`信号量协调线程

## Semaphores

本文中，我们将使用`Semaphores`信号量协调多线程并发。那么什么是`Semaphores`信号量？
引用*Java Docs*:
>A counting semaphore. Conceptually, a semaphore maintains a set of permits. Each acquire() blocks if necessary until a permit is available, and then takes it. Each release() adds a permit, potentially releasing a blocking acquirer. However, no actual permit objects are used; the Semaphore just keeps a count of the number available and acts accordingly.

信号量维护了一组许可集。`acqurie()`会阻塞，直到许可可用才能获取的到。`release()`添加许可，释放正在阻塞的获取者。`Semaphore`使用的不是实际的许可对象而是维护了计数器来实现。

> A semaphore initialized to one, and which is used such that it only has at most one permit available, can serve as a mutual exclusion lock. This is more commonly known as a binary semaphore, because it only has two states: one permit available, or zero permits available. When used in this way, the binary semaphore has the property (unlike many Lock implementations), that the "lock" can be released by a thread other than the owner (as semaphores have no notion of ownership). This can be useful in some specialized contexts, such as deadlock recovery.

初始许可数量为1的信号量，被称为二元信号量，表示最多只有一个许可可用，相当于互斥锁。不同于其他锁的实现，信号量可以通过线程释放“锁”。信号量是线程共享的。

进入正文：

假设我们有5个线程（A、B、C、D、E）正在从网络下载5个大文件，我们现在需要将这些线程串起来，A线程下载10%后暂停，到B线程下载，下载到10%后暂停，再到C下载，下载到10%后暂停，依次执行下去，直到所有的下载完成。

因为**the “lock” can be released by a thread other than the owner**，当一个线程下载到10%挂起，通过释放下一个信号量“锁住”当前线程。

首先，我们定义5个信号量，第一个信号量初始值为1表示未锁的状态，其他的都是锁定状态。

```
Semaphore[] semaphores = new Semaphore[NUM_PROGRESSBARS];

// notice the first Semaphore is 'unlocked' while the rest are 'locked'
semaphores[0] = new Semaphore(1);
semaphores[1] = new Semaphore(0);
semaphores[2] = new Semaphore(0);
semaphores[3] = new Semaphore(0);
semaphores[4] = new Semaphore(0);
```

我们再定义一个工作线程，该工作线程持有当前的信号量和下一个信号量。因为信号量是线程共享的，所以如果下一个信号量使用了release()方法表示添加了许可，则当前信号量的require()就会阻塞。

```Java
private static class WorkerThread extends Thread {

    private final ProgressBarHandler progressBarHandler;
    private final Semaphore[] semaphores;
    private final int index;
    private int progress;

    public WorkerThread(final ProgressBarHandler progressBarHandler, final Semaphore[] semaphores, final int index) {
        this.progressBarHandler = progressBarHandler;
        this.semaphores = semaphores;
        this.index = index;
    }

    /**
        * We use Semaphores here to coordinate the threads because the Semaphore in java is not 'fully-bracketed',
        * which means the thread to release a permit does not have to be the one that has acquired
        * the permit in the first place.
        * We can utilise this feature of Semaphore to let one thread to release a permit for the next thread.
        */
    @Override
    public void run() {
        final Semaphore currentSemaphore = semaphores[index];
        final Semaphore nextSemaphore = semaphores[(index + 1) % semaphores.length];

        try {
            while (true) {
                currentSemaphore.acquire();

                sleep(1000); // we use sleep call to mock some lengthy work.
                Message message = progressBarHandler.obtainMessage();
                message.arg1 = (progress += 10);
                progressBarHandler.sendMessage(message);

                nextSemaphore.release();

                if (progress == 100) {
                    progress = 0;
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

## 总结
除了Semaphores，还有闭锁CountDownLatch和栅栏CyclicBarrier也可以协调线程的并发。CountDownLatch和CyclicBarrier再另外写文章分析。
