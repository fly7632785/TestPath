package com.jafir.testpath;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by jafir on 2017/12/8.
 *
 *
 * 模拟地图定位marker在轨迹上做平滑的移动
 *
 * 模拟现在地图路径path上有两个定位点
 * （这一次的和上一次的，由于时间差太大，如果不做处理，会出现这一帧和下一帧移动太大出现 “跳跃的情况”）
 * 截取两个点之间的path，再做一次动画。
 *
 */
public class MyView extends View {
    private Paint paint;
    private float distance;
    private float[] pos;
    private Paint circlePaint;
    private ValueAnimator valueAnimator;
    private Point oldPoint;
    private Point nowPoint;

    public MyView(Context context) {
        super(context);
        init(context);
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Path path;
    private PathMeasure pathMeasure;
    private PathMeasure partPathMeasure;

    private Point points[] = new Point[]{
            new Point(100, 100),
            new Point(100, 150),
            new Point(150, 150),
            new Point(50, 300),
            new Point(220, 400),
            new Point(10, 600),
            new Point(400, 700),
            new Point(400, 800),
            new Point(400, 900),
            new Point(400, 1000),
    };


    private void init(Context context) {
        path = new Path();
        for (int i = 0; i < points.length; i++) {
            path.lineTo(points[i].x, points[i].y);
        }
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);

        circlePaint = new Paint();
        circlePaint.setColor(Color.YELLOW);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pathMeasure = new PathMeasure(path, false);

        oldPoint = points[4];
        nowPoint = points[8];
        pos = new float[]{oldPoint.x, oldPoint.y};
    }

    public void reset() {
        valueAnimator.cancel();
        pos = new float[]{oldPoint.x, oldPoint.y};
        distance = 0;
        invalidate();
    }


    public void startMove(int time) {


        // 求取两个距离，从而通过getSegment求得，截取后的path
        float start = getPointDistanceInPath(pathMeasure, oldPoint);
        System.out.println("count1:"+ count);
        float stop = getPointDistanceInPath(pathMeasure, nowPoint);
        System.out.println("count2:"+ count);

        //计算这个两个点在path上的距离
        Path dst = new Path();
        pathMeasure.getSegment(start, stop, dst, true);
        partPathMeasure = new PathMeasure(dst, false);
        final float speed = partPathMeasure.getLength() / time;
        valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(time);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.d("debug", "a:" + animation.getAnimatedValue());
                if (distance < partPathMeasure.getLength()) {
                    pos = new float[2];
                    float[] tan = new float[2];
                    // getPosTan pins the distance along the Path and
                    // computes the position and the tangent.
                    partPathMeasure.getPosTan(distance, pos, tan);
                    distance = partPathMeasure.getLength() * Float.valueOf(animation.getAnimatedValue().toString());   // Traversal
                    invalidate();
                }
            }
        });
        valueAnimator.start();
    }

    /**
     * 通过二分法来查找对应点的distance
     * @param pm
     * @param point
     * @return
     */
    private float getPointDistanceInPath(PathMeasure pm, Point point) {
        float length = pm.getLength();
        final float[] p = {point.x, point.y};
        float threshold = length / 1000;

        float start = 0;
        float end = length;
        while (true) {
            if (Math.abs(end - start) < threshold) {
                break;
            }
            if (isLeft(pm, p, start, end)) {
                end = getMid(start, end);
            } else {
                start = getMid(start, end);
            }
        }
        return start;
    }

    final float[] left = new float[2];
    final float[] right = new float[2];
    final float[] tan = new float[2];
    int count ;

    private float getMid(float start, float end) {
        return (start + end) / 2;
    }

    private boolean isLeft(PathMeasure pathMeasure, float[] point, float start, float end) {
        float mid = getMid(start, end);
        float leftD = getMid(start, mid);
        float rightD = getMid(mid, end);
        pathMeasure.getPosTan(leftD, left, tan);
        pathMeasure.getPosTan(rightD, right, tan);
        count++;
        return getTwoPointD(point, left) < getTwoPointD(point, right);
    }

    private float getTwoPointD(float[] point, float[] point2) {
        float dx = point[0] - point2[0];
        float dy = point[1] - point2[1];
        return (float) Math.hypot(dx, dy);
    }


    /**
     * @param pathMeasure pathMeasure
     * @param nowPoint    path上面一个点，想要知道点到起始的距离
     * @return 求点到起始的距离
     *
     * 不建议使用 有问题
     *
     */
    private float getDistance(PathMeasure pathMeasure, Point nowPoint) {
        float d = 0;
        while (d < pathMeasure.getLength()) {
            d += 1;
            float[] tan = new float[2];
            float[] pos = new float[2];
            pathMeasure.getPosTan(d, pos, tan);
            if ((int) pos[0] == nowPoint.x && (int) pos[1] == nowPoint.y) {
                return d;
            }
        }
        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
        canvas.drawCircle(pos[0], pos[1], 50, circlePaint);
    }
}
