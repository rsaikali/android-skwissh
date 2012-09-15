package com.saikali.android_skwissh.charts;

import java.util.Arrays;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.preference.PreferenceManager;
import android.util.Log;

import com.saikali.android_skwissh.objects.SkwisshSensorItem;
import com.saikali.android_skwissh.utils.Constants;

public class SensorGraphViewBuilder {

	private int[] colors = new int[] { Color.rgb(127, 174, 0), Color.rgb(234, 162, 40), Color.rgb(213, 56, 59), Color.rgb(78, 165, 181), Color.rgb(127, 174, 0), Color.rgb(234, 162, 40), Color.rgb(213, 56, 59), Color.rgb(78, 165, 181),
			Color.rgb(127, 174, 0), Color.rgb(234, 162, 40), Color.rgb(213, 56, 59), Color.rgb(78, 165, 181), Color.rgb(127, 174, 0), Color.rgb(234, 162, 40), Color.rgb(213, 56, 59), Color.rgb(78, 165, 181) };
	private int fontColor = Color.rgb(77, 77, 77);
	private int lightFontColor = Color.rgb(170, 170, 170);
	private int backgroundColor = Color.rgb(214, 214, 214);
	private String period;
	private SkwisshSensorItem sensor;
	private Context context;

	public SensorGraphViewBuilder(Context context, SkwisshSensorItem sensor) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		this.period = sharedPrefs.getString("default_period", "day");
		this.sensor = sensor;
		this.context = context;
	}

	public GraphicalView createGraphView() {
		if (sensor.getGraphTypeName().equals("pie")) {
			Log.i(Constants.SKWISSH_TAG, "Drawing PIE");
			return this.createPieView();
		} else if (sensor.getGraphTypeName().contains("linegraph")) {
			Log.i(Constants.SKWISSH_TAG, "Drawing LINE");
			return this.createLineView();
		} else if (sensor.getGraphTypeName().contains("bargraph")) {
			Log.i(Constants.SKWISSH_TAG, "Drawing BAR");
			return this.createBarView();
		} else {
			return null;
		}
	}

	public GraphicalView createPieView() {

		// Categories
		CategorySeries categorySeries = new CategorySeries(this.sensor.getDisplayName());

		String[] labels = sensor.getLabels().split(";");
		String[] values = sensor.getMeasures().get(0).getValue().split(";");
		for (int i = 0; i < values.length; i++) {
			categorySeries.add(labels[i], new Double(values[i]));
		}

		// Renderer
		DefaultRenderer renderer = new DefaultRenderer();
		for (int color : Arrays.copyOfRange(colors, 0, values.length)) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		renderer.setApplyBackgroundColor(true);
		renderer.setInScroll(true);
		renderer.setAntialiasing(true);
		renderer.setLabelsTextSize(18);
		renderer.setLegendTextSize(18);
		renderer.setLabelsColor(this.fontColor);
		renderer.setBackgroundColor(this.backgroundColor);
		renderer.setPanEnabled(false);
		renderer.setShowLegend(false);

		return ChartFactory.getPieChartView(context, categorySeries, renderer);
	}

	public GraphicalView createLineView() {

		// Categories
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		String[] labels = sensor.getLabels().split(";");
		int nb_values = sensor.getMeasures().get(0).getValue().split(";").length;
		for (int i = 0; i < nb_values; i++) {
			TimeSeries timeSerie = new TimeSeries(labels[i]);
			for (int j = 0; j < sensor.getMeasures().size(); j++) {
				String value = sensor.getMeasures().get(j).getValue().split(";")[i];
				Date date = sensor.getMeasures().get(j).getTimestamp();
				timeSerie.add(date, new Double(value));
			}
			dataset.addSeries(timeSerie);
		}

		// Renderer
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		for (int color : Arrays.copyOfRange(colors, 0, nb_values)) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color);
			r.setFillBelowLine(true);
			r.setFillBelowLine(false);
			r.setFillPoints(true);
			r.setLineWidth(2);
			renderer.addSeriesRenderer(r);
		}
		renderer.setApplyBackgroundColor(true);
		renderer.setInScroll(true);
		renderer.setAntialiasing(true);
		renderer.setLabelsTextSize(18);
		renderer.setLegendTextSize(18);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(true, false);
		renderer.setLabelsColor(this.fontColor);
		renderer.setMarginsColor(this.backgroundColor);
		renderer.setAxesColor(this.fontColor);

		renderer.setBackgroundColor(this.backgroundColor);
		renderer.setShowLegend(true);
		renderer.setYAxisMin(0);
		renderer.setShowGrid(true);
		renderer.setGridColor(this.lightFontColor);

		renderer.setXLabelsColor(this.fontColor);
		renderer.setYLabelsColor(0, this.fontColor);
		renderer.setYLabelsAlign(Align.RIGHT);

		renderer.setApplyBackgroundColor(true);

		renderer.setMargins(new int[] { 50, 50, 50, 50 });
		renderer.setFitLegend(true);

		String dateFormat = "HH:mm\nMMM dd";
		if (this.period.equals("hour"))
			dateFormat = "HH:mm";

		return ChartFactory.getTimeChartView(context, dataset, renderer, dateFormat);
	}

	public GraphicalView createBarView() {

		// Categories
		XYMultipleSeriesDataset tmp_dataset = new XYMultipleSeriesDataset();

		String[] labels = sensor.getLabels().split(";");
		int nb_values = sensor.getMeasures().get(0).getValue().split(";").length;

		for (int i = 0; i < nb_values; i++) {
			TimeSeries timeSerie = new TimeSeries(labels[i]);
			for (int j = 0; j < sensor.getMeasures().size(); j++) {
				Date date = sensor.getMeasures().get(j).getTimestamp();
				Double value = 0.0;
				for (int k = 0; k <= i; k++) {
					Double old_value = new Double(sensor.getMeasures().get(j).getValue().split(";")[k]);
					value = value + old_value;
				}
				timeSerie.add(date, value);
			}
			tmp_dataset.addSeries(timeSerie);
		}

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		for (int i = tmp_dataset.getSeries().length - 1; i >= 0; i--) {
			dataset.addSeries(tmp_dataset.getSeriesAt(i));
		}

		// Renderer
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		for (int i = Arrays.copyOfRange(this.colors, 0, nb_values).length - 1; i >= 0; i--) {
			int color = Arrays.copyOfRange(this.colors, 0, nb_values)[i];
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color);
			r.setFillBelowLine(true);
			r.setFillBelowLine(false);
			r.setFillPoints(true);
			r.setLineWidth(2);
			renderer.addSeriesRenderer(r);
		}
		renderer.setApplyBackgroundColor(true);
		renderer.setInScroll(true);
		renderer.setBarSpacing(0.5);
		renderer.setAntialiasing(true);
		renderer.setLabelsTextSize(18);
		renderer.setLegendTextSize(18);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(true, false);
		renderer.setLabelsColor(this.fontColor);
		renderer.setMarginsColor(this.backgroundColor);
		renderer.setAxesColor(this.fontColor);

		renderer.setBackgroundColor(this.backgroundColor);
		renderer.setShowLegend(true);
		renderer.setYAxisMin(0);
		renderer.setShowGrid(true);
		renderer.setGridColor(this.lightFontColor);

		renderer.setXLabelsColor(this.fontColor);
		renderer.setYLabelsColor(0, this.fontColor);
		renderer.setYLabelsAlign(Align.RIGHT);

		renderer.setApplyBackgroundColor(true);

		renderer.setMargins(new int[] { 50, 50, 50, 50 });
		renderer.setFitLegend(true);

		return ChartFactory.getBarChartView(context, dataset, renderer, Type.STACKED);
	}
}