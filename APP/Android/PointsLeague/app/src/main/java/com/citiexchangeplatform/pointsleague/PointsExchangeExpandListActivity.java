package com.citiexchangeplatform.pointsleague;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;


import com.citiexchangeplatform.pointsleague.adapter.ExpandableAdapter;
import com.citiexchangeplatform.pointsleague.adapter.VExpandableAdapter;
import com.citiexchangeplatform.pointsleague.data.RecordParent;

import java.util.List;

import static com.citiexchangeplatform.pointsleague.utils.Utils.geneRandomData;
import static com.citiexchangeplatform.pointsleague.utils.Utils.showJson;


/**
 * Created by wanjian on 2018/1/29.
 */
public class PointsExchangeExpandListActivity extends AppCompatActivity {
    List<RecordParent> shopList;
    ExpandableAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expandlist);
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);

        shopList = geneRandomData();
        showJson(shopList);

        vertical(recyclerView);


        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<RecordParent> newData = geneRandomData();
                shopList.clear();

                shopList.addAll(newData);
                showJson(shopList);
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.collapseAllGroup();
            }
        });
        findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.expandAllGroup();
            }
        });

    }


    private void vertical(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new VExpandableAdapter(shopList);
        recyclerView.setAdapter(adapter);

    }


}
