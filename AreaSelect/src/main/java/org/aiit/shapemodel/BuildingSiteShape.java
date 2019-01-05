package org.aiit.shapemodel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BuildingSiteShape extends AbstractShape {
    private Paint textPaint;
    private Paint boundPaint;
    private Typeface textFont;

    public BuildingSiteShape(String id) {
        super(id);
        boundPaint = new Paint();
        boundPaint.setColor(Color.BLACK);
        boundPaint.setStyle(Paint.Style.STROKE);
        boundPaint.setStrokeWidth(3);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textFont = Typeface.create("黑体", Typeface.NORMAL);
        textPaint.setTypeface(textFont);
    }

    @Override
    public void initWithJson(JSONObject json) throws JSONException {
        super.initWithJson(json);
    }

    @Override
    public void draw(Canvas canvas, Matrix transform) {
        RectF r = new RectF(super.bound);
        if (transform != null) {
            transform.mapRect(r);
        }
        // draw bound
        canvas.drawRect(r, defaultPaint);
        if (!this.available) {
            boundPaint.setColor(Color.RED);
            boundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            textPaint.setColor(Color.YELLOW);
        } else if (this.selected) {
            boundPaint.setColor(Color.BLUE);
            boundPaint.setStrokeWidth(5);
            textPaint.setColor(Color.BLUE);
        } else {
            boundPaint.setColor(Color.BLACK);
            boundPaint.setStrokeWidth(3);
            textPaint.setColor(Color.BLACK);
        }
        canvas.drawRect(r, boundPaint);

        // draw name
        float textSize = super.bound.height() / 2 * (r.width() / super.bound.width());
        textPaint.setTextSize(textSize);
        float txtWidth = textPaint.measureText(super.name);
        float x = r.left + (r.width() - txtWidth) / 2;
        float y = r.bottom - (r.height() - textSize) / 2;
        canvas.drawText(super.name, x, y, textPaint);
    }
}
