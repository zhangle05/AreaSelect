package org.aiit.shapemodel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShapeUtil {

    private static Map<String, AbstractShape> S_SHAPE_MAP = new HashMap<String, AbstractShape>();

    public static void addShape(AbstractShape shape) {
        if (S_SHAPE_MAP.containsKey(shape.id)) {
            throw new IllegalStateException("Shape ID '" + shape.id + "' duplicate");
        }
        S_SHAPE_MAP.put(shape.id, shape);
    }

    public static CommunityShape parseCommunity(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            String id = json.optString("id");
            CommunityShape result = new CommunityShape(id);
            result.initWithJson(json);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getBitmapFromURL(final String src, final LoadBitmapCallback callback) {
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (callback != null) {
                        callback.bitmapLoaded(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.bitmapLoaded(null);
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    public interface LoadBitmapCallback {
        void bitmapLoaded(Bitmap bitmap);
    }
}
