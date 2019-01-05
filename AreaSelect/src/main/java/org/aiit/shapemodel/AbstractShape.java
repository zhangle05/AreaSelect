package org.aiit.shapemodel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractShape {
    protected String id;
    protected String name;
    protected RectF bound = new RectF();
    protected boolean selectable = true;
    protected boolean selected = false;
    protected boolean available = true;
    protected Paint defaultPaint;
    protected InvalidateCallback invalidateCallback;

    public AbstractShape(String id) {
        this.id = id;
        ShapeUtil.addShape(this);
        defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultPaint.setStyle(Paint.Style.FILL);
        defaultPaint.setColor(Color.parseColor("#e2e2e2"));
    }

    public String getName() {
        return name;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBound(RectF rect) {
        this.bound = rect;
    }
    public void setBound(float left, float top, float width, float height) {
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
        this.available = json.optBoolean("available");
    }

    public void drawLegend(Canvas canvas, RectF bound) {
        // Root shapes should override this method to draw legend
    }

    public abstract void draw(Canvas canvas, Matrix transform);

    public interface InvalidateCallback {
        void invalidate(Rect area);
        void invalidate();
    }
}
