package demo.topsnackbar;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import demo.library.TopSnackbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ConstraintLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = (ConstraintLayout) findViewById(R.id.cl_content);
        findViewById(R.id.tv_hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "hello world is onClick!");
                TopSnackbar.make(content, "说的发送大公司是多个电饭锅电饭锅地方郭德纲水电费费郭德纲电饭锅地方高档房个的风格的", TopSnackbar.LENGTH_SHORT).show();
            }
        });
    }
}
