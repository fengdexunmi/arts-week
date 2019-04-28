# 9 ways to avoid memory leaks in Android
原文链接 https://android.jlelse.eu/9-ways-to-avoid-memory-leaks-in-android-b6d81648e35e

我作为一名Android开发人员已经有一段时间了，我发现App开发的大多数时间花费在添加新特性和视觉扩展上面，而不是关注性能和质量问题。这篇文章，我们来关注下**内存泄漏**，最重要的优化技术之一。

## 什么是内存泄漏？
当被分配的内存一直没有释放就会发生内存泄漏。这意味着垃圾回收器没办法清理无用对象。一开始，这个好像不是问题，但是想象一下，如果两个星期都没有清理房间的垃圾会怎样？

类似的，如果用户一直使用App，内存会一直增加并且没有处理内存泄漏，垃圾回收器就不会清理无用的内存。这样内存会一直增加到没有更多的内存可以被分配，最终会导致App崩溃。

## 如何检测内存泄漏？
可以使用[LeakyCanary](https://github.com/square/leakcanary)库帮助我们检测内存泄漏。

## 常见的导致内存泄漏的错误

### 1. Broadcast Receivers
在Activity中注册了一个本地广播接收器，当关闭Activity时，没有取消注册这个广播接收器，那么它会一直持有Activity的引用。

```Java
public class BroadcastReceiverLeakActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
    }

    private void registerBroadCastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //your receiver code goes here!
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("SmsMessage.intent.MAIN"));
    }
    
    
    @Override
    protected void onStart() {
        super.onStart();
        registerBroadCastReceiver();
    }    


    @Override
    protected void onStop() {
        super.onStop();

        /*
         * Uncomment this line in order to avoid memory leak.
         * You need to unregister the broadcast receiver since the broadcast receiver keeps a reference of the activity.
         * Now when its time for your Activity to die, the Android framework will call onDestroy() on it
         * but the garbage collector will not be able to remove the instance from memory because the broadcastReceiver
         * is still holding a strong reference to it.
         * */

        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }
}
```

### 2. 静态Activity或者View引用
当直接或者间接地静态引用了Activity或者View，导致Activity或者View销毁后还在内存中，因为静态变量是在类加载的时候就产生的，它的生命周期比Activity的长。
```Java
public class StaticReferenceLeakActivity extends AppCompatActivity {

    /*  
     * This is a bad idea! 
     */
    private static TextView textView;
    private static Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        
        
        textView = findViewById(R.id.activity_text);
        textView.setText("Bad Idea!");
           
        activity = this;
    }
}
```
如何解决？不要使用静态变量声明View、Activity或者Context

### 3. 单例模式引用
在单例类中传递了context作为参数，考虑下面例子：
```Java
public class SingletonLeakExampleActivity extends AppCompatActivity {

    private SingletonSampleClass singletonSampleClass;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    /* 
     * Option 1: Do not pass activity context to the Singleton class. Instead pass application Context
     */      
        singletonSampleClass = SingletonSampleClass.getInstance(this);
    }
  
    
   @Override
   protected void onDestroy() {
        super.onDestroy();

    /* 
     * Option 2: Unregister the singleton class here i.e. if you pass activity context to the Singleton class, 
     * then ensure that when the activity is destroyed, the context in the singleton class is set to null.
     */
     singletonSampleClass.onDestroy();
   }
}
```

```Java
public class SingletonSampleClass {
  
    private Context context;
    private static SingletonSampleClass instance;
  
    private SingletonSampleClass(Context context) {
        this.context = context;
    }

    public synchronized static SingletonSampleClass getInstance(Context context) {
        if (instance == null) instance = new SingletonSampleClass(context);
        return instance;
    }
  
    public void onDestroy() {
       if(context != null) {
          context = null; 
       }
    }
}
```

如何解决？
- 使用ApplicationContext代替Activity Context
- 确保传递的Activity Context在Activity销毁的时候被置为null

### 4. 非静态内部类引用
内部类隐式持有外部类的引用
```Java
public class InnerClassReferenceLeakActivity extends AppCompatActivity {

  /* 
   * Mistake Number 1: 
   * Never create a static variable of an inner class
   * Fix I:
   * private LeakyClass leakyClass;
   */
  private static LeakyClass leakyClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        
        new LeakyClass(this).redirectToSecondScreen();

        /*
         * Inner class is defined here
         * */
         leakyClass = new LeakyClass(this);
         leakyClass.redirectToSecondScreen();
    }
    
  /* 
   * Mistake Number 2: 
   * 1. Never create a inner variable of an inner class
   * 2. Never pass an instance of the activity to the inner class
   */       
    private class LeakyClass {
        
        private Activity activity;
        public LeakyClass(Activity activity) {
            this.activity = activity;
        }
        
        public void redirectToSecondScreen() {
            this.activity.startActivity(new Intent(activity, SecondActivity.class));
        }
    }
}
```
如何解决？
- 不要创建静态的变量（内部类变量）
- 声明内部类为静态类型
- 使用Activity或则View的弱引用代替强引用，代码如下：
```Java
public class InnerClassReferenceLeakActivity extends AppCompatActivity {

  /* 
   * Mistake Number 1: 
   * Never create a static variable of an inner class
   * Fix I:
   */
  private LeakyClass leakyClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        
        new LeakyClass(this).redirectToSecondScreen();

        /*
         * Inner class is defined here
         * */
         leakyClass = new LeakyClass(this);
         leakyClass.redirectToSecondScreen();
    }
  
  
    /*  
     * How to fix the above class:
     * Fix memory leaks:
     * Option 1: The class should be set to static
     * Explanation: Instances of anonymous classes do not hold an implicit reference to their outer class 
     * when they are "static".
     *
     * Option 2: Use a weakReference of the textview or any view/activity for that matter
     * Explanation: Weak References: Garbage collector can collect an object if only weak references 
     * are pointing towards it.
     * */
    private static class LeakyClass {
        
        private final WeakReference<Activity> messageViewReference;
        public LeakyClass(Activity activity) {
            this.activity = new WeakReference<>(activity);
        }
        
        public void redirectToSecondScreen() {
            Activity activity = messageViewReference.get();
            if(activity != null) {
               activity.startActivity(new Intent(activity, SecondActivity.class));
            }
        }
    }  
}
```

### 5. 匿名内部类

和上面类似，匿名内部类也隐式持有外部类的引用，解决方法和上面一样。

```Java
public class AnonymousClassReferenceLeakActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);


        textView = findViewById(R.id.activity_text);
        textView.setText(getString(R.string.text_inner_class_1));
        findViewById(R.id.activity_dialog_btn).setVisibility(View.INVISIBLE);

        /*
         * Runnable class is defined here
         * */
         new Thread(new LeakyRunnable(textView)).start();
    }



    private static class LeakyRunnable implements Runnable {

        private final WeakReference<TextView> messageViewReference;
        private LeakyRunnable(TextView textView) {
            this.messageViewReference = new WeakReference<>(textView);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TextView textView = messageViewReference.get();
            if(textView != null) {
                textView.setText("Runnable class has completed its work");
            }
        }
    }
}
```

### 6. 使用AsyncTask要避免内存泄漏
考虑以下代码

```Java
public class AsyncTaskReferenceLeakActivity extends AppCompatActivity {

    private TextView textView;
    private BackgroundTask backgroundTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        /*
         * Executing AsyncTask here!
         * */
        backgroundTask = new BackgroundTask(textView);
        backgroundTask.execute();
    }

    /*
     * Couple of things we should NEVER do here:
     * Mistake number 1. NEVER reference a class inside the activity. If we definitely need to, we should set the class as static as static inner classes don’t hold
     *    any implicit reference to its parent activity class
     * Mistake number 2. We should always cancel the asyncTask when activity is destroyed. This is because the asyncTask will still be executing even if the activity
     *    is destroyed.
     * Mistake number 3. Never use a direct reference of a view from acitivty inside an asynctask.
     * */
 private class BackgroundTask extends AsyncTask<Void, Void, String> {    
        @Override
        protected String doInBackground(Void... voids) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "The task is completed!";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textView.setText(s);
        }
    }

```
如何解决？
- 我们应该在Activity销毁时取消asyncTask
- 静态内部类代替非静态内部类
- 使用WeakReference

### 7. Handler引用
```Java
public class HandlersReferenceLeakActivity extends AppCompatActivity {

    private TextView textView;

    /*
     * Mistake Number 1
     * */
     private Handler leakyHandler = new Handler();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        /*
         * Mistake Number 2
         * */
        leakyHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText(getString(R.string.text_handler_1));
            }
        }, 5000);
    }
```

如何解决？
- 静态声明Handler
- 使用弱引用

代码如下：
```
public class HandlersReferenceLeakActivity extends AppCompatActivity {

    private TextView textView;

    /*
     * Fix number I
     * */
    private final LeakyHandler leakyHandler = new LeakyHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        leakyHandler.postDelayed(leakyRunnable, 5000);
    }

    /*
     * Fix number II - define as static
     * */
    private static class LeakyHandler extends Handler {
      
    /*
     * Fix number III - Use WeakReferences
     * */      
        private WeakReference<HandlersReferenceLeakActivity> weakReference;
        public LeakyHandler(HandlersReferenceLeakActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HandlersReferenceLeakActivity activity = weakReference.get();
            if (activity != null) {
                activity.textView.setText(activity.getString(R.string.text_handler_2));
            }
        }
    }

    private static final Runnable leakyRunnable = new Runnable() {
        @Override
        public void run() { /* ... */ }
    }
```

### 8. Thread引用

重复同样的错误使用

```Java
public class ThreadReferenceLeakActivity extends AppCompatActivity {

    /*  
     * Mistake Number 1: Do not use static variables
     * */    
    private static LeakyThread thread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        createThread();
        redirectToNewScreen();
    }


    private void createThread() {
        thread = new LeakyThread();
        thread.start();
    }

    private void redirectToNewScreen() {
        startActivity(new Intent(this, SecondActivity.class));
    }


    /*
     * Mistake Number 2: Non-static anonymous classes hold an 
     * implicit reference to their enclosing class.
     * */
    private class LeakyThread extends Thread {
        @Override
        public void run() {
            while (true) {
            }
        }
    }
```
如何解决？
- 使用静态匿名类
- Activity销毁时关闭Thread

```Java
public class ThreadReferenceLeakActivity extends AppCompatActivity {

    /*
     * FIX I: make variable non static
     * */
    private LeakyThread leakyThread = new LeakyThread();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        createThread();
        redirectToNewScreen();
    }


    private void createThread() {
        leakyThread.start();
    }

    private void redirectToNewScreen() {
        startActivity(new Intent(this, SecondActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // FIX II: kill the thread
        leakyThread.interrupt();
    }


    /*
     * Fix III: Make thread static
     * */
    private static class LeakyThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
            }
        }
    }
}
```

### 9. TimerTask引用
Thead的例子通用适用于TimerTask，以下例子会导致内存泄漏
```Java
public class TimerTaskReferenceLeakActivity extends Activity {

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        startTimer();
    }

    /*  
     * Mistake 1: Cancel Timer is never called 
     * even though activity might be completed
     * */
    public void cancelTimer() {
        if(countDownTimer != null) countDownTimer.cancel();
    }

    
    private void startTimer() {
        countDownTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final int secondsRemaining = (int) (millisUntilFinished / 1000);
                //update UI
            }

            @Override
            public void onFinish() {
                //handle onFinish
            }
        };
        countDownTimer.start();
    }
}
```
如何解决？
- 销毁Activity的时候取消Timer
```Java
public class TimerTaskReferenceLeakActivity extends Activity {

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        startTimer();
    }


    public void cancelTimer() {
        if(countDownTimer != null) countDownTimer.cancel();
    }


    private void startTimer() {
        countDownTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final int secondsRemaining = (int) (millisUntilFinished / 1000);
                //update UI
            }

            @Override
            public void onFinish() {
                //handle onFinish
            }
        };
        countDownTimer.start();
    }
  
  
    /*
     * Fix 1: Cancel Timer when 
     * activity might be completed
     * */  
   @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }
}
```

## 总结
避免内存泄漏可以以下几个方面考虑：
- 使用Application Context
- 不要使用静态变量声明View或者Activity
- 静态内部类代替非静态内部类
- 在Activity销毁时要确保取消注册广播、异步任务、计时器或者线程等
- 使用Activity或者View的弱引用

Happy Coding!
