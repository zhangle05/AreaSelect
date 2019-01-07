package org.aiit.shapemodel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuildingShape extends AbstractShape {
    private FloorShape curFloor;
    private List<FloorShape> floorShapeList = new ArrayList<FloorShape>();

    public BuildingShape(String id, ShapeManager mgr) {
        super(id, mgr);
    }

    @Override
    public void initWithJson(JSONObject json) throws JSONException {
        super.initWithJson(json);
        JSONArray arr = json.optJSONArray("floorShapeList");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject floorJson = arr.getJSONObject(i);
            String id = floorJson.optString("id");
            FloorShape shape = new FloorShape(id, mgr);
            shape.initWithJson(floorJson);
            this.floorShapeList.add(shape);
        }
        if (floorShapeList.size() > 0) {
            curFloor = floorShapeList.get(0);
            curFloor.setShapeSelectCallback(this.shapeSelectCallback);
        }
    }

    @Override
    public void setShapeSelectCallback(ShapeSelectCallback shapeSelectCallback) {
        super.setShapeSelectCallback(shapeSelectCallback);
        if (curFloor != null) {
            curFloor.setShapeSelectCallback(shapeSelectCallback);
        }
    }

    @Override
    public float setBoundLimit(int maxWidth, int maxHeight) {
        float ratio = super.setBoundLimit(maxWidth, maxHeight);
        for (FloorShape s : floorShapeList) {
            s.setBoundLimit(maxWidth, maxHeight);
        }
        return ratio;
    }

    @Override
    public void draw(Canvas canvas, Matrix transform) {
        if (curFloor == null) {
            return;
        }
        curFloor.draw(canvas, transform);
    }

    @Override
    public void drawLegend(Canvas canvas, RectF bound) {
        if (curFloor == null) {
            return;
        }
        curFloor.drawLegend(canvas, bound);
    }

    @Override
    public void drawInfo(Canvas canvas, RectF bound) {
        if (curFloor == null) {
            return;
        }
        curFloor.drawInfo(canvas, bound);
    }

    @Override
    public AbstractShape getSelectedShape() {
        if (curFloor == null) {
            return null;
        }
        return curFloor.getSelectedShape();
    }

    @Override
    public boolean onSingleTap(float x, float y) {
        if (curFloor == null) {
            return false;
        }
        return curFloor.onSingleTap(x, y);
    }

    @Override
    public List<AbstractShape> getChildren() {
        List<AbstractShape> children = new ArrayList<AbstractShape>();
        children.addAll(floorShapeList);
        return children;
    }

}
