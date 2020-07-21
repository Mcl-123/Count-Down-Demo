package com.saicmotor.pvwav.asyncdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity {

    private final String HANDLER_KEY = "handler_key";
    private final int COUNT_DOWN_TIME = 5;
    private MainViewModel viewModel;

    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.handler_btn)
    Button handlerBtn;
    @BindView(R.id.asynctask_btn)
    Button asynctaskBtn;
    @BindView(R.id.rxjava_btn)
    Button rxjavaBtn;
    @BindView(R.id.coroutine_btn)
    Button coroutineBtn;

    MyHandler myHandler = new MyHandler(this);
    MyAsyncTask myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getTitleLiveData().observe(this, s -> {
            textView.setText(s);
        });
        viewModel.getBtnClickableLiveData().observe(this, clickable -> {
            coroutineBtn.setEnabled(clickable);
        });
    }

    @OnClick({R.id.handler_btn, R.id.asynctask_btn, R.id.rxjava_btn, R.id.coroutine_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.handler_btn:

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int time = COUNT_DOWN_TIME;
                        while (time > 0) {

                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putString(HANDLER_KEY, time + "s");
                            message.setData(bundle);
                            myHandler.sendMessage(message);

                            time -= 1;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        Message message = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString(HANDLER_KEY, "handler 完成");
                        message.setData(bundle);
                        myHandler.sendMessage(message);
                    }
                }).start();
                break;
            case R.id.asynctask_btn:
                myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute(COUNT_DOWN_TIME);
                break;
            case R.id.rxjava_btn:
                final int count = COUNT_DOWN_TIME;
                Observable.interval(0, 1, TimeUnit.SECONDS)
                        .take(count + 1)
                        .map(new Function<Long, Long>() {
                            @Override
                            public Long apply(Long aLong) throws Exception {
                                return count - aLong;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                rxjavaBtn.setEnabled(false);
                            }
                        })
                        .subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Long aLong) {
                                textView.setText(aLong + "s");
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                textView.setText("rxjava 完成");
                                rxjavaBtn.setEnabled(true);
                            }
                        });
                break;
            case R.id.coroutine_btn:
                viewModel.startCountDown();
                break;
            default:
                break;
        }
    }

    public static class MyHandler extends Handler {

        private WeakReference<MainActivity> mainActivityWeakReference;

        public MyHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mainActivityWeakReference.get();
            String s = msg.getData().getString(activity.HANDLER_KEY);
            activity.textView.setText(s);
        }
    }

    public class MyAsyncTask extends AsyncTask<Integer, String, String> {

        @Override
        protected String doInBackground(Integer... integers) {
            int time = integers[0];
            while (time > 0) {
                time -= 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(time + "s");
            }
            return "Async task 处理完成";
        }

        @Override
        protected void onPreExecute() {
            textView.setText(COUNT_DOWN_TIME + "s");
        }

        @Override
        protected void onPostExecute(String s) {
            textView.setText(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            textView.setText(values[0]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
    }
}
