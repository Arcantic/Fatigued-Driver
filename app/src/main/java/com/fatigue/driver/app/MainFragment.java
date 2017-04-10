package com.fatigue.driver.app;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by Eric on 11/14/2016.
 */

public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setupGraph(view);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public int dataPoints[] = {10, 10, 10, 9 , 9, 10, 10, 8, 7, 6, 6, 6, 7, 9, 10, 10, 10, 9, 9, 9, 8, 8, 9, 10, 10, 10};

    public void setupGraph(View view){
        GraphView graph = (GraphView) view.findViewById(R.id.graph);
        float scale = 2.5f;

        DataPoint pointList[] = new DataPoint[dataPoints.length];
        for(int i = 0; i < dataPoints.length; i++) {
            pointList[i] = new DataPoint((i*scale)-24*scale, dataPoints[i]);
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(pointList);
        series.setColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
        graph.addSeries(series);

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(-24*scale, 4),
                new DataPoint(24*scale, 4),
        });

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorDrkRed));
        paint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
        series2.setCustomPaint(paint);
        graph.addSeries(series2);



        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(-24*scale);
        graph.getViewport().setMaxX(0);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10);

        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Seconds");
    }

}