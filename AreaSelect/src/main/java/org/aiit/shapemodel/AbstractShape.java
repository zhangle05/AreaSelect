package org.aiit.shapemodel;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractShape {
    protected int id;
    protected String name;
    protected RectF bound = new RectF();
    protected InvalidateCallback invalidateCallback;

    public AbstractShape(int id) {
        this.id = id;
        ShapeUtil.addShape(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBound(RectF rect) {
        this.bound = rect;
    }
    public void setBound(int left, int top, int width, int height) {
        bound.set(left, top, left + width, top + height);
    }
    public float setBoundLimit(int maxWidth, int maxHeight) {
        float widthRatio = (float)maxWidth / bound.width();
        float heightRatio = (float)maxHeight / bound.height();
        float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        bound.right = bound.left + bound.width() * ratio;
        bound.bottom = bound.top + bound.height() * ratio;
        return ratio;
    }

    public void setInvalidateCallback(InvalidateCallback invalidateCallback) {
        this.invalidateCallback = invalidateCallback;
    }

    public void initWithJson(JSONObject json) throws JSONException {
        this.name = json.optString("name");
        JSONObject boundJson = json.optJSONObject("bound");
        if (boundJson != null) {
            bound.set(boundJson.optInt("left"), boundJson.optInt("top"), boundJson.optInt("right"), boundJson.optInt("bottom"));
        }
    }

    public abstract void draw(Canvas canvas, Matrix transform, Paint paint);

    public interface InvalidateCallback {
        void invalidate(Rect area);
        void invalidate();
    }
}
