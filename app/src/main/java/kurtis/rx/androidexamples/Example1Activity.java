package kurtis.rx.androidexamples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;

/**
 * https://medium.com/@kurtisnusbaum/rxandroid-basics-part-1-c0d5edcf6850#.to5vs1sbl
 */
public class Example1Activity extends AppCompatActivity {

    RecyclerView mColorListView;
    SimpleStringAdapter mSimpleStringAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureLayout();
        createObservable();
    }

    private void createObservable() {
        // TODO 1 We’ll then use the emitted value to populate the list.
        /**
         * This method creates an Observable such that when an Observer subscribes,
         * the onNext() of the Observer is immediately called with the argument provided to Observable.just().
         * The onComplete() will then be called since the Observable has no other values to emit.
         */


        /**
         *         getColorList() = non blocking
         * Nhưng Observable.just(mRestClient.getFavoriteTvShows()) = blocking network call.
         * -> will be evaluated immediately and block the UI thread.
         */

        // chưa phát ra
        Observable<List<String>> listObservable = Observable.just(getColorList());

        // sẽ phát liền khi có 1 subcribe gắn và nó
        listObservable.subscribe(new Observer<List<String>>() {
            // TODO: 7/30/16 3 there is no more data (we only gave our Observable a single value to emit in Observable.just()), the onComplete() callback is called.
            //  don’t care about what happens when the Observable has completed so we leave the onComplete() method empty.
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            // TODO 2 The onNext() method is called and the emitted list of colors is set as the data for the adapter.
            /**
             * ta nhận thấy rằng "just" tạo Observable chứa 1 List<String>, là 1 list chứa cac String, nhưng nó ko phát tưng String mà nó phát hết 1 list of String.
             * @param colors 1 list gồm các màu sác được phát ra.
             */
            @Override
            public void onNext(List<String> colors) {
                mSimpleStringAdapter.setStrings(colors);
            }
        });

    }

    private void configureLayout() {
        setContentView(R.layout.activity_example_1);
        mColorListView = (RecyclerView) findViewById(R.id.color_list);
        mColorListView.setLayoutManager(new LinearLayoutManager(this));
        mSimpleStringAdapter = new SimpleStringAdapter(this);
        mColorListView.setAdapter(mSimpleStringAdapter);
    }

    private static List<String> getColorList() {
        ArrayList<String> colors = new ArrayList<>();
        colors.add("blue");
        colors.add("green");
        colors.add("red");
        colors.add("chartreuse");
        colors.add("Van Dyke Brown");
        return colors;
    }
}
