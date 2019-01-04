package org.aiit.views.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import org.aiit.shapemodel.CommunityShape;
import org.aiit.shapemodel.ShapeUtil;

public class MainActivity extends AppCompatActivity {
    public org.aiit.widgets.AreaSelectView areaSelectView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        areaSelectView = (org.aiit.widgets.AreaSelectView) findViewById(R.id.seatView);
        String communityJson = "{\n" +
                "    \"id\":1,\n" +
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
                "            \"id\":2,\n" +
                "            \"name\":\"1号楼\",\n" +
                "            \"bound\":{\n" +
                "                \"left\":30,\n" +
                "                \"top\":100,\n" +
                "                \"right\":80,\n" +
                "                \"bottom\":130\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":3,\n" +
                "            \"name\":\"2号楼\",\n" +
                "            \"bound\":{\n" +
                "                \"left\":130,\n" +
                "                \"top\":120,\n" +
                "                \"right\":200,\n" +
                "                \"bottom\":150\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        CommunityShape community = ShapeUtil.parseCommunity(communityJson);
        areaSelectView.setData(10,15);
        areaSelectView.setRootShape(community);
    }

}
