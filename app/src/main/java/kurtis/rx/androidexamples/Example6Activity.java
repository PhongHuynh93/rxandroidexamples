package kurtis.rx.androidexamples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * We want to setup a PublishSubject such that it receives values the user types into a search box,
 * fetches a list of suggestions based on that query, and then displays them.
 */
public class Example6Activity extends AppCompatActivity {

    private RestClient mRestClient;
    private EditText mSearchInput;
    private TextView mNoResultsIndicator;
    private RecyclerView mSearchResults;
    private SimpleStringAdapter mSearchResultsAdapter;

    private PublishSubject<String> mSearchResultsSubject;
    private Subscription mTextWatchSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRestClient = new RestClient(this);
        configureLayout();
        createObservables();
        listenToSearchInput();
    }

    private void createObservables() {
        /**
         * Well if you look at how we’ve setup the TextWatcher, you’ll notice a new value is going to come into our PublishSubject every, single time the user adds or removes a character from their search.
         *
         * This is neat, but we don’t want to send out a request to the server on every single keystroke. We’d like to wait a little bit for the user to stop typing (so that we’re sure we’ve got a good query) and then send our search request to the server.
         *
         * -> debounce() allows us to do
         *
         *  It tells mSearchResultsSubject to only emit the last value that came into it after nothing new has come into the mSearchResultsSubject for 400 milliseconds.
         * Essentially, this means our subject won’t emit the search string until the user hasn’t changed the string for 400 milliseconds, and at the end of the 400 milliseconds it will only emit the latest search string the user entered.
         *
         *                         We want to use what the debounce emits to query our server via our RestClient.
         *  Since querying our RestClient is an IO operation we need to observe the emissions of debounce on the IO Scheduler. So boom, observeOn(Schedulers.io()).
         *
         *
         *
         *
         */

        // TODO Note the ordering of all our observerOn()s here.
        /**
         * mSearchResultsSubject
         |
         |
         V
         debounce
         |||
         |||
         V
         map
         |
         |
         V
         observer

         The | represents emissions happening on the UI Thread and the ||| represents emissions happening on the IO Scheduler.

         */
        mSearchResultsSubject = PublishSubject.create();
        mTextWatchSubscription = mSearchResultsSubject
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                /**
                 * Because map can run any arbitrary function, we’ll use our RestClient to transform our search query into the list of actual results we want to display.
                 *
                 */
                .map(new Func1<String, List<String>>() {
                    @Override
                    public List<String> call(String s) {
                        return mRestClient.searchForCity(s);
                    }
                })
                /**
                 * Since our map was run on the IO Scheduler, and we want to use the results it emits to populate our views, we then need to switch back to the UI thread. So we add an observeOn(AndroidSchedulers.mainThread()).
                 */
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<String> cities) {
                        handleSearchResults(cities);
                    }
                });
    }

    private void handleSearchResults(List<String> cities) {
        if (cities.isEmpty()) {
            showNoSearchResults();
        } else {
            showSearchResults(cities);
        }
    }

    private void showNoSearchResults() {
        mNoResultsIndicator.setVisibility(View.VISIBLE);
        mSearchResults.setVisibility(View.GONE);
    }

    private void showSearchResults(List<String> cities) {
        mNoResultsIndicator.setVisibility(View.GONE);
        mSearchResults.setVisibility(View.VISIBLE);
        mSearchResultsAdapter.setStrings(cities);
    }

    private void listenToSearchInput() {
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchResultsSubject.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void configureLayout() {
        setContentView(R.layout.activity_example_6);
        mSearchInput = (EditText) findViewById(R.id.search_input);
        mNoResultsIndicator = (TextView) findViewById(R.id.no_results_indicator);
        mSearchResults = (RecyclerView) findViewById(R.id.search_results);
        mSearchResults.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsAdapter = new SimpleStringAdapter(this);
        mSearchResults.setAdapter(mSearchResultsAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTextWatchSubscription != null && !mTextWatchSubscription.isUnsubscribed()) {
            mTextWatchSubscription.unsubscribe();
        }
    }
}
