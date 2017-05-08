package demo.library;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @author woniu
 * @title TopSnackbar
 * @description
 * @modifier
 * @date
 * @since 17/5/8 下午1:05
 */
public class TopSnackbar extends BaseTransientTopBar<TopSnackbar> {

    /**
     * Show the Snackbar indefinitely. This means that the Snackbar will be displayed from the time
     * that is {@link #show() shown} until either it is dismissed, or another Snackbar is shown.
     *
     * @see #setDuration
     */
    public static final int LENGTH_INDEFINITE = BaseTransientTopBar.LENGTH_INDEFINITE;

    /**
     * Show the Snackbar for a short period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_SHORT = BaseTransientTopBar.LENGTH_SHORT;

    /**
     * Show the Snackbar for a long period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_LONG = BaseTransientTopBar.LENGTH_LONG;

    /**
     * Callback class for {@link TopSnackbar} instances.
     * <p>
     * Note: this class is here to provide backwards-compatible way for apps written before
     * the existence of the base {@link BaseTransientTopBar} class.
     *
     * @see BaseTransientTopBar#addCallback(BaseCallback)
     */
    public static class Callback extends BaseCallback<TopSnackbar> {
        /**
         * Indicates that the Snackbar was dismissed via a swipe.
         */
        public static final int DISMISS_EVENT_SWIPE = BaseCallback.DISMISS_EVENT_SWIPE;
        /**
         * Indicates that the Snackbar was dismissed via an action click.
         */
        public static final int DISMISS_EVENT_ACTION = BaseCallback.DISMISS_EVENT_ACTION;
        /**
         * Indicates that the Snackbar was dismissed via a timeout.
         */
        public static final int DISMISS_EVENT_TIMEOUT = BaseCallback.DISMISS_EVENT_TIMEOUT;
        /**
         * Indicates that the Snackbar was dismissed via a call to {@link #dismiss()}.
         */
        public static final int DISMISS_EVENT_MANUAL = BaseCallback.DISMISS_EVENT_MANUAL;
        /**
         * Indicates that the Snackbar was dismissed from a new Snackbar being shown.
         */
        public static final int DISMISS_EVENT_CONSECUTIVE = BaseCallback.DISMISS_EVENT_CONSECUTIVE;

        @Override
        public void onShown(TopSnackbar sb) {
            // Stub implementation to make API check happy.
        }

        @Override
        public void onDismissed(TopSnackbar transientBottomBar, @DismissEvent int event) {
            // Stub implementation to make API check happy.
        }
    }

    @Nullable
    private BaseCallback<TopSnackbar> mCallback;

    /**
     * Constructor for the transient bottom bar.
     *
     * @param parent              The parent for this transient bottom bar.
     * @param content             The content view for this transient bottom bar.
     * @param contentViewCallback The content view callback for this transient bottom bar.
     */
    protected TopSnackbar(@NonNull ViewGroup parent, @NonNull View content, @NonNull ContentViewCallback contentViewCallback) {
        super(parent, content, contentViewCallback);
    }

    protected TopSnackbar(TopSnackbar.Builder builder) {
        super(builder.parent, builder.content, builder.content);
        setDuration(builder.duration);
    }

