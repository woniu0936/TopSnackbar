package demo.library;

/**
 * @author woniu
 * @title TopSnackbarContentLayout
 * @description
 * @modifier
 * @date
 * @since 17/5/8 下午1:14
 */

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public abstract class TopSnackbarContentLayout extends LinearLayout implements
        BaseTransientTopBar.ContentViewCallback {

    public TopSnackbarContentLayout(Context context) {
        super(context);
    }

    public TopSnackbarContentLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

//    abstract void setOnClickListener();

}
