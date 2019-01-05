package org.aiit.shapemodel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApartmentShape extends AbstractShape {
    private Paint textPaint;
    private Paint boundPaint;
    private Typeface textFont;
    private Typeface selectedTextFont;
    private RectF drawRect;

    public ApartmentShape(String id, ShapeManager mgr) {
        super(id, mgr);
        boundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boundPaint.setColor(Color.BLACK);
        boundPaint.setStyle(Paint.Style.STROKE);
        boundPaint.setStrokeWidth(3);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textFont = Typeface.create("黑体", Typeface.NORMAL);
        selectedTextFont = Typeface.create("黑体", Typeface.BOLD);
        textPaint.setTypeface(textFont);
    }

    @Override
    public void initWithJson(JSONObject json) throws JSONException {
        super.initWithJson(json);
    }

    @Override
    public void draw(Canvas canvas, Matrix transform) {
        drawRect = new RectF(super.bound);
        if (transform != null) {
            transform.mapRect(drawRect);
        }
        // draw bound
        if (!this.available) {
            boundPaint.setColor(Color.RED);
            boundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            textPaint.setColor(Color.YELLOW);
        } else if (this.selected) {
            boundPaint.setColor(Color.BLUE);
            boundPaint.setStrokeWidth(5);
            textPaint.setColor(Color.BLUE);
            textPaint.setTypeface(selectedTextFont);
            defaultPaint.setColor(Color.parseColor("#e2FFe2"));
        } else {
            boundPaint.setColor(Color.BLACK);
            boundPaint.setStrokeWidth(3);
            textPaint.setColor(Color.BLACK);
            textPaint.setTypeface(textFont);
            defaultPaint.setColor(Color.parseColor("#e2e2e2"));
        }
        canvas.drawRect(drawRect, defaultPaint);
        canvas.drawRect(drawRect, boundPaint);

        // draw name
        float textSize1 = super.bound.height() / 2;
        float textSize2 = super.bound.width() / super.name.length();
        float textSize = (textSize1 < textSize2 ? textSize1 : textSize2)  * (drawRect.width() / super.bound.width());
        textPaint.setTextSize(textSize);
        float txtWidth = textPaint.measureText(super.name);
        float x = drawRect.left + (drawRect.width() - txtWidth) / 2;
        float y = drawRect.bottom - (drawRect.height() - textSize) / 2;
        canvas.drawText(super.name, x, y, textPaint);
    }

    @Override
    public boolean onSingleTap(float x, float y) {
        if (!this.available || !this.selectable || drawRect == null) {
            return false;
        }
        if (drawRect.contains(x, y)) {
            this.selected = !this.selected;
            return true;
        }
        return false;
    }

    @Override
    public List<AbstractShape> getChildren() {
        List<AbstractShape> children = new ArrayList<AbstractShape>();
        return children;
    }
}
