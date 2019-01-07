package org.aiit.shapemodel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FloorShape extends AbstractShape {
    private Paint infoPaint;
    private Paint textPaint;
    private Bitmap bgBitmap;
    private String bgImageUrl;
    private float bgScale = 0;
    private List<ApartmentShape> apartmentShapeList = new ArrayList<ApartmentShape>();
    private List<ApartmentShape> legendList;
    private final Handler handler = new Handler();

    public FloorShape(String id, ShapeManager mgr) {
        super(id, mgr);
        infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPaint.setColor(Color.BLACK);
        Typeface infoFont = Typeface.create("黑体", Typeface.BOLD);
        infoPaint.setTypeface(infoFont);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(infoFont);
    }

    @Override
    public void initWithJson(JSONObject json) throws JSONException {
        super.initWithJson(json);
        this.bgImageUrl = json.optString("bgImageUrl");
        ShapeManager.getBitmapFromURL(bgImageUrl, new ShapeManager.LoadBitmapCallback() {
            @Override
            public void bitmapLoaded(final Bitmap bitmap) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        FloorShape.this.bgBitmap = bitmap;
                        FloorShape.super.invalidateCallback.invalidate();
                    }
                });
            }
        });
        JSONArray arr = json.optJSONArray("apartmentShapeList");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject apartmentJson = arr.getJSONObject(i);
            String id = apartmentJson.optString("id");
            ApartmentShape shape = new ApartmentShape(id, mgr);
            shape.initWithJson(apartmentJson);
            this.apartmentShapeList.add(shape);
        }
    }

    @Override
    public void draw(Canvas canvas, Matrix transform) {
        if (bgBitmap == null) {
            RectF drawRect = new RectF(super.bound);
            if (transform != null) {
                transform.mapRect(drawRect);
            }
            String waitingText = super.name + "图片加载中，请稍等......";
            float textSize1 = super.bound.height() / 2;
            float textSize2 = super.bound.width() / waitingText.length();
            float textSize = (textSize1 < textSize2 ? textSize1 : textSize2)  * (drawRect.width() / super.bound.width());
            textPaint.setTextSize(textSize);
            float txtWidth = textPaint.measureText(waitingText);
            float x = drawRect.left + (drawRect.width() - txtWidth) / 2;
            float y = drawRect.bottom - (drawRect.height() - textSize) / 2;
            canvas.drawText(waitingText, x, y, textPaint);
            return;
        }
        if (transform == null) {
            Rect src = new Rect(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight());
            canvas.drawBitmap(bgBitmap, src, super.bound, defaultPaint);
        } else {
            if (bgScale <= 0) {
                float xRatio = super.bound.width() / bgBitmap.getWidth();
                float yRatio = super.bound.height() / bgBitmap.getHeight();
                bgScale = xRatio < yRatio ? xRatio : yRatio;
            }
            if (bgScale > 0) {
                Matrix m = new Matrix();
                m.postScale(bgScale, bgScale, 0, 0);
                m.postConcat(transform);
                transform = m;
            }
            canvas.drawBitmap(bgBitmap, transform, defaultPaint);
        }
        if (apartmentShapeList != null) {
            for (ApartmentShape s : apartmentShapeList) {
                s.draw(canvas, transform);
            }
        }
    }

    @Override
    public void drawLegend(Canvas canvas, RectF bound) {
        if (legendList == null) {
            legendList = new ArrayList<ApartmentShape>();
            float width = bound.width() / 7;
            float height = bound.height() / 1.5F;
            float top = bound.top + 5;

            ApartmentShape available = new ApartmentShape(UUID.randomUUID().toString(), mgr);
            available.setName("可选");
            available.setSelectable(false);
            available.setBound(bound.left + width, top, width, height);
            legendList.add(available);

            ApartmentShape rentOut = new ApartmentShape(UUID.randomUUID().toString(), mgr);
            rentOut.setName("已租");
            rentOut.setSelectable(false);
            rentOut.setAvailable(false);
            rentOut.setBound(bound.left + width * 3, top, width, height);
            legendList.add(rentOut);

            ApartmentShape selected = new ApartmentShape(UUID.randomUUID().toString(), mgr);
            selected.setName("已选");
            selected.setSelectable(false);
            selected.setSelected(true);
            selected.setBound(bound.left + width * 5, top, width, height);
            legendList.add(selected);
        }
        for (ApartmentShape s : legendList) {
            s.draw(canvas, null);
        }
    }

    @Override
    public void drawInfo(Canvas canvas, RectF bound) {
        AbstractShape selected = this.getSelectedShape();
        String info = "当前楼层：" + name + "；   已选择：";
        if (selected == null) {
            info += "无。";
        } else {
            info += selected.name + "。";
        }
        float txtSize = bound.height() / 2;
        infoPaint.setTextSize(txtSize);
        float txtWidth = infoPaint.measureText(info);
        float x = bound.left + (bound.width() - txtWidth) / 2;
        float y = bound.bottom - (bound.height() - txtSize) / 2;

        canvas.drawText(info, x, y, infoPaint);
    }

    @Override
    public AbstractShape getSelectedShape() {
        for (ApartmentShape s : apartmentShapeList) {
            if (s.selected) {
                return s;
            }
        }
        return null;
    }

    @Override
    public boolean onSingleTap(float x, float y) {
        for (ApartmentShape s : apartmentShapeList) {
            s.setSelected(false);
        }
        for (ApartmentShape s : apartmentShapeList) {
            if (s.onSingleTap(x, y)) {
                try {
                    if (shapeSelectCallback != null) {
                        shapeSelectCallback.shapeSelected(s);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<AbstractShape> getChildren() {
        List<AbstractShape> children = new ArrayList<AbstractShape>();
        children.addAll(apartmentShapeList);
        return children;
    }

}
