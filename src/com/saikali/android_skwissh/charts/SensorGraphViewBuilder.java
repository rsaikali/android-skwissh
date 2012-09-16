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
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.saikali.android_skwissh.objects.SkwisshSensorItem;

public class SensorGraphViewBuilder {

	private int[] colors = new int[] { Color.rgb(127, 174, 0), Color.rgb(234, 162, 40), Color.rgb(213, 56, 59), Color.rgb(78, 165, 181), Color.rgb(127, 174, 0), Color.rgb(234, 162, 40), Color.rgb(213, 56, 59), Color.rgb(78, 165, 181),
			Color.rgb(127, 174, 0), Color.rgb(234, 162, 40), Color.rgb(213, 56, 59), Color.rgb(78, 165, 181), Color.rgb(127, 174, 0), Color.rgb(234, 162, 40), Color.rgb(213, 56, 59), Color.rgb(78, 165, 181) };
	private int fontColor = Color.rgb(77, 77, 77);
	private int lightFontColor = Color.rgb(170, 170, 170);
	private int backgroundColor = Color.rgb(242, 242, 242);
	private String period;
	private SkwisshSensorItem sensor;
	private Context context;
	private int[] margins = new int[] { 50, 50, 50, 20 };

	public SensorGraphViewBuilder(Context context, SkwisshSensorItem sensor) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		this.period = sharedPrefs.getString("default_period", "day");
		this.sensor = sensor;
		this.context = context;
	}

	public View createGraphView() {
		if (this.sensor.getGraphTypeName().equals("pie"))
			return this.createPieView();
		else if (this.sensor.getGraphTypeName().equals("linegraph"))
			return this.createLineView();
		else if (this.sensor.getGraphTypeName().equals("linegraph_stacked"))
			return this.createLineStackedView();
		else if (this.sensor.getGraphTypeName().equals("bargraph"))
			return this.createBarView();
		else if (this.sensor.getGraphTypeName().equals("bargraph_groups"))
			return this.createBarGroupView();
		else if (this.sensor.getGraphTypeName().equals("text"))
			return this.createTextView();
		return null;

	}

	private XYMultipleSeriesRenderer getDefaultRenderer() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setApplyBackgroundColor(true);
		renderer.setInScroll(true);
		renderer.setAntialiasing(true);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPanEnabled(false);
		renderer.setShowLegend(false);
		renderer.setShowGrid(true);
		renderer.setMargins(this.margins);
		renderer.setFitLegend(true);
		renderer.setAxesColor(this.fontColor);
		renderer.setLabelsColor(this.fontColor);
		renderer.setBackgroundColor(this.backgroundColor);
		return renderer;
	}

	private GraphicalView createPieView() {

		// Categories
		CategorySeries categorySeries = new CategorySeries(this.sensor.getDisplayName());

		String[] labels = this.sensor.getLabels().split(";");
		String[] values = this.sensor.getMeasures().get(0).getValue().split(";");
		for (int i = 0; i < values.length; i++) {
			categorySeries.add(labels[i], new Double(values[i]));
		}

		// Renderer
		DefaultRenderer renderer = this.getDefaultRenderer();
		for (int color : Arrays.copyOfRange(this.colors, 0, values.length)) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}

		return ChartFactory.getPieChartView(this.context, categorySeries, renderer);
	}

	private GraphicalView createLineView() {

		// Categories
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		String[] labels = this.sensor.getLabels().split(";");
		int nb_values = this.sensor.getMeasures().get(0).getValue().split(";").length;
		for (int i = 0; i < nb_values; i++) {
			TimeSeries timeSerie = new TimeSeries(labels[i]);
			for (int j = 0; j < this.sensor.getMeasures().size(); j++) {
				Double value = new Double(this.sensor.getMeasures().get(j).getValue().split(";")[i]);
				Date date = this.sensor.getMeasures().get(j).getTimestamp();
				timeSerie.add(date, value);
			}
			dataset.addSeries(timeSerie);
		}

		// Renderer
		XYMultipleSeriesRenderer renderer = this.getDefaultRenderer();
		for (int color : Arrays.copyOfRange(this.colors, 0, nb_values)) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color);
			r.setFillBelowLine(true);
			r.setFillBelowLine(false);
			r.setFillPoints(true);
			r.setLineWidth(2);
			renderer.addSeriesRenderer(r);
		}

		renderer.setShowLegend(true);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(true, false);

		renderer.setGridColor(this.lightFontColor);
		renderer.setMarginsColor(this.backgroundColor);

		renderer.setXLabelsColor(this.fontColor);
		renderer.setYLabelsColor(0, this.fontColor);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setYAxisMin(0);

		String dateFormat = "HH:mm\nMMM dd";
		if (this.period.equals("hour")) {
			dateFormat = "HH:mm";
		}

		return ChartFactory.getTimeChartView(this.context, dataset, renderer, dateFormat);
	}

	private GraphicalView createLineStackedView() {

		// Categories
		XYMultipleSeriesDataset tmp_dataset = new XYMultipleSeriesDataset();

		String[] labels = this.sensor.getLabels().split(";");
		int nb_values = this.sensor.getMeasures().get(0).getValue().split(";").length;

		for (int i = 0; i < nb_values; i++) {
			TimeSeries timeSerie = new TimeSeries(labels[i]);
			for (int j = 0; j < this.sensor.getMeasures().size(); j++) {
				Date date = this.sensor.getMeasures().get(j).getTimestamp();
				Double value = 0.0;
				for (int k = 0; k <= i; k++) {
					Double old_value = new Double(this.sensor.getMeasures().get(j).getValue().split(";")[k]);
					value += old_value;
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
		XYMultipleSeriesRenderer renderer = this.getDefaultRenderer();

		for (int i = Arrays.copyOfRange(this.colors, 0, nb_values).length - 1; i >= 0; i--) {
			int color = Arrays.copyOfRange(this.colors, 0, nb_values)[i];
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color);
			r.setFillBelowLineColor(color);
			r.setFillBelowLine(true);
			r.setLineWidth(2);
			renderer.addSeriesRenderer(r);
		}

		renderer.setShowLegend(true);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(true, false);

		renderer.setGridColor(this.lightFontColor);
		renderer.setMarginsColor(this.backgroundColor);

		renderer.setXLabelsColor(this.fontColor);
		renderer.setYLabelsColor(0, this.fontColor);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setYAxisMin(0);

		renderer.setBarSpacing(0);

		return ChartFactory.getLineChartView(this.context, dataset, renderer);
	}

	private GraphicalView createBarView() {

		// Categories
		XYMultipleSeriesDataset tmp_dataset = new XYMultipleSeriesDataset();

		String[] labels = this.sensor.getLabels().split(";");
		int nb_values = this.sensor.getMeasures().get(0).getValue().split(";").length;

		for (int i = 0; i < nb_values; i++) {
			TimeSeries timeSerie = new TimeSeries(labels[i]);
			for (int j = 0; j < this.sensor.getMeasures().size(); j++) {
				Date date = this.sensor.getMeasures().get(j).getTimestamp();
				Double value = 0.0;
				for (int k = 0; k <= i; k++) {
					Double old_value = new Double(this.sensor.getMeasures().get(j).getValue().split(";")[k]);
					value += old_value;
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
		XYMultipleSeriesRenderer renderer = this.getDefaultRenderer();

		for (int i = Arrays.copyOfRange(this.colors, 0, nb_values).length - 1; i >= 0; i--) {
			int color = Arrays.copyOfRange(this.colors, 0, nb_values)[i];
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}

		renderer.setShowLegend(true);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(true, false);

		renderer.setGridColor(this.lightFontColor);
		renderer.setMarginsColor(this.backgroundColor);

		renderer.setXLabelsColor(this.fontColor);
		renderer.setYLabelsColor(0, this.fontColor);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setYAxisMin(0);

		renderer.setBarSpacing(0.2);

		return ChartFactory.getBarChartView(this.context, dataset, renderer, Type.STACKED);
	}

	private GraphicalView createBarGroupView() {

		// Categories
		XYMultipleSeriesDataset tmp_dataset = new XYMultipleSeriesDataset();

		String[] labels = this.sensor.getLabels().split(";");
		int nb_values = this.sensor.getMeasures().get(0).getValue().split(";").length;

		for (int i = 0; i < nb_values; i++) {
			TimeSeries timeSerie = new TimeSeries(labels[i]);
			for (int j = 0; j < this.sensor.getMeasures().size(); j++) {
				Date date = this.sensor.getMeasures().get(j).getTimestamp();
				Double value = new Double(this.sensor.getMeasures().get(j).getValue().split(";")[i]);
				timeSerie.add(date, value);
			}
			tmp_dataset.addSeries(timeSerie);
		}

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		for (int i = tmp_dataset.getSeries().length - 1; i >= 0; i--) {
			dataset.addSeries(tmp_dataset.getSeriesAt(i));
		}

		// Renderer
		XYMultipleSeriesRenderer renderer = this.getDefaultRenderer();

		for (int i = Arrays.copyOfRange(this.colors, 0, nb_values).length - 1; i >= 0; i--) {
			int color = Arrays.copyOfRange(this.colors, 0, nb_values)[i];
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		renderer.setShowLegend(true);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(true, false);

		renderer.setGridColor(this.lightFontColor);
		renderer.setMarginsColor(this.backgroundColor);

		renderer.setXLabelsColor(this.fontColor);
		renderer.setYLabelsColor(0, this.fontColor);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setYAxisMin(0);

		renderer.setBarSpacing(0.2);

		return ChartFactory.getBarChartView(this.context, dataset, renderer, Type.DEFAULT);
	}

	@SuppressWarnings("deprecation")
	private View createTextView() {
		final TextView tv = new TextView(this.context);
		String str = this.sensor.getMeasures().get(0).getValue();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(params);
		tv.setPadding(10, 10, 10, 10);
		tv.setBackgroundColor(Color.parseColor("#303030"));
		tv.setTextColor(Color.parseColor("#7FAE00"));
		tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
		tv.setText(str);
		tv.setTextSize(16);
		return tv;
	}
}
