package kurtis.rx.androidexamples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Example3Activity extends AppCompatActivity {

    private Subscription mTvShowSubscription;
    private RecyclerView mTvShowListView;
    private ProgressBar mProgressBar;
    private TextView mErrorMessage;
    private SimpleStringAdapter mSimpleStringAdapter;
    private RestClient mRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRestClient = new RestClient(this);
        configureLayout();
        createSingle();
    }

    /**
     * Using Singles
     * let’s do it in a simpler fashion.
     *
     * Observables are great, but in many cases they’re kind of overkill.
     * For example, you’ll notice in the last two examples we only ever emitted a single value and never used the onComplete() callback.
     *
     * there’s a simpler version of an Observable called a Single.
     * Singles work almost exactly the same as Observables.
     * But instead of there being an onComplete(), onNext(), and onError(), there are only two callbacks: onSuccess() and onError().
     *
     *
     *
     */
    private void createSingle() {
        Single<List<String>> tvShowSingle = Single.fromCallable(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                /**
                 * Uncomment me (and comment out the line below) to see what happens when an error occurs.
                 *
                 * return RestClient.getFavoriteTvShowsWithException();
                 */
                return mRestClient.getFavoriteTvShows();
            }
        });

        mTvShowSubscription = tvShowSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<List<String>>() {
                    @Override
                    public void onSuccess(List<String> tvShows) {
                        displayTvShows(tvShows);
                    }

                    @Override
                    public void onError(Throwable error) {
                        displayErrorMessage();
                    }
                });
    }

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

    private void displayErrorMessage() {
        mProgressBar.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    private void configureLayout() {
        setContentView(R.layout.activity_example_3);
        mErrorMessage = (TextView) findViewById(R.id.error_message);
        mProgressBar = (ProgressBar) findViewById(R.id.loader);
        mTvShowListView = (RecyclerView) findViewById(R.id.tv_show_list);
        mTvShowListView.setLayoutManager(new LinearLayoutManager(this));
        mSimpleStringAdapter = new SimpleStringAdapter(this);
        mTvShowListView.setAdapter(mSimpleStringAdapter);
    }
}
