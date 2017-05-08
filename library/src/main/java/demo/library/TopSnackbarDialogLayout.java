package demo.library;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author woniu
 * @title TopSnackbarConfirmLayout
 * @description
 * @modifier
 * @date
 * @since 17/5/8 下午3:54
 */
public class TopSnackbarDialogLayout extends TopSnackbarContentLayout {

    private static final String TAG = TopSnackbarDialogLayout.class.getSimpleName();

    private Context context;
    private TextView tvContent;
    private Button btnConfirm;
    private Button btnCancel;

    public TopSnackbarDialogLayout(Context context) {
        super(context);
        this.context = context;
    }

    public TopSnackbarDialogLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "width: " + getMeasuredWidth() + ", screen width: " + getWidth(context)
                + ", should width: " + (getWidth(context) - dip2px(context, 24)));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getWidth(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);

        return outMetrics.widthPixels;
    }

    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (null != btnConfirm) {
            btnConfirm.setOnClickListener(l);
        }
        if (null != btnCancel) {
            btnCancel.setOnClickListener(l);
        }
//        super.setOnClickListener(l);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tvContent = (TextView) findViewById(R.id.tv_content);
        btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setTag("hello confirm");
        btnCancel = (Button) findViewById(R.id.btn_cancel);
    }

    @Override
    public void animateContentIn(int delay, int duration) {
//        ViewCompat.setAlpha(mMessageView, 0f);
//        ViewCompat.animate(mMessageView).alpha(1f).setDuration(duration)
//                .setStartDelay(delay).start();
//
//        if (mActionView.getVisibility() == VISIBLE) {
//            ViewCompat.setAlpha(mActionView, 0f);
//            ViewCompat.animate(mActionView).alpha(1f).setDuration(duration)
//                    .setStartDelay(delay).start();
//        }
    }

    @Override
    public void animateContentOut(int delay, int duration) {
//        ViewCompat.setAlpha(mMessageView, 1f);
//        ViewCompat.animate(mMessageView).alpha(0f).setDuration(duration)
//                .setStartDelay(delay).start();
//
//        if (mActionView.getVisibility() == VISIBLE) {
//            ViewCompat.setAlpha(mActionView, 1f);
//            ViewCompat.animate(mActionView).alpha(0f).setDuration(duration)
//                    .setStartDelay(delay).start();
//        }
    }
}
