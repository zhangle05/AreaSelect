package org.aiit.widgets;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import org.aiit.views.areaselect.R;

import org.aiit.shapemodel.AbstractShape;

import java.util.ArrayList;
import java.util.Collections;

public class AreaSelectView extends View {
    private final boolean DEBUG = false;

    Paint paint = new Paint();
    Paint overviewPaint=new Paint();
    private AbstractShape rootShape;

    Matrix matrix = new Matrix();

    /**
     * 行数
     */
    int row;

    /**
     * 列数
     */
    int column;

    /**
     * 可选时座位的图片
     */
    Bitmap seatBitmap;

    /**
     * 选中时座位的图片
     */
    Bitmap checkedSeatBitmap;

    /**
     * 座位已经售出时的图片
     */
    Bitmap seatSoldBitmap;

    Bitmap overviewBitmap;

    int lastX;
    int lastY;

    /**
     * 整个座位图的宽度
     */
    int seatBitmapWidth;

    /**
     * 整个座位图的高度
     */
    int seatBitmapHeight;

    /**
     * 标识是否需要绘制座位图
     */
    boolean isNeedDrawSeatBitmap = true;

    /**
     * 概览图白色方块高度
     */
    float rectHeight;

    /**
     * 概览图白色方块的宽度
     */
    float rectWidth;

    /**
     * 概览图上方块的水平间距
     */
    float overviewSpacing;

    /**
     * 概览图上方块的垂直间距
     */
    float overviewVerSpacing;

    /**
     * 概览图的比例
     */
    float overviewScale = 4.8f;

    /**
     * 荧幕高度
     */
    float screenHeight;

    /**
     * 荧幕默认宽度与座位图的比例
     */
    float screenWidthScale = 0.5f;

    /**
     * 荧幕最小宽度
     */
    int defaultScreenWidth;

    /**
     * 标识是否正在缩放
     */
    boolean isScaling;
    float scaleX, scaleY;

    /**
     * 是否是第一次缩放
     */
    boolean firstScale = true;

    /**
     * 最多可以选择的座位数量
     */
    int maxSelected = 1;

    /**
     * 荧幕名称
     */
//    private String screenName = "";

    /**
     * 整个概览图的宽度
     */
    float rectW;

    /**
     * 整个概览图的高度
     */
    float rectH;

    Paint headPaint;
    Bitmap headBitmap;

    /**
     * 是否第一次执行onDraw
     */
    boolean isFirstDraw = true;

    /**
     * 标识是否需要绘制概览图
     */
    boolean isDrawOverview = false;

    /**
     * 标识是否需要更新概览图
     */
    boolean isDrawOverviewBitmap = true;

    int overview_checked;
    int overview_sold;
    int txt_color;
    int seatCheckedResID;
    int seatSoldResID;
    int seatAvailableResID;

    boolean isOnClick;

    /**
     * 座位已售
     */
    private static final int SEAT_TYPE_SOLD = 1;

    /**
     * 座位已经选中
     */
    private static final int SEAT_TYPE_SELECTED = 2;

    /**
     * 座位可选
     */
    private static final int SEAT_TYPE_AVAILABLE = 3;

    /**
     * 座位不可用
     */
    private static final int SEAT_TYPE_NOT_AVAILABLE = 4;

    private int downX, downY;
    private boolean pointer;

    /**
     * 顶部高度,可选,已选,已售区域的高度
     */
    float headHeight;

    Paint pathPaint;
    RectF rectF;

    /**
     * 头部下面横线的高度
     */
    int borderHeight = 1;
    Paint redBorderPaint;

    /**
     * 默认的座位图宽度,如果使用的自己的座位图片比这个尺寸大或者小,会缩放到这个大小
     */
    private float defaultImgW = 40;

    /**
     * 默认的座位图高度
     */
    private float defaultImgH = 34;

    /**
     * 座位图片的宽度
     */
    private int seatWidth;

    /**
     * 座位图片的高度
     */
    private int seatHeight;

