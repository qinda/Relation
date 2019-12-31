package com.jiatui.relation;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * <pre>
 *      author : fangx
 *      e-mail : fangx@hyxt.com
 *      time   : 2019/12/28
 *      desc   :
 * </pre>
 */
public class RelationUtils {

    public static Point calcPointWithAngle(Point center, int radius, double angle) {
        double radian = Math.toRadians(angle);
        int x = (int) (center.x + Math.cos(radian) * radius);
        int y = (int) (center.y + Math.sin(radian) * radius);
        return new Point(x, y);
    }

    public static Point calcPointWithAngle(int x, int y, int radius, double angle) {
        double radian = Math.toRadians(angle);
        int x_ = (int) (x + Math.cos(radian) * radius);
        int y_ = (int) (y + Math.sin(radian) * radius);
        return new Point(x_, y_);
    }

}
