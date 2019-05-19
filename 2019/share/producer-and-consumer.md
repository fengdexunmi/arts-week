# 生产者与消费者问题

第一次接触生产者与消费者问题，是在《操作系统原理》这门课上。于是我在cousera上找了下操作系统原理相关的课。传送门=> https://zh.coursera.org/lecture/os-pku/jin-cheng-de-bing-fa-zhi-xing-QTvLa

使用Java来解决生产者与消费者问题，可以参考掘金网的一篇文章 https://juejin.im/entry/596343686fb9a06bbd6f888c

文章中使用5种方式来解决生产者和消费者问题

- wait()和notify()

- 可重入锁ReentrantLock

- 阻塞队列BlockingQueue

- 信号量Semaphore

- 管道

