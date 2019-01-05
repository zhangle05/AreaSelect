package org.aiit.views.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.aiit.shapemodel.AbstractShape;
import org.aiit.shapemodel.BuildingShape;
import org.aiit.shapemodel.ShapeManager;

public class FloorActivity extends AppCompatActivity {
    public org.aiit.widgets.AreaSelectView floorView;
    /**
     * building sample json:
     * {
     *     "id":"233",
     *     "name":"2号楼",
     *     "floorShapeList":
     *     [
     *         {
     *             "id":"2331",
     *             "name":"1层",
     *             "bgImageUrl":"http://static.octopusdio.com/floor.jpg",
     *             "bound":{
     *                 "left":0,
     *                 "top":0,
     *                 "right":1280,
     *                 "bottom":696
     *             },
     *             "apartmentShapeList":[
     *                 {
     *                     "id":"23311",
     *                     "name":"2101",
     *                     "available":true,
     *                     "bound":{
     *                         "left":170,
     *                         "top":180,
     *                         "right":370,
     *                         "bottom":280
     *                     }
     *                 },
     *                 {
     *                     "id":"23312",
     *                     "name":"2102",
     *                     "available":true,
     *                     "bound":{
     *                         "left":425,
     *                         "top":350,
     *                         "right":625,
     *                         "bottom":450
     *                     }
     *                 },
     *                 {
     *                     "id":"23313",
     *                     "name":"2103",
     *                     "available":false,
     *                     "bound":{
     *                         "left":642,
     *                         "top":350,
     *                         "right":842,
     *                         "bottom":450
     *                     }
     *                 },
     *                 {
     *                     "id":"23314",
     *                     "name":"2104",
     *                     "available":true,
     *                     "bound":{
     *                         "left":900,
     *                         "top":180,
     *                         "right":1100,
     *                         "bottom":280
     *                     }
     *                 }
     *             ]
     *         }
     *     ]
     * }
     */
    private BuildingShape building;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_floor);

        floorView = (org.aiit.widgets.AreaSelectView) findViewById(R.id.floorView);
        String buildingJson = "{\n" +
                "    \"id\":\"233\",\n" +
                "    \"name\":\"2号楼\",\n" +
                "    \"floorShapeList\":\n" +
                "    [\n" +
                "        {\n" +
                "            \"id\":\"2331\",\n" +
                "            \"name\":\"1层\",\n" +
                "            \"bgImageUrl\":\"http://static.octopusdio.com/floor.jpg\",\n" +
                "            \"bound\":{\n" +
                "                \"left\":0,\n" +
                "                \"top\":0,\n" +
                "                \"right\":1280,\n" +
                "                \"bottom\":696\n" +
                "            },\n" +
                "            \"apartmentShapeList\":[\n" +
                "                {\n" +
                "                    \"id\":\"23311\",\n" +
                "                    \"name\":\"2101\",\n" +
                "                    \"available\":true,\n" +
                "                    \"bound\":{\n" +
                "                        \"left\":170,\n" +
                "                        \"top\":180,\n" +
                "                        \"right\":370,\n" +
                "                        \"bottom\":280\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\":\"23312\",\n" +
                "                    \"name\":\"2102\",\n" +
                "                    \"available\":true,\n" +
                "                    \"bound\":{\n" +
                "                        \"left\":425,\n" +
                "                        \"top\":350,\n" +
                "                        \"right\":625,\n" +
                "                        \"bottom\":450\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\":\"23313\",\n" +
                "                    \"name\":\"2103\",\n" +
                "                    \"available\":false,\n" +
                "                    \"bound\":{\n" +
                "                        \"left\":642,\n" +
                "                        \"top\":350,\n" +
                "                        \"right\":842,\n" +
                "                        \"bottom\":450\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\":\"23314\",\n" +
                "                    \"name\":\"2104\",\n" +
                "                    \"available\":true,\n" +
                "                    \"bound\":{\n" +
                "                        \"left\":900,\n" +
                "                        \"top\":180,\n" +
                "                        \"right\":1100,\n" +
                "                        \"bottom\":280\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        ShapeManager mgr = new ShapeManager();
        building = mgr.parseBuilding(buildingJson);
        floorView.setRootShape(building);

        btnDone = (Button)findViewById(R.id.done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (building == null) {
                    Toast.makeText(FloorActivity.this, "未找到楼层信息", Toast.LENGTH_LONG).show();
                    return;
                }
                AbstractShape selected = building.getSelectedShape();
                if (selected == null) {
                    Toast.makeText(FloorActivity.this, "请先选择户号", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(FloorActivity.this, "已选择：" + selected.getName(), Toast.LENGTH_LONG).show();
            }
        });
        super.onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (floorView != null) {
            floorView.invalidate();
        }
    }

}