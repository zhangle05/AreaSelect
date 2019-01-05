package org.aiit.widgets;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import org.aiit.views.areaselect.R;

import org.aiit.shapemodel.AbstractShape;

public class AreaSelectView extends View {
    private final boolean DEBUG = false;
    private AbstractShape rootShape;
    private RectF headRect;
    private RectF infoRect;
    Paint paint = new Paint();
    Matrix matrix = new Matrix();
    float xScale1 = 1;
    float yScale1 = 1;
    int lastX;
    int lastY;
    /**
     * 整个画布的宽度
     */
    int canvasWidth;

    /**
     * 整个画布的高度
     */
    int canvasHeight;
    /**
     * 标识是否正在缩放
     */
    boolean isScaling;
    float scaleX, scaleY;

    /**
     * 是否是第一次缩放
     */
    boolean firstScale = true;
    boolean isOnClick;
    private int downX, downY;
    private boolean pointer;
    /**
     * 顶部图例高度
     */
    float headHeight;
    /**
     * 头部下面横线的高度
     */
    int borderHeight = 1;
    /**
     * 信息区高度
     */
    float infoHeight;

    public AreaSelectView(Context context) {
        super(context);
    }

    public AreaSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AreaSelectView);
        typedArray.recycle();
    }

    public AreaSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init() {
        paint.setColor(Color.RED);
        infoHeight = dip2Px(30);
        headHeight = dip2Px(30);
        matrix.postTranslate(0, headHeight + infoHeight + borderHeight);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (rootShape != null) {
            rootShape.setBoundLimit(this.getWidth(), this.getHeight());
        }
        canvasWidth = this.getWidth();
        canvasHeight = this.getHeight();
    }

    public void setRootShape(AbstractShape rootShape) {
        init();
        this.rootShape = rootShape;
        if (rootShape != null) {
            rootShape.setInvalidateCallback(new AbstractShape.InvalidateCallback() {
                @Override
                public void invalidate(Rect area) {
                    AreaSelectView.this.invalidate(area);
                }

                @Override
                public void invalidate() {
                    AreaSelectView.this.invalidate();
                }
            });
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long startTime = System.currentTimeMillis();

        zoom = getMatrixScaleX();
        float translateX = getTranslateX();
        float translateY = getTranslateY();
        float scaleX = zoom;
        float scaleY = zoom;
        tempMatrix.setTranslate(translateX, translateY);
        tempMatrix.postScale(xScale1, yScale1, translateX, translateY);
        tempMatrix.postScale(scaleX, scaleY, translateX, translateY);

        if (rootShape != null) {
            rootShape.draw(canvas, tempMatrix);
            if (headRect == null) {
                headRect = new RectF(0, 0, canvas.getWidth(), headHeight);
            }
            if (infoRect == null) {
                infoRect = new RectF(0, headHeight + borderHeight, canvas.getWidth(), headHeight + borderHeight + infoHeight);
            }
            rootShape.drawLegend(canvas, headRect);
            rootShape.drawInfo(canvas, infoRect);
        }
        if (DEBUG) {
            long drawTime = System.currentTimeMillis() - startTime;
            Log.d("drawTime", "totalDrawTime:" + drawTime);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        int x = (int) event.getX();
        super.onTouchEvent(event);

        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            pointer = true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointer = false;
                downX = x;
                downY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isScaling && !isOnClick) {
                    int downDX = Math.abs(x - downX);
                    int downDY = Math.abs(y - downY);
                    if ((downDX > 10 || downDY > 10) && !pointer) {
                        int dx = x - lastX;
                        int dy = y - lastY;
                        matrix.postTranslate(dx, dy);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                autoScale();
                int downDX = Math.abs(x - downX);
                int downDY = Math.abs(y - downY);
                if ((downDX > 10 || downDY > 10) && !pointer) {
                    autoScroll();
                }
                break;
        }
        isOnClick = false;
        lastY = y;
        lastX = x;
        return true;
    }

    Matrix tempMatrix = new Matrix();

    /**
     * 自动回弹
     * 整个大小不超过控件大小的时候:
     * 往左边滑动,自动回弹到右边
     * 往右边滑动,自动回弹到左边
     * 往上,下滑动,自动回弹到顶部
     * <p>
     * 整个大小超过控件大小的时候:
     * 往左侧滑动,回弹到最右边,往右侧滑回弹到最左边
     * 往上滑动,回弹到底部,往下滑动回弹到顶部
     */
    private void autoScroll() {
        float scaleX = getMatrixScaleX();
        float scaleY = getMatrixScaleY();
        float translateX = getTranslateX();
        float translateY = getTranslateY();
        float currentSeatBitmapWidth = canvasWidth * scaleX;
        float currentSeatBitmapHeight = canvasHeight * scaleY;
        float moveYLength = 0;
        float moveXLength = 0;

        //处理左右滑动的情况
        if (currentSeatBitmapWidth < getWidth()) {
            if (translateX < 0 || scaleX < 0) {
                //计算要移动的距离
                if (translateX < 0) {
                    moveXLength = (-translateX);
                } else {
                    moveXLength = 0 - translateX;
                }
            } else {
                if (translateX < 0) {
                    moveXLength = (-translateX);
                } else {
                    moveXLength = 0 - translateX;
                }
            }
        } else {
            if (translateX < 0 && translateX + currentSeatBitmapWidth > getWidth()) {

            } else {
                //往左侧滑动
                if (translateX + currentSeatBitmapWidth < getWidth()) {
                    moveXLength = getWidth() - (translateX + currentSeatBitmapWidth);
                } else {
                    //右侧滑动
                    moveXLength = -translateX;
                }
            }

        }

        float startYPosition = infoHeight * scaleY + headHeight + borderHeight;

        //处理上下滑动
        if (currentSeatBitmapHeight+headHeight < getHeight()) {

            if (translateY < startYPosition) {
                moveYLength = startYPosition - translateY;
            } else {
                moveYLength = -(translateY - (startYPosition));
            }

        } else {

            if (translateY < 0 && translateY + currentSeatBitmapHeight > getHeight()) {

            } else {
                //往上滑动
                if (translateY + currentSeatBitmapHeight < getHeight()) {
                    moveYLength = getHeight() - (translateY + currentSeatBitmapHeight);
                } else {
                    moveYLength = -(translateY - (startYPosition));
                }
            }
        }

        Point start = new Point();
        start.x = (int) translateX;
        start.y = (int) translateY;

        Point end = new Point();
        end.x = (int) (start.x + moveXLength);
        end.y = (int) (start.y + moveYLength);

        moveAnimate(start, end);

    }

    private void autoScale() {

        if (getMatrixScaleX() > 2.2) {
            zoomAnimate(getMatrixScaleX(), 2.0f);
        } else if (getMatrixScaleX() < 0.98) {
            zoomAnimate(getMatrixScaleX(), 1.0f);
        }
    }

    float[] m = new float[9];

    private float getTranslateX() {
        matrix.getValues(m);
        return m[2];
    }

    private float getTranslateY() {
        matrix.getValues(m);
        return m[5];
    }

    private float getMatrixScaleY() {
        matrix.getValues(m);
        return m[4];
    }

    private float getMatrixScaleX() {
        matrix.getValues(m);
        return m[Matrix.MSCALE_X];
    }

    private float dip2Px(float value) {
        return getResources().getDisplayMetrics().density * value;
    }

    private void moveAnimate(Point start, Point end) {
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new AreaSelectView.MoveEvaluator(), start, end);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        AreaSelectView.MoveAnimation moveAnimation = new AreaSelectView.MoveAnimation();
        valueAnimator.addUpdateListener(moveAnimation);
        valueAnimator.setDuration(400);
        valueAnimator.start();
    }

    private void zoomAnimate(float cur, float tar) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(cur, tar);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        AreaSelectView.ZoomAnimation zoomAnim = new AreaSelectView.ZoomAnimation();
        valueAnimator.addUpdateListener(zoomAnim);
        valueAnimator.addListener(zoomAnim);
        valueAnimator.setDuration(400);
        valueAnimator.start();
    }

    private float zoom;

    private void zoom(float zoom) {
        float z = zoom / getMatrixScaleX();
        matrix.postScale(z, z, scaleX, scaleY);
        invalidate();
    }

    private void move(Point p) {
        float x = p.x - getTranslateX();
        float y = p.y - getTranslateY();
        matrix.postTranslate(x, y);
        invalidate();
    }

    class MoveAnimation implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Point p = (Point) animation.getAnimatedValue();

            move(p);
        }
    }

    class MoveEvaluator implements TypeEvaluator {

        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Point startPoint = (Point) startValue;
            Point endPoint = (Point) endValue;
            int x = (int) (startPoint.x + fraction * (endPoint.x - startPoint.x));
            int y = (int) (startPoint.y + fraction * (endPoint.y - startPoint.y));
            return new Point(x, y);
        }
    }

    class ZoomAnimation implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            zoom = (Float) animation.getAnimatedValue();
            zoom(zoom);

            if (DEBUG) {
                Log.d("zoomTest", "zoom:" + zoom);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

    }

    ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            isScaling = true;
            float scaleFactor = detector.getScaleFactor();
            if (getMatrixScaleY() * scaleFactor > 3) {
                scaleFactor = 3 / getMatrixScaleY();
            }
            if (firstScale) {
                scaleX = detector.getCurrentSpanX();
                scaleY = detector.getCurrentSpanY();
                firstScale = false;
            }

            if (getMatrixScaleY() * scaleFactor < 0.5) {
                scaleFactor = 0.5f / getMatrixScaleY();
            }
            matrix.postScale(scaleFactor, scaleFactor, scaleX, scaleY);
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
            firstScale = true;
        }
    });

    GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            isOnClick = true;
            float x = e.getX();
            float y = e.getY();
            if (rootShape != null) {
                rootShape.onSingleTap(x, y);
                invalidate();
            }
            return super.onSingleTapConfirmed(e);
        }
    });

}
