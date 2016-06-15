package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class StockDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CURSER_LOADER_ID = 0;
    private Cursor mCursor;
    private LineChartView lineChartView;
    private LineSet lineSet;
    int min, max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        lineSet = new LineSet();
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        Intent in = getIntent();
        Bundle args = new Bundle();
        args.putString("symbol", in.getStringExtra("symbol"));
        getLoaderManager().initLoader(CURSER_LOADER_ID, args, this);
    }

    private void initgraph() {
        lineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(Color.parseColor("#6a84c3"))
                .setXAxis(true)
                .setYAxis(true)
                .setAxisBorderValues(Math.round(Math.max(0f, min - 5f)), Math.round(max + 5f));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + "= ?",
                new String[]{args.getString("symbol")},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        findMinMax(mCursor);
        initgraph();
        plotGraph();
    }

    private void plotGraph() {
        mCursor.moveToFirst();
        for (int i = 0; i < mCursor.getCount(); i++){
            float stock_price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            lineSet.addPoint("" , stock_price);
            mCursor.moveToNext();
        }
        lineSet.setColor(Color.parseColor("#758cbb"))
                .setFill(Color.parseColor("#2d374c"))
                .setThickness(1);

        lineChartView.addData(lineSet);
        lineChartView.show();


    }

    private void findMinMax(Cursor mCursor) {
        ArrayList<Float> list = new ArrayList<Float>();
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext() )
        {
            list.add(Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE))));
        }

        min = Math.round(Collections.min(list));
        max = Math.round(Collections.max(list));
        Log.e("min = " + min,"max = "+ max);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