    public AreaSelectView(Context context) {
        super(context);
    }

    public AreaSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AreaSelectView);
        overview_checked = typedArray.getColor(R.styleable.AreaSelectView_overview_checked, Color.parseColor("#5A9E64"));
        overview_sold = typedArray.getColor(R.styleable.AreaSelectView_overview_occupied, Color.RED);
        txt_color=typedArray.getColor(R.styleable.AreaSelectView_txt_color,Color.WHITE);
        seatCheckedResID = typedArray.getResourceId(R.styleable.AreaSelectView_area_checked, R.drawable.seat_green);
        seatSoldResID = typedArray.getResourceId(R.styleable.AreaSelectView_overview_occupied, R.drawable.seat_sold);
        seatAvailableResID = typedArray.getResourceId(R.styleable.AreaSelectView_area_available, R.drawable.seat_gray);
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

    float xScale1 = 1;
    float yScale1 = 1;

    private void init() {
        defaultScreenWidth = (int) dip2Px(80);

        seatBitmap = BitmapFactory.decodeResource(getResources(), seatAvailableResID);

        seatHeight= (int) (seatBitmap.getHeight()*yScale1);
        seatWidth= (int) (seatBitmap.getWidth()*xScale1);

        checkedSeatBitmap = BitmapFactory.decodeResource(getResources(), seatCheckedResID);
        seatSoldBitmap = BitmapFactory.decodeResource(getResources(), seatSoldResID);

        seatBitmapWidth = (int) (column * seatBitmap.getWidth()*xScale1);
        seatBitmapHeight = (int) (row * seatBitmap.getHeight()*yScale1);
        paint.setColor(Color.RED);

        screenHeight = dip2Px(20);
        headHeight = dip2Px(30);

        headPaint = new Paint();
        headPaint.setStyle(Paint.Style.FILL);
        headPaint.setTextSize(24);
        headPaint.setColor(Color.WHITE);
        headPaint.setAntiAlias(true);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(Color.parseColor("#e2e2e2"));

        redBorderPaint = new Paint();
        redBorderPaint.setAntiAlias(true);
        redBorderPaint.setColor(Color.RED);
        redBorderPaint.setStyle(Paint.Style.STROKE);
        redBorderPaint.setStrokeWidth(getResources().getDisplayMetrics().density * 1);

        rectF = new RectF();

        rectHeight = seatHeight / overviewScale;
        rectWidth = seatWidth / overviewScale;
        overviewSpacing = 0;
        overviewVerSpacing = 0;

        rectW = column * rectWidth + (column - 1) * overviewSpacing + overviewSpacing * 2;
        rectH = row * rectHeight + (row - 1) * overviewVerSpacing + overviewVerSpacing * 2;
        overviewBitmap = Bitmap.createBitmap((int) rectW, (int) rectH, Bitmap.Config.ARGB_4444);

        matrix.postTranslate(0, headHeight + screenHeight + borderHeight);

    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (rootShape != null) {
            rootShape.setBoundLimit(this.getWidth(), this.getHeight());
        }
    }

    public AbstractShape getRootShape() {
        return rootShape;
    }

    public void setRootShape(AbstractShape rootShape) {
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long startTime = System.currentTimeMillis();
        if (row <= 0 || column == 0) {
            return;
        }

        canvas.drawRGB(196, 196, 196);

        zoom = getMatrixScaleX();
        float translateX = getTranslateX();
        float translateY = getTranslateY();
        float scaleX = zoom;
        float scaleY = zoom;
        tempMatrix.setTranslate(translateX, translateY);
        tempMatrix.postScale(xScale1, yScale1, translateX, translateY);
        tempMatrix.postScale(scaleX, scaleY, translateX, translateY);

        if (rootShape != null) {
            rootShape.draw(canvas, tempMatrix, pathPaint);
        }

        if (headBitmap == null) {
            headBitmap = drawHeadInfo();
        }
        canvas.drawBitmap(headBitmap, 0, 0, null);

        if (isDrawOverview) {
            long s = System.currentTimeMillis();
            if (isDrawOverviewBitmap) {
                drawOverview();
            }
            canvas.drawBitmap(overviewBitmap, 0, 0, null);
            drawOverview(canvas);
            Log.d("drawTime", "OverviewDrawTime:" + (System.currentTimeMillis() - s));
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
                isDrawOverview = true;
                handler.removeCallbacks(hideOverviewRunnable);
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
                handler.postDelayed(hideOverviewRunnable, 1500);

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

    private Runnable hideOverviewRunnable = new Runnable() {
        @Override
        public void run() {
            isDrawOverview = false;
            invalidate();
        }
    };

    Bitmap drawHeadInfo() {
        String txt = "已售";
        float txtY = getBaseLine(headPaint, 0, headHeight);
        int txtWidth = (int) headPaint.measureText(txt);
        float spacing = dip2Px(10);
        float spacing1 = dip2Px(5);
        float y = (headHeight - seatBitmap.getHeight()) / 2;

        float width = seatBitmap.getWidth() + spacing1 + txtWidth + spacing + seatSoldBitmap.getWidth() + txtWidth + spacing1 + spacing + checkedSeatBitmap.getHeight() + spacing1 + txtWidth;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), (int) headHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        //绘制背景
        canvas.drawRect(0, 0, getWidth(), headHeight, headPaint);
        headPaint.setColor(Color.BLACK);

        float startX = (getWidth() - width) / 2;
        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(startX,(headHeight - seatHeight) / 2);
        canvas.drawBitmap(seatBitmap, tempMatrix, headPaint);
        canvas.drawText("可选", startX + seatWidth + spacing1, txtY, headPaint);

        float soldSeatBitmapY = startX + seatBitmap.getWidth() + spacing1 + txtWidth + spacing;
        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(soldSeatBitmapY,(headHeight - seatHeight) / 2);
        canvas.drawBitmap(seatSoldBitmap, tempMatrix, headPaint);
        canvas.drawText("已租完", soldSeatBitmapY + seatWidth + spacing1, txtY, headPaint);

        float checkedSeatBitmapX = soldSeatBitmapY + seatSoldBitmap.getWidth() + spacing1 + txtWidth + spacing;
        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(checkedSeatBitmapX,y);
        canvas.drawBitmap(checkedSeatBitmap, tempMatrix, headPaint);
        canvas.drawText("已选", checkedSeatBitmapX + spacing1 + seatWidth, txtY, headPaint);

        //绘制分割线
        headPaint.setStrokeWidth(1);
        headPaint.setColor(Color.GRAY);
        canvas.drawLine(0, headHeight, getWidth(), headHeight, headPaint);
        return bitmap;

    }

    Matrix tempMatrix = new Matrix();

    private int getSeatType(int row, int column) {

        if (isHave(getID(row, column)) >= 0) {
            return SEAT_TYPE_SELECTED;
        }

        return SEAT_TYPE_AVAILABLE;
    }

    private int getID(int row, int column) {
        return row * this.column + (column + 1);
    }

    /**
     * 绘制选中座位的行号列号
     *
     * @param row
     * @param column
     */
    private void drawText(Canvas canvas, int row, int column, float top, float left) {

        String txt = (row + 1) + "排";
        String txt1 = (column + 1) + "座";

        TextPaint txtPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(txt_color);
        txtPaint.setTypeface(Typeface.DEFAULT_BOLD);
        float seatHeight = this.seatHeight * getMatrixScaleX();
        float seatWidth = this.seatWidth * getMatrixScaleX();
        txtPaint.setTextSize(seatHeight / 3);

        //获取中间线
        float center = seatHeight / 2;
        float txtWidth = txtPaint.measureText(txt);
        float startX = left + seatWidth / 2 - txtWidth / 2;

        //只绘制一行文字
        if(txt1==null){
            canvas.drawText(txt, startX, getBaseLine(txtPaint, top, top + seatHeight), txtPaint);
        }else {
            canvas.drawText(txt, startX, getBaseLine(txtPaint, top, top + center), txtPaint);
            canvas.drawText(txt1, startX, getBaseLine(txtPaint, top + center, top + center + seatHeight / 2), txtPaint);
        }

        if (DEBUG) {
            Log.d("drawTest:", "top:" + top);
        }
    }

    int bacColor = Color.parseColor("#7e000000");

    /**
     * 绘制概览图
     */
    void drawOverview(Canvas canvas) {

        //绘制红色框
        int left = (int) -getTranslateX();
        if (left < 0) {
            left = 0;
        }
        left /= overviewScale;
        left /= getMatrixScaleX();

        int currentWidth = (int) (getTranslateX() + (column * seatWidth));
        if (currentWidth > getWidth()) {
            currentWidth = currentWidth - getWidth();
        } else {
            currentWidth = 0;
        }
        int right = (int) (rectW - currentWidth / overviewScale / getMatrixScaleX());

        float top = -getTranslateY() + headHeight;
        if (top < 0) {
            top = 0;
        }
        top /= overviewScale;
        top /= getMatrixScaleY();
        if (top > 0) {
            top += overviewVerSpacing;
        }

        int currentHeight = (int) (getTranslateY() + (row * seatHeight) * getMatrixScaleY());
        if (currentHeight > getHeight()) {
            currentHeight = currentHeight - getHeight();
        } else {
            currentHeight = 0;
        }
        int bottom = (int) (rectH - currentHeight / overviewScale / getMatrixScaleY());

        canvas.drawRect(left, top, right, bottom, redBorderPaint);
    }

    Bitmap drawOverview() {
        isDrawOverviewBitmap = false;

        int bac = Color.parseColor("#7e000000");
        overviewPaint.setColor(bac);
        overviewPaint.setAntiAlias(true);
        overviewPaint.setStyle(Paint.Style.FILL);
        overviewBitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(overviewBitmap);
        //绘制透明灰色背景
        canvas.drawRect(0, 0, rectW, rectH, overviewPaint);

        overviewPaint.setColor(Color.WHITE);
        for (int i = 0; i < row; i++) {
            float top = i * rectHeight + i * overviewVerSpacing + overviewVerSpacing;
            for (int j = 0; j < column; j++) {

                int seatType = getSeatType(i, j);
                switch (seatType) {
                    case SEAT_TYPE_AVAILABLE:
                        overviewPaint.setColor(Color.WHITE);
                        break;
                    case SEAT_TYPE_NOT_AVAILABLE:
                        continue;
                    case SEAT_TYPE_SELECTED:
                        overviewPaint.setColor(overview_checked);
                        break;
                    case SEAT_TYPE_SOLD:
                        overviewPaint.setColor(overview_sold);
                        break;
                }

                float left;

                left = j * rectWidth + j * overviewSpacing + overviewSpacing;
                canvas.drawRect(left, top, left + rectWidth, top + rectHeight, overviewPaint);
            }
        }

        return overviewBitmap;
    }

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
        float currentSeatBitmapWidth = seatBitmapWidth * getMatrixScaleX();
        float currentSeatBitmapHeight = seatBitmapHeight * getMatrixScaleY();
        float moveYLength = 0;
        float moveXLength = 0;

        //处理左右滑动的情况
        if (currentSeatBitmapWidth < getWidth()) {
            if (getTranslateX() < 0 || getMatrixScaleX() < 0) {
                //计算要移动的距离
                if (getTranslateX() < 0) {
                    moveXLength = (-getTranslateX());
                } else {
                    moveXLength = 0 - getTranslateX();
                }
            } else {
                if (getTranslateX() < 0) {
                    moveXLength = (-getTranslateX());
                } else {
                    moveXLength = 0 - getTranslateX();
                }
            }
        } else {

            if (getTranslateX() < 0 && getTranslateX() + currentSeatBitmapWidth > getWidth()) {

            } else {
                //往左侧滑动
                if (getTranslateX() + currentSeatBitmapWidth < getWidth()) {
                    moveXLength = getWidth() - (getTranslateX() + currentSeatBitmapWidth);
                } else {
                    //右侧滑动
                    moveXLength = -getTranslateX();
                }
            }

        }

        float startYPosition = screenHeight * getMatrixScaleY() + headHeight + borderHeight;

        //处理上下滑动
        if (currentSeatBitmapHeight+headHeight < getHeight()) {

            if (getTranslateY() < startYPosition) {
                moveYLength = startYPosition - getTranslateY();
            } else {
                moveYLength = -(getTranslateY() - (startYPosition));
            }

        } else {

            if (getTranslateY() < 0 && getTranslateY() + currentSeatBitmapHeight > getHeight()) {

            } else {
                //往上滑动
                if (getTranslateY() + currentSeatBitmapHeight < getHeight()) {
                    moveYLength = getHeight() - (getTranslateY() + currentSeatBitmapHeight);
                } else {
                    moveYLength = -(getTranslateY() - (startYPosition));
                }
            }
        }

        Point start = new Point();
        start.x = (int) getTranslateX();
        start.y = (int) getTranslateY();

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

    Handler handler = new Handler();

    ArrayList<Integer> selects = new ArrayList<>();

    public ArrayList<String> getSelectedSeat(){
        ArrayList<String> results=new ArrayList<>();
        for(int i=0;i<this.row;i++){
            for(int j=0;j<this.column;j++){
                if(isHave(getID(i,j))>=0){
                    results.add(i+","+j);
                }
            }
        }
        return results;
    }

    private int isHave(Integer seat) {
        return Collections.binarySearch(selects, seat);
    }

    private void remove(int index) {
        selects.remove(index);
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

    private float getBaseLine(Paint p, float top, float bottom) {
        Paint.FontMetrics fontMetrics = p.getFontMetrics();
        int baseline = (int) ((bottom + top - fontMetrics.bottom - fontMetrics.top) / 2);
        return baseline;
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

    public void setData(int row, int column) {
        this.row = row;
        this.column = column;
        init();
        invalidate();
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
        public boolean onSingleTapConfirmed(MotionEvent e) {
            isOnClick = true;
            int x = (int) e.getX();
            int y = (int) e.getY();

            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    int tempX = (int) ((j * seatWidth) * getMatrixScaleX() + getTranslateX());
                    int maxTemX = (int) (tempX + seatWidth * getMatrixScaleX());

                    int tempY = (int) ((i * seatHeight) * getMatrixScaleY() + getTranslateY());
                    int maxTempY = (int) (tempY + seatHeight * getMatrixScaleY());

                }
            }

            return super.onSingleTapConfirmed(e);
        }
    });

    private void addChooseSeat(int row, int column) {
        int id = getID(row, column);
        for (int i = 0; i < selects.size(); i++) {
            int item = selects.get(i);
            if (id < item) {
                selects.add(i, id);
                return;
            }
        }

        selects.add(id);
    }

    public interface SeatChecker {
        /**
         * 是否可用座位
         *
         * @param row
         * @param column
         * @return
         */
        boolean isValidSeat(int row, int column);

        /**
         * 是否已售
         *
         * @param row
         * @param column
         * @return
         */
        boolean isSold(int row, int column);

        void checked(int row, int column);

        void unCheck(int row, int column);

        /**
         * 获取选中后座位上显示的文字
         * @param row
         * @param column
         * @return 返回2个元素的数组,第一个元素是第一行的文字,第二个元素是第二行文字,如果只返回一个元素则会绘制到座位图的中间位置
         */
        String[] checkedSeatTxt(int row,int column);

    }

//    public void setScreenName(String screenName) {
//        this.screenName = screenName;
//    }

    public void setMaxSelected(int maxSelected) {
        this.maxSelected = maxSelected;
    }

}
