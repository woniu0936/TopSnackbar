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
//                Log.d(TAG, "hello world is onClick!");
//                TopSnackbar.make(content, "hello world", TopSnackbar.LENGTH_SHORT).show();
//                TopSnackbar.confirm(content, TopSnackbar.LENGTH_INDEFINITE).show();
//                TopSnackbarDialogLayout topSnackbarDialogLayout = new TopSnackbarDialogLayout(MainActivity.this);
//                TopSnackbar.makeLayout(content, R.layout.layout_top_snackbar_dialog, TopSnackbar.LENGTH_INDEFINITE).show();
                TopSnackbar.builder()
                        .parent(content)
                        .content(R.layout.layout_top_snackbar_dialog)
                        .onClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                if (demo.library.R.id.btn_confirm == v.getId()) {
//                                    Log.d(TAG, "confirm is onClick!");
//                                }
                                Log.d(TAG, "view tag is: " + v.getTag());
                            }
                        })
                        .duration(TopSnackbar.LENGTH_INDEFINITE)
                        .build()
                        .show();

            }
        });
    }
}
