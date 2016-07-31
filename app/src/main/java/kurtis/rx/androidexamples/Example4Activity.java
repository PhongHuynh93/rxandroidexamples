package kurtis.rx.androidexamples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rx.Observer;
import rx.subjects.PublishSubject;

public class Example4Activity extends AppCompatActivity {

    private TextView mCounterDisplay;
    private Button mIncrementButton;
    private PublishSubject<Integer> mCounterEmitter;

    private int mCounter = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureLayout();
        createCounterEmitter();
    }

    /**
     * todo 1 Subjects: are special objects that are both an Observable and an Observer.
     *
     * I like to think of Subjects as a pipe.
     * You can put things into one end of the Subject and it will come out the other.
     *
     * PublishSubject: There are several types of Subjects, but we’re going to use the simplest one: a PublishSubject.
     * With a PublishSubject, as soon as you put something in one end of the pipe it immediately comes out the other.
     *
     *  We said that Subjects were Observables which means we can Observe them like we would any other Observable.
     *
     *
     *
     */
    private void createCounterEmitter() {
        mCounterEmitter = PublishSubject.create();
        // đây là observable lý do là ta oo thể gắn subscribe vào được
        mCounterEmitter.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                  mCounterDisplay.setText(String.valueOf(integer));
            }
        });
    }

    private void configureLayout() {
        setContentView(R.layout.activity_example_4);
        configureCounterDisplay();
        configureIncrementButton();
    }

    private void configureCounterDisplay() {
        mCounterDisplay = (TextView) findViewById(R.id.counter_display);
        mCounterDisplay.setText(String.valueOf(mCounter));
    }

    private void configureIncrementButton() {
        mIncrementButton = (Button) findViewById(R.id.increment_button);
        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onIncrementButtonClick();
            }
        });
    }

    /**
     * It calls onNext() on the mCounterEmitter with the new value of mCounter.

     */
    private void onIncrementButtonClick() {
        mCounter++;
        // todo 2 đây là observer do nó gọi được hàm onNext()
        mCounterEmitter.onNext(mCounter);
    }
}
