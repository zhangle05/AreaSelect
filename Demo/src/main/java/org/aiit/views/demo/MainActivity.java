package org.aiit.views.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.aiit.shapemodel.AbstractShape;
import org.aiit.shapemodel.CommunityShape;
import org.aiit.shapemodel.ShapeManager;

public class MainActivity extends AppCompatActivity {
    public org.aiit.widgets.AreaSelectView communityView;
    /**
     * community sample json:
     * {
     *     "id":"1",
     *     "name":"立涛园",
     *     "bound":{
     *         "left":0,
     *         "top":0,
     *         "right":1334,
     *         "bottom":1000
     *     },
     *     "bgImageUrl":"https://res.co188.com/data/drawing/img640/306151284459805.jpg",
     *     "buildingSiteShapeList":[
     *         {
     *             "id":"2",
     *             "name":"1号楼",
     *             "available":false,
     *             "bound":{
     *                 "left":130,
     *                 "top":165,
     *                 "right":165,
     *                 "bottom":189
     *             }
     *         },
     *         {
     *             "id":"3",
     *             "name":"2号楼",
     *             "available":true,
     *             "bound":{
     *                 "left":130,
     *                 "top":205,
     *                 "right":183,
     *                 "bottom":222
     *             }
     *         },
     *         {
     *             "id":"4",
     *             "name":"3号楼",
     *             "available":true,
     *             "bound":{
     *                 "left":200,
     *                 "top":200,
     *                 "right":235,
     *                 "bottom":224
     *             }
     *         }
     *     ]
     * }
     */
    private CommunityShape community;
    private TextView txtInfo;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        communityView = (org.aiit.widgets.AreaSelectView) findViewById(R.id.communityView);
        String communityJson = "{\n" +
                "    \"id\":\"1\",\n" +
                "    \"name\":\"立涛园\",\n" +
                "    \"bound\":{\n" +
                "        \"left\":0,\n" +
                "        \"top\":0,\n" +
                "        \"right\":1334,\n" +
                "        \"bottom\":1000\n" +
                "    },\n" +
                "    \"bgImageUrl\":\"https://res.co188.com/data/drawing/img640/306151284459805.jpg\",\n" +
                "    \"buildingSiteShapeList\":[\n" +
                "        {\n" +
                "            \"id\":\"2\",\n" +
                "            \"name\":\"1号楼\",\n" +
                "            \"available\":false,\n" +
                "            \"bound\":{\n" +
                "                \"left\":130,\n" +
                "                \"top\":165,\n" +
                "                \"right\":165,\n" +
                "                \"bottom\":189\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"3\",\n" +
                "            \"name\":\"2号楼\",\n" +
                "            \"available\":true,\n" +
                "            \"bound\":{\n" +
                "                \"left\":130,\n" +
                "                \"top\":205,\n" +
                "                \"right\":183,\n" +
                "                \"bottom\":222\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"4\",\n" +
                "            \"name\":\"3号楼\",\n" +
                "            \"available\":true,\n" +
                "            \"bound\":{\n" +
                "                \"left\":200,\n" +
                "                \"top\":200,\n" +
                "                \"right\":235,\n" +
                "                \"bottom\":224\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        ShapeManager mgr = new ShapeManager();
        community = mgr.parseCommunity(communityJson);
        communityView.setRootShape(community);

        txtInfo = (TextView)findViewById(R.id.selectInfo);
        community.setShapeSelectCallback(new AbstractShape.ShapeSelectCallback() {
            @Override
            public void shapeSelected(AbstractShape selectedShape) {
                txtInfo.setText("已选楼栋：" + selectedShape.getName());
            }
        });

        btnNext = (Button)findViewById(R.id.next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (community == null) {
                    Toast.makeText(MainActivity.this, "未找到小区信息", Toast.LENGTH_LONG).show();
                    return;
                }
                AbstractShape selected = community.getSelectedShape();
                if (selected == null) {
                    Toast.makeText(MainActivity.this, "请先选择楼号", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, FloorActivity.class);
                intent.putExtra("BUILDING_ID", selected.getId());
                startActivity(intent);
            }
        });
    }

}