    /**
     * Make a Snackbar to display a message
     * <p>
     * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given
     * to {@code view}. Snackbar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows TopSnackbar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static TopSnackbar make(@NonNull View view, @NonNull CharSequence text,
                                   @Duration int duration) {
        final ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException("No suitable parent found from the given view. "
                    + "Please provide a valid view.");
        }

        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final TopSnackbarNormalLayout content =
                (TopSnackbarNormalLayout) inflater.inflate(
                        R.layout.layout_top_snackbar_normal, parent, false);
        final TopSnackbar snackbar = new TopSnackbar(parent, content, content);
        snackbar.setText(text);
        snackbar.setDuration(duration);
        return snackbar;
    }

    public static TopSnackbar confirm(@NonNull View view, @Duration int duration) {
        final ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException("No suitable parent found from the given view. "
                    + "Please provide a valid view.");
        }
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final TopSnackbarDialogLayout content =
                (TopSnackbarDialogLayout) inflater.inflate(
                        R.layout.layout_top_snackbar_dialog, parent, false);
        final TopSnackbar snackbar = new TopSnackbar(parent, content, content);
        snackbar.setDuration(duration);
        return snackbar;
    }

    public static TopSnackbar makeLayout(@NonNull View view, @LayoutRes int layoutResId, @Duration int duration) {
        final ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException("No suitable parent found from the given view. "
                    + "Please provide a valid view.");
        }
        TopSnackbarContentLayout content = getLayout(parent, layoutResId);
        final TopSnackbar snackbar = new TopSnackbar(parent, content, content);
        snackbar.setDuration(duration);
        return snackbar;
    }

    public static <T extends TopSnackbarContentLayout> T getLayout(@NonNull ViewGroup parent, @LayoutRes int layoutResId) {
        if (layoutResId <= 0) {
            layoutResId = R.layout.layout_top_snackbar_normal;
        }
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final T content = (T) inflater.inflate(layoutResId, parent, false);
        return content;
    }

    /**
     * Make a Snackbar to display a message.
     * <p>
     * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given
     * to {@code view}. Snackbar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param resId    The resource id of the string resource to use. Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static TopSnackbar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
    }

    /**
     * Update the text in this {@link TopSnackbar}.
     *
     * @param message The new text for this {@link BaseTransientTopBar}.
     */
    @NonNull
    public TopSnackbar setText(@NonNull CharSequence message) {
        final TopSnackbarNormalLayout contentLayout = (TopSnackbarNormalLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getMessageView();
        tv.setText(message);
        return this;
    }

    /**
     * Update the text in this {@link TopSnackbar}.
     *
     * @param resId The new text for this {@link BaseTransientTopBar}.
     */
    @NonNull
    public TopSnackbar setText(@StringRes int resId) {
        return setText(getContext().getText(resId));
    }

    /**
     * Set the action to be displayed in this {@link BaseTransientTopBar}.
     *
     * @param resId    String resource to display for the action
     * @param listener callback to be invoked when the action is clicked
     */
    @NonNull
    public TopSnackbar setAction(@StringRes int resId, View.OnClickListener listener) {
        return setAction(getContext().getText(resId), listener);
    }

    /**
     * Set the action to be displayed in this {@link BaseTransientTopBar}.
     *
     * @param text     Text to display for the action
     * @param listener callback to be invoked when the action is clicked
     */
    @NonNull
    public TopSnackbar setAction(CharSequence text, final View.OnClickListener listener) {
        final TopSnackbarNormalLayout contentLayout = (TopSnackbarNormalLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();

        if (TextUtils.isEmpty(text) || listener == null) {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                    // Now dismiss the Snackbar
                    dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
                }
            });
        }
        return this;
    }

    public TopSnackbar action(final View.OnClickListener listener) {
        mView.getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
            }
        });
        return this;
    }

    /**
     * Sets the text color of the action specified in
     * {@link #setAction(CharSequence, View.OnClickListener)}.
     */
    @NonNull
    public TopSnackbar setActionTextColor(ColorStateList colors) {
        final TopSnackbarNormalLayout contentLayout = (TopSnackbarNormalLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        tv.setTextColor(colors);
        return this;
    }

    /**
     * Sets the text color of the action specified in
     * {@link #setAction(CharSequence, View.OnClickListener)}.
     */
    @NonNull
    public TopSnackbar setActionTextColor(@ColorInt int color) {
        final TopSnackbarNormalLayout contentLayout = (TopSnackbarNormalLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        tv.setTextColor(color);
        return this;
    }

    /**
     * Set a callback to be called when this the visibility of this {@link TopSnackbar}
     * changes. Note that this method is deprecated
     * and you should use {@link #addCallback(BaseCallback)} to add a callback and
     * {@link #removeCallback(BaseCallback)} to remove a registered callback.
     *
     * @param callback Callback to notify when transient bottom bar events occur.
     * @see Callback
     * @see #addCallback(BaseCallback)
     * @see #removeCallback(BaseCallback)
     * @deprecated Use {@link #addCallback(BaseCallback)}
     */
    @Deprecated
    @NonNull
    public TopSnackbar setCallback(Callback callback) {
        // The logic in this method emulates what we had before support for multiple
        // registered callbacks.
        if (mCallback != null) {
            removeCallback(mCallback);
        }
        if (callback != null) {
            addCallback(callback);
        }
        // Update the deprecated field so that we can remove the passed callback the next
        // time we're called
        mCallback = callback;
        return this;
    }

    /**
     * @hide Note: this class is here to provide backwards-compatible way for apps written before
     * the existence of the base {@link BaseTransientTopBar} class.
     */
    @RestrictTo(LIBRARY_GROUP)
    public static final class TopSnackbarLayout extends BaseTransientTopBar.SnackbarBaseLayout {
        public TopSnackbarLayout(Context context) {
            super(context);
        }

        public TopSnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            // Work around our backwards-compatible refactoring of Snackbar and inner content
            // being inflated against snackbar's parent (instead of against the snackbar itself).
            // Every child that is width=MATCH_PARENT is remeasured again and given the full width
            // minus the paddings.
            int childCount = getChildCount();
            int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.measure(View.MeasureSpec.makeMeasureSpec(availableWidth, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),
                                    View.MeasureSpec.EXACTLY));
                }
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private View view;
        @LayoutRes
        private int layoutResId;
        @Duration
        private int duration;
        private Bundle data;
        private View.OnClickListener listener;

        private ViewGroup parent;
        private TopSnackbarContentLayout content;

        public Builder() {

        }

        public Builder view(@NonNull View view) {
            this.view = view;
            return this;
        }

        public Builder layout(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
            return this;
        }

        public Builder data(Bundle data) {
            this.data = data;
            return this;
        }

        public Builder click(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder duration(@Duration int duration) {
            this.duration = duration;
            return this;
        }

        public TopSnackbar build() {
            parent = findSuitableParent(view);
            Log.d("Builder", "layoutResId: " + layoutResId);
            content = getLayout(parent, layoutResId);
            content.initView(data);
            content.setOnClickListener(listener);
            return new TopSnackbar(this);
        }

    }

}
