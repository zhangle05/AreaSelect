package org.aiit.shapemodel;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BuildingSiteShape extends AbstractShape {

    public BuildingSiteShape(int id) {
        super(id);
    }

    @Override
    public void initWithJson(JSONObject json) throws JSONException {
        super.initWithJson(json);
    }

    @Override
    public void draw(Canvas canvas, Matrix transform, Paint paint) {
        RectF r = new RectF(super.bound);
        if (transform != null) {
            transform.mapRect(r);
        }
        canvas.drawRect(r, paint);
    }
}
