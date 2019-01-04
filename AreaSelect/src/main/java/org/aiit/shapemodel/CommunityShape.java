package org.aiit.shapemodel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommunityShape extends AbstractShape {
    private Bitmap bgBitmap;
    private String bgImageUrl;
    private float bgScale = 0;
    private List<BuildingSiteShape> buildingSiteShapeList = new ArrayList<BuildingSiteShape>();
    private final Handler handler = new Handler();

    public CommunityShape(int id) {
        super(id);
    }

    public String getBgImageUrl() {
        return bgImageUrl;
    }

    public void setBgImageUrl(String bgImageUrl) {
        this.bgImageUrl = bgImageUrl;
        ShapeUtil.getBitmapFromURL(bgImageUrl, new ShapeUtil.LoadBitmapCallback() {
            @Override
            public void bitmapLoaded(final Bitmap bitmap) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        CommunityShape.this.bgBitmap = bitmap;
                        CommunityShape.super.invalidateCallback.invalidate();
                    }
                });
            }
        });
    }

    @Override
    public void initWithJson(JSONObject json) throws JSONException {
        super.initWithJson(json);
        this.bgImageUrl = json.optString("bgImageUrl");
        ShapeUtil.getBitmapFromURL(bgImageUrl, new ShapeUtil.LoadBitmapCallback() {
            @Override
            public void bitmapLoaded(final Bitmap bitmap) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        CommunityShape.this.bgBitmap = bitmap;
                        CommunityShape.super.invalidateCallback.invalidate();
                    }
                });
            }
        });
        JSONArray arr = json.optJSONArray("buildingSiteShapeList");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject buildingSiteJson = arr.getJSONObject(i);
            int id = buildingSiteJson.optInt("id");
            BuildingSiteShape shape = new BuildingSiteShape(id);
            shape.initWithJson(buildingSiteJson);
            this.buildingSiteShapeList.add(shape);
        }
    }

    @Override
    public void draw(Canvas canvas, Matrix transform, Paint paint) {
        if (bgBitmap == null) {
            return;
        }
        if (transform == null) {
            Rect src = new Rect(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight());
            canvas.drawBitmap(bgBitmap, src, super.bound, paint);
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
            canvas.drawBitmap(bgBitmap, transform, paint);
        }
        if (buildingSiteShapeList != null) {
            for (BuildingSiteShape s : buildingSiteShapeList) {
                s.draw(canvas, transform, paint);
            }
        }
    }
}
