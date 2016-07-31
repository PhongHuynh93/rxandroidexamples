package kurtis.rx.androidexamples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

public class Example5Activity extends AppCompatActivity {

    private TextView mValueDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureLayout();

        /**
         * You can think of map as a function that takes in one value and outputs another value. Usually there is some relationship between value put in to the map and the value that is output
         *
         * convert it from an Integer to a String -> One way we can do this is using map().
         *
         * As we’ll see in the next example, maps can be used to execute arbitrary code and help us transform data in very useful ways.
         *
         */
        Single.just(4).map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                return String.valueOf(integer);
            }
        }).subscribe(new SingleSubscriber<String>() {
            @Override
            public void onSuccess(String value) {
                  mValueDisplay.setText(value);
            }

            @Override
            public void onError(Throwable error) {

            }
        });
    }

    private void configureLayout() {
        setContentView(R.layout.activity_example_5);
        mValueDisplay = (TextView) findViewById(R.id.value_display);
    }
}
