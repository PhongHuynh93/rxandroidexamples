package kurtis.rx.androidexamples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * https://medium.com/@kurtisnusbaum/rxandroid-basics-part-1-c0d5edcf6850#.to5vs1sbl
 */
public class Example2Activity extends AppCompatActivity {

    private Subscription mTvShowSubscription;
    private RecyclerView mTvShowListView;
    private ProgressBar mProgressBar;
    private SimpleStringAdapter mSimpleStringAdapter;
    private RestClient mRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRestClient = new RestClient(this);
        configureLayout();
        createObservable();
    }

    /**
     * TODO 1 ko thể sd Observable.just(mRestClient.getFavoriteTvShows()) trong {@link Example1Activity}
     * Observable.fromCallable() method
     * 1 The code for creating the emitted value is not run until someone subscribes to the Observer.
     * 2 The creation code can be run on a different thread.
     * <p/>
     * Observable.fromCallable() allows us to delay the creation of a value to be emitted by an Observable.
     * This is handy when the value you want to emit from your Observable needs to be created off of the UI thread.
     */

    // tức là hàm getFavoriteTvShows() chưa được chạy
    private void createObservable() {
        Observable<List<String>> tvShowObservable = Observable.fromCallable(new Callable<List<String>>() {
            @Override
            public List<String> call() {
                return mRestClient.getFavoriteTvShows();
            }
        });

        /**
         * // TODO: 7/30/16 2 khi có subcribe mới chạy hàm getFavoriteTvShows() được
         * subscribeOn():
         * alters the Observable we created above.
         * All of the code that this Observable would normally run, including the code the gets run upon subscription,
         * will now run on a different thread.
         * subscribeOn() allows us to run our value creation code on a specific thread, namely a thread that is not the UI thread.
         *
         * -> getFavoriteTvShows(): will run on a different thread.
         *  But which thread will it run on? In this case we specify that the code is run on the “IO Scheduler” (Schedulers.io()).
         *  For now we can just think of a Scheduler as a separate thread for doing work.
         *
         *  Problem: Since our Observable is set to run on the IO Scheduler, this means it’s going to interact with our Observer on the IO Scheduler as well.
         *
         * We can tell RxJava that we want to observe this Observable on the UI thread
         * we want our onNext() callback to be called on the UI thread.
         * We do this by specifying a different scheduler in the observeOn() method, namely the scheduler returned by AndroidSchedules.mainThread() (a scheduler for the UI thread).
         *
         * subscribe():This is critical since the code in our Callable won’t run until we actually subscribe something to it.
         */


        /**
         *
         * Subscription:  When an Observer subscribes to an Observable a Subscription is created.
         * A Subscription represents a connection between an Observer and an Observable.
         *
         * Sometimes we need to sever this connection. {@link Example2Activity#onDestroy()}
         * tại sao lại phải xóa đường connection này:
         * If you’ve ever done threading work on Android before you know there’s typically one huge problem:
         * what happens if your thread finishes (or never finishes) it’s execution after an Activity has been torn down.
         * This can cause a whole host of problems including memory leaks and NullPointerExceptions.
         *
         * Subscriptions: “Hey, Observable, this Observer doesn’t want to receive your emissions anymore. Please disconnect from the Observer.” We do this by calling unsubscribe().
         * After calling unsubscribe(), the Observer we created above will no longer receive emissions
         */

        /**
         * observeOn() allows us to then observe the emitted values of an Observable on an appropriate thread,
         * namely the main UI thread.
         */
        mTvShowSubscription = tvShowObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    //                            onNext() calls methods on some of our views. nhưng nó sẽ chạy trên worker thread nếu ko có observeOn
                    @Override
                    public void onNext(List<String> tvShows) {
                        displayTvShows(tvShows);
                    }
                });
    }

    /**
     * todo 3 - We should always unsubscribe our Observers in order to prevent nasty things from happening when we’re using Observables to load things asynchronously.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTvShowSubscription != null && !mTvShowSubscription.isUnsubscribed()) {
            mTvShowSubscription.unsubscribe();
        }
    }

    private void displayTvShows(List<String> tvShows) {
        mSimpleStringAdapter.setStrings(tvShows);
        mProgressBar.setVisibility(View.GONE);
        mTvShowListView.setVisibility(View.VISIBLE);
    }

    private void configureLayout() {
        setContentView(R.layout.activity_example_2);
        mProgressBar = (ProgressBar) findViewById(R.id.loader);
        mTvShowListView = (RecyclerView) findViewById(R.id.tv_show_list);
        mTvShowListView.setLayoutManager(new LinearLayoutManager(this));
        mSimpleStringAdapter = new SimpleStringAdapter(this);
        mTvShowListView.setAdapter(mSimpleStringAdapter);
    }
}
