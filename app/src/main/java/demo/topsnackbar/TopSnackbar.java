package demo.topsnackbar;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static demo.topsnackbar.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;

/**
 * @author woniu
 * @title TopSnackbar
 * @description
 * @modifier
 * @date
 * @since 17/5/5 下午7:39
 */
public class TopSnackbar {

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    interface OnLayoutChangeListener {
        void onLayoutChange(View view, int left, int top, int right, int bottom);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);

        void onViewDetachedFromWindow(View v);
    }

    public abstract static class Callback {

        public static final int DISMISS_EVENT_SWIPE = 0;

        public static final int DISMISS_EVENT_ACTION = 1;

        public static final int DISMISS_EVENT_TIMEOUT = 2;

        public static final int DISMISS_EVENT_MANUAL = 3;

        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        /**
         * @hide
         */
        @RestrictTo(LIBRARY_GROUP)
        @IntDef({DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent {
        }

        public void onDismissed(TopSnackbar topSnackbar, @DismissEvent int event) {
            // empty
        }


        public void onShown(TopSnackbar topSnackbar) {
            // empty
        }
    }

    public static final int LENGTH_INDEFINITE = -2;

    public static final int LENGTH_SHORT = -1;

    public static final int LENGTH_LONG = 0;

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
    @IntRange(from = 1)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    static final Handler sHandler;
    static final int MSG_SHOW = 0;
    static final int MSG_DISMISS = 1;

    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((TopSnackbar) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((TopSnackbar) message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * Interface that defines the behavior of the main content of a transient bottom bar.
     */
    public interface ContentViewCallback {
        /**
         * Animates the content of the transient bottom bar in.
         *
         * @param delay    Animation delay.
         * @param duration Animation duration.
         */
        void animateContentIn(int delay, int duration);

        /**
         * Animates the content of the transient bottom bar out.
         *
         * @param delay    Animation delay.
         * @param duration Animation duration.
         */
        void animateContentOut(int delay, int duration);
    }

    static final int ANIMATION_DURATION = 250;
    static final int ANIMATION_FADE_DURATION = 180;

    private int mDuration;

    private ViewGroup mParent;
    private Context mContext;
    private TopSnackbarLayout mView;
    private Callback mCallback;
    private List<Callback> mCallbacks;
    private ContentViewCallback mContentViewCallback;

    private final AccessibilityManager mAccessibilityManager;

    public TopSnackbar(ViewGroup parent) {
        mParent = parent;
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mView = (TopSnackbarLayout) inflater.inflate(R.layout.layout_top_snackbar, mParent, false);
        mContentViewCallback = mView;

        mAccessibilityManager = (AccessibilityManager)
                mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    @NonNull
    public static TopSnackbar make(@NonNull View view, @NonNull CharSequence text,
                                   @Duration int duration) {
        final ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException("No suitable parent found from the given view. "
                    + "Please provide a valid view.");
        }

        final TopSnackbar snackbar = new TopSnackbar(parent);
        snackbar.setDuration(duration);
        return snackbar;
    }

    @NonNull
    public static TopSnackbar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {

                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {


                    return (ViewGroup) view;
                } else {

                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {

                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);


        return fallback;
    }


    @NonNull
    public TopSnackbar setDuration(@Duration int duration) {
        mDuration = duration;
        return this;
    }


    @Duration
    public int getDuration() {
        return mDuration;
    }

    public boolean isShownOrQueued() {
        return SnackbarManager.getInstance().isCurrentOrNext(mManagerCallback);
    }

    final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, TopSnackbar.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, TopSnackbar.this));
        }
    };

    final void showView() {
        if (mView.getParent() == null) {
            final ViewGroup.LayoutParams lp = mView.getLayoutParams();

            if (lp instanceof CoordinatorLayout.LayoutParams) {
                // If our LayoutParams are from a CoordinatorLayout, we'll setup our Behavior
                final CoordinatorLayout.LayoutParams clp = (CoordinatorLayout.LayoutParams) lp;

                final Behavior behavior = new Behavior();
                behavior.setStartAlphaSwipeDistance(0.1f);
                behavior.setEndAlphaSwipeDistance(0.6f);
                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        view.setVisibility(View.GONE);
                        dispatchDismiss(Callback.DISMISS_EVENT_SWIPE);
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                        switch (state) {
                            case SwipeDismissBehavior.STATE_DRAGGING:
                            case SwipeDismissBehavior.STATE_SETTLING:
                                // If the view is being dragged or settling, pause the timeout
                                SnackbarManager.getInstance().pauseTimeout(mManagerCallback);
                                break;
                            case SwipeDismissBehavior.STATE_IDLE:
                                // If the view has been released and is idle, restore the timeout
                                SnackbarManager.getInstance()
                                        .restoreTimeoutIfPaused(mManagerCallback);
                                break;
                        }
                    }
                });
                clp.setBehavior(behavior);
                // Also set the inset edge so that views can dodge the bar correctly
                clp.insetEdge = Gravity.BOTTOM;
            }

            mParent.addView(mView);
        }

        mView.setOnAttachStateChangeListener(new OnAttachStateChangeListener() {

            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (isShownOrQueued()) {
                    // If we haven't already been dismissed then this event is coming from a
                    // non-user initiated action. Hence we need to make sure that we callback
                    // and keep our state up to date. We need to post the call since
                    // removeView() will call through to onDetachedFromWindow and thus overflow.
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onViewHidden(Callback.DISMISS_EVENT_MANUAL);
                        }
                    });
                }
            }
        });

        if (ViewCompat.isLaidOut(mView)) {
            if (shouldAnimate()) {
                // If animations are enabled, animate it in
                animateViewIn();
            } else {
                // Else if anims are disabled just call back now
                onViewShown();
            }
        } else {
            // Otherwise, add one of our layout change listeners and show it in when laid out
            mView.setOnLayoutChangeListener(new TopSnackbar.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    mView.setOnLayoutChangeListener(null);

                    if (shouldAnimate()) {
                        // If animations are enabled, animate it in
                        animateViewIn();
                    } else {
                        // Else if anims are disabled just call back now
                        onViewShown();
                    }
                }
            });
        }
    }

    void animateViewIn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewCompat.setTranslationY(mView, mView.getHeight());
            ViewCompat.animate(mView)
                    .translationY(0f)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            mContentViewCallback.animateContentIn(
                                    ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                                    ANIMATION_FADE_DURATION);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            onViewShown();
                        }
                    }).start();
        }
    }

    private void animateViewOut(final int event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewCompat.animate(mView)
                    .translationY(mView.getHeight())
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            mContentViewCallback.animateContentOut(0, ANIMATION_FADE_DURATION);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            onViewHidden(event);
                        }
                    }).start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(mView.getContext(),
                    android.support.design.R.anim.design_snackbar_out);
            anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    onViewHidden(event);
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mView.startAnimation(anim);
        }
    }

    final void hideView(@Callback.DismissEvent final int event) {
        if (shouldAnimate() && mView.getVisibility() == View.VISIBLE) {
            animateViewOut(event);
        } else {
            // If anims are disabled or the view isn't visible, just call back now
            onViewHidden(event);
        }
    }

    void onViewShown() {
        SnackbarManager.getInstance().onShown(mManagerCallback);
        if (mCallbacks != null) {
            // Notify the callbacks. Do that from the end of the list so that if a callback
            // removes itself as the result of being called, it won't mess up with our iteration
            int callbackCount = mCallbacks.size();
            for (int i = callbackCount - 1; i >= 0; i--) {
                mCallbacks.get(i).onShown(this);
            }
        }
    }

    void onViewHidden(int event) {
        // First tell the SnackbarManager that it has been dismissed
        SnackbarManager.getInstance().onDismissed(mManagerCallback);
        if (mCallbacks != null) {
            // Notify the callbacks. Do that from the end of the list so that if a callback
            // removes itself as the result of being called, it won't mess up with our iteration
            int callbackCount = mCallbacks.size();
            for (int i = callbackCount - 1; i >= 0; i--) {
                mCallbacks.get(i).onDismissed(this, event);
            }
        }
        if (Build.VERSION.SDK_INT < 11) {
            // We need to hide the Snackbar on pre-v11 since it uses an old style Animation.
            // ViewGroup has special handling in removeView() when getAnimation() != null in
            // that it waits. This then means that the calculated insets are wrong and the
            // any dodging views do not return. We workaround it by setting the view to gone while
            // ViewGroup actually gets around to removing it.
            mView.setVisibility(View.GONE);
        }
        // Lastly, hide and remove the view from the parent (if attached)
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
    }

    boolean shouldAnimate() {
        return !mAccessibilityManager.isEnabled();
    }

    /**
     * Show
     */
    public void show() {
        SnackbarManager.getInstance().show(mDuration, mManagerCallback);
    }

    /**
     * Dismiss
     */
    public void dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
    }

    void dispatchDismiss(@Callback.DismissEvent int event) {
        SnackbarManager.getInstance().dismiss(mManagerCallback, event);
    }

    static class TopSnackbarLayout extends FrameLayout implements ContentViewCallback {

        private TopSnackbar.OnLayoutChangeListener mOnLayoutChangeListener;
        private TopSnackbar.OnAttachStateChangeListener mOnAttachStateChangeListener;

        public TopSnackbarLayout(@NonNull Context context) {
            super(context);
        }

        public TopSnackbarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public TopSnackbarLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }

            ViewCompat.requestApplyInsets(this);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        void setOnLayoutChangeListener(
                TopSnackbar.OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(TopSnackbar.OnAttachStateChangeListener listener) {
            mOnAttachStateChangeListener = listener;
        }

        @Override
        public void animateContentIn(int delay, int duration) {
//            ViewCompat.setAlpha(mMessageView, 0f);
//            ViewCompat.animate(mMessageView).alpha(1f).setDuration(duration)
//                    .setStartDelay(delay).start();
//
//            if (mActionView.getVisibility() == VISIBLE) {
//                ViewCompat.setAlpha(mActionView, 0f);
//                ViewCompat.animate(mActionView).alpha(1f).setDuration(duration)
//                        .setStartDelay(delay).start();
//            }
        }

        @Override
        public void animateContentOut(int delay, int duration) {
//            ViewCompat.setAlpha(mMessageView, 1f);
//            ViewCompat.animate(mMessageView).alpha(0f).setDuration(duration)
//                    .setStartDelay(delay).start();
//
//            if (mActionView.getVisibility() == VISIBLE) {
//                ViewCompat.setAlpha(mActionView, 1f);
//                ViewCompat.animate(mActionView).alpha(0f).setDuration(duration)
//                        .setStartDelay(delay).start();
//            }
        }
    }

    final class Behavior extends SwipeDismissBehavior<TopSnackbarLayout> {
        @Override
        public boolean canSwipeDismissView(View child) {
            return child instanceof TopSnackbarLayout;
        }

        @Override
        public boolean onInterceptTouchEvent(CoordinatorLayout parent, TopSnackbarLayout child,
                                             MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // We want to make sure that we disable any Snackbar timeouts if the user is
                    // currently touching the Snackbar. We restore the timeout when complete
                    if (parent.isPointInChildBounds(child, (int) event.getX(),
                            (int) event.getY())) {
                        SnackbarManager.getInstance().pauseTimeout(mManagerCallback);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    SnackbarManager.getInstance().restoreTimeoutIfPaused(mManagerCallback);
                    break;
            }
            return super.onInterceptTouchEvent(parent, child, event);
        }
    }

}
