package com.demo.chart;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ChartView mChartView = (ChartView) findViewById(R.id.chart_view);
		
		mChartView.setDataSource(new float[]{1.2f, 0.3f, 5f, 4, 3.1f, 10, 7 });
	}
}
