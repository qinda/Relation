package com.jiatui.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * <pre>
 *      author : fangx
 *      e-mail : fangx@hyxt.com
 *      time   : 2019/12/28
 *      desc   :
 * </pre>
 */
public class RelationView extends View {
    private static final String TAG = "RelationView";
    private Paint mPaint;
    private Point centerPoint;
    private int radius = dp2px(50);
    private int lineLength = dp2px(150);
    private int lineCount = 8;
    private TextPaint mTextPaint;
    private float mScale = 1;
    private float initialScale = 1;
    private Matrix matrix;
    private float offsetX;
    private float offsetY;

    private int viewId;

    private List<Region> childRegions;

    private Region parentRegion;

    private Point expandPoint;//在父容器的连线起点
    private Point parentPoint;//在父容器的layout 的中心店
    private Point originPoint;//原始点

    public RelationView(Context context) {
        this(context, null);
    }

    public RelationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Timber.plant(new Timber.DebugTree());
        init(context);
    }

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    /**
     * 缩放手势检测器
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private OverScroller mScroller;
    private GestureDetectorCompat detector;


    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(dp2px(18));
        mTextPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(dp2px(1));
        centerPoint = new Point();
        matrix = new Matrix();
        // mScaleGestureDetector = new ScaleGestureDetector(getContext(),
        //         new MySimpleOnScaleGestureListener());
        mScroller = new OverScroller(context);
        detector = new GestureDetectorCompat(context, new MyGestureListener());
        childRegions = new ArrayList<>();
        for (int i = 0; i < lineCount + 1; i++) {
            childRegions.add(new Region());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        centerPoint.x = cx;
        centerPoint.y = cy;
        canvas.drawCircle(cx, cy, radius, mPaint);
        if (childRegions.get(0) != null) {
            childRegions.get(0).set(cx - radius, cy - radius, cx + radius, cy + radius);
        }
        for (int i = 1; i <= lineCount; i++) {
            double angle = 360 / lineCount;
            Point point = RelationUtils.calcPointWithAngle(centerPoint, lineLength, angle * i);
            canvas.drawLine(cx, cy, point.x, point.y, mPaint);
            int smallRadius = this.radius / 2;
            canvas.drawCircle(point.x, point.y, smallRadius, mPaint);
            canvas.drawText(String.valueOf(i), point.x, point.y, mTextPaint);
            if (childRegions.get(i) != null) {
                childRegions.get(i).set(point.x - smallRadius, point.y - smallRadius,
                        point.x + smallRadius, point.y + smallRadius);
            }
        }
    }

    /**
     * 根据 点击的X,Y 判断是否有点击到child 并且 返回 子View 所在的区域
     *
     * @param x 在当前View中的x
     * @param y 在当前View中的y
     * @return 返回 子View 所在的区域 如果没有点击到 则返回null
     */
    public Rect getChildRect(int x, int y) {
        Rect rect = new Rect();
        for (Region region : childRegions) {
            if (region.contains(x, y)) {
                Timber.d("onClick:%s", childRegions.indexOf(region));
                rect.set(region.getBounds());
                return rect;
            }
        }
        return null;
    }

    public double getChildAngle(int x, int y) {
        int w = x - getMeasuredWidth() / 2;
        int h = y - getMeasuredHeight() / 2;
        double angle = Math.toDegrees(Math.atan2(h, w));
        // 修正角度 返回  0-360 之间的角度
        if (angle != 0) {
            angle = angle % 360 == 0 ? 360 : angle % 360;
            if (angle < 0) {
                angle = angle + 360;
            }
        }
        Timber.d("childAngle:%s", angle);
        return angle;
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Timber.d("onSingleTapConfirmed:x=%s,y=%s", e.getX(), e.getY());
            for (Region region : childRegions) {
                if (region.contains(((int) e.getX()), ((int) e.getY()))) {
                    Timber.d("onClick:%s", childRegions.indexOf(region));
                    break;
                }
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Timber.d("onSingleTapUp");
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // offsetX -= distanceX;
            // offsetY -= distanceY;
            // matrix.setTranslate(offsetX, offsetY);
            // invalidate();
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + .5f);
    }

    public void setParentRegion(Region parentRegion) {
        this.parentRegion = parentRegion;
    }

    public Region getParentRegion() {
        return parentRegion;
    }

    public Point getExpandPoint() {
        return expandPoint;
    }

    public void setExpandPoint(Point expandPoint) {
        this.expandPoint = expandPoint;
    }

    public Point getParentPoint() {
        return parentPoint;
    }

    public void setParentPoint(Point parentPoint) {
        this.parentPoint = parentPoint;
    }

    public void setOriginPoint(Point originPoint) {
        this.originPoint = originPoint;
    }

    public Point getOriginPoint() {
        return originPoint;
    }
}
