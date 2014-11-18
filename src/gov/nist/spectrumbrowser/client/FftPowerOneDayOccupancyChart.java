package gov.nist.spectrumbrowser.client;

import gov.nist.spectrumbrowser.common.AbstractSpectrumBrowserScreen;
import gov.nist.spectrumbrowser.common.SpectrumBrowserCallback;
import gov.nist.spectrumbrowser.common.SpectrumBrowserScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.Selection;
import com.googlecode.gwt.charts.client.corechart.LineChart;
import com.googlecode.gwt.charts.client.corechart.LineChartOptions;
import com.googlecode.gwt.charts.client.event.SelectEvent;
import com.googlecode.gwt.charts.client.event.SelectHandler;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.VAxis;

public class FftPowerOneDayOccupancyChart extends AbstractSpectrumBrowserScreen
		implements SpectrumBrowserCallback<String> {
	public static final String END_LABEL = "Single Day Occupancy";
	public static String LABEL = END_LABEL + ">>";
	private long mStartTime;
	private VerticalPanel mVerticalPanel;
	private int mWidth;
	private int mHeight;
	private String mSensorId;
	private SpectrumBrowser mSpectrumBrowser;
	private JSONValue jsonValue;
	private HorizontalPanel horizontalPanel;
	private LineChart lineChart;
	private HashMap<Integer, SelectionProperty> selectionProperties = new HashMap<Integer, SelectionProperty>();
	private long mMinFreq;
	private long mMaxFreq;
	private String sys2detect;
	private ArrayList<SpectrumBrowserScreen> navigation;
	private Grid prevNextButtons;

	private static Logger logger = Logger.getLogger("SpectrumBrowser");

	class SelectionProperty {
		long selectionTime;

		SelectionProperty(long time) {
			selectionTime = time;
		}
	}

	public FftPowerOneDayOccupancyChart(SpectrumBrowser spectrumBrowser,
			ArrayList<SpectrumBrowserScreen> navigation, String sensorId,
			long startTime, String sys2detect, long minFreq, long maxFreq,
			VerticalPanel verticalPanel, int width, int height) {

		mStartTime = startTime;
		mSensorId = sensorId;
		mVerticalPanel = verticalPanel;
		mWidth = width;
		mHeight = height;
		mSpectrumBrowser = spectrumBrowser;
		super.setNavigation(verticalPanel, navigation, spectrumBrowser,
				END_LABEL);
		this.navigation = new ArrayList<SpectrumBrowserScreen>(navigation);
		this.navigation.add(this);

		String sessionId = spectrumBrowser.getSessionId();
		this.sys2detect = sys2detect;
		mMinFreq = minFreq;
		mMaxFreq = maxFreq;
		mSpectrumBrowser.getSpectrumBrowserService().getOneDayStats(sessionId,
				sensorId, startTime, sys2detect, minFreq, maxFreq, this);

	}

	@Override
	public void onSuccess(String result) {
		try {
			jsonValue = JSONParser.parseLenient(result);
			draw();
		} catch (Throwable th) {
			logger.log(Level.SEVERE, "Error in parsing result", th);
		}
	}

	@Override
	public void onFailure(Throwable throwable) {
		logger.log(Level.SEVERE, "Error in contacting server", throwable);
		mSpectrumBrowser.displayError("Problem contacting server");

	}


	public String getLabel() {
		return LABEL;
	}

	public String getEndLabel() {
		return END_LABEL;
	}

	public void draw() {
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);

		chartLoader.loadApi(new Runnable() {

			@Override
			public void run() {
				mVerticalPanel.clear();
				horizontalPanel = new HorizontalPanel();
				horizontalPanel.setWidth(mWidth + "px");
				horizontalPanel.setHeight(mHeight + "px");

				lineChart = new LineChart();
				horizontalPanel.add(lineChart);
				drawNavigation();
				String dateString = jsonValue.isObject().get("formattedDate")
						.isString().stringValue();
				HTML heading = new HTML("<h2>" + END_LABEL + "</h2>");
				mVerticalPanel.add(heading);
				int minFreq = (int) ((mMinFreq + 500000) / 1E6);
				int maxFreq = (int) ((mMaxFreq + 500000) / 1E6);
				int nChannels = (int) jsonValue.isObject().get("channelCount")
						.isNumber().doubleValue();
				int acquisitionCount = (int) jsonValue.isObject()
						.get("acquisitionCount").isNumber().doubleValue();
				int measurementsPerAcquisition = (int) jsonValue.isObject()
						.get("measurementsPerAcquisition").isNumber()
						.doubleValue();
				int cutoff = (int) jsonValue.isObject().get("cutoff")
						.isNumber().doubleValue();
				HTML infoTitle = new HTML("<h3> Start Time = " + dateString
						+ ";Detected System = " + sys2detect + "; minFreq = "
						+ minFreq + " MHz; maxFreq = " + maxFreq + " MHz"
						+ "; nChannels = " + nChannels
						+ "; Occupancy cutoff = " + cutoff + " dBm </h3>");
				mVerticalPanel.add(infoTitle);
				HTML infoTitle1 = new HTML(
						"<h3>Measurements Per Acquisition = "
								+ measurementsPerAcquisition
								+ "; Acquisition Count = " + acquisitionCount
								+ "</h3>");
				mVerticalPanel.add(infoTitle1);
				prevNextButtons = new Grid(1, 2);
				mVerticalPanel.add(prevNextButtons);

				mVerticalPanel.add(horizontalPanel);

				final long currentStartTime = (long) jsonValue.isObject()
						.get("currentIntervalStart").isNumber().doubleValue();
				final long prevStartTime = (long) jsonValue.isObject()
						.get("prevIntervalStart").isNumber().doubleValue();
				final long nextStartTime = (long) jsonValue.isObject()
						.get("nextIntervalStart").isNumber().doubleValue();
				if (prevStartTime < currentStartTime) {
					Button prevIntervalButton = new Button("<< Previous Day");
					prevIntervalButton.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							mSpectrumBrowser.getSpectrumBrowserService().getOneDayStats(mSpectrumBrowser.getSessionId(),
									mSensorId, prevStartTime, sys2detect, mMinFreq, mMaxFreq, FftPowerOneDayOccupancyChart.this);
						}
					});
					prevNextButtons.setWidget(0, 0, prevIntervalButton);
				}
				
				if (nextStartTime > currentStartTime) {
					Button nextIntervalButton = new Button("Next Day >>");
					nextIntervalButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							mSpectrumBrowser.getSpectrumBrowserService().getOneDayStats(mSpectrumBrowser.getSessionId(),
									mSensorId, nextStartTime, sys2detect, mMinFreq, mMaxFreq, FftPowerOneDayOccupancyChart.this);
						}
					});
					prevNextButtons.setWidget(0, 1, nextIntervalButton);
				}
				

				DataTable dataTable = DataTable.create();
				dataTable.addColumn(ColumnType.NUMBER,
						" Hours since start of day.");
				dataTable.addColumn(ColumnType.NUMBER, " Max");
				dataTable.addColumn(ColumnType.NUMBER, " Min");
				dataTable.addColumn(ColumnType.NUMBER, " Median");
				dataTable.addColumn(ColumnType.NUMBER, " Mean");

				JSONObject jsonObject = jsonValue.isObject().get("values")
						.isObject();
				int rowCount = jsonObject.size();
				logger.finer("rowCount " + rowCount);
				dataTable.addRows(rowCount);
				try {
					int rowIndex = 0;
					for (String secondString : jsonObject.keySet()) {
						JSONObject statsObject = jsonObject.get(secondString)
								.isObject();
						int second = Integer.parseInt(secondString);
						long time = (long) statsObject.get("t").isNumber()
								.doubleValue();
						double mean = statsObject.get("meanOccupancy")
								.isNumber().doubleValue() * 100;
						double max = statsObject.get("maxOccupancy").isNumber()
								.doubleValue() * 100;
						double min = statsObject.get("minOccupancy").isNumber()
								.doubleValue() * 100;
						double median = statsObject.get("medianOccupancy")
								.isNumber().doubleValue() * 100;
						float hours = round((double) second / (double) 3600);
						dataTable.setCell(rowIndex, 0, hours, hours
								+ " hours since start of day.");
						dataTable.setCell(rowIndex, 1, round(max), round(max)
								+ "%");
						dataTable.setCell(rowIndex, 2, round(min), round(min)
								+ "%");
						dataTable.setCell(rowIndex, 3, round(median),
								round(median) + "%");
						dataTable.setCell(rowIndex, 4, round(mean), round(mean)
								+ "%");
						selectionProperties.put(rowIndex,
								new SelectionProperty(time));
						rowIndex++;
					}

					lineChart.addSelectHandler(new SelectHandler() {

						@Override
						public void onSelect(SelectEvent event) {
							JsArray<Selection> selections = lineChart
									.getSelection();
							int length = selections.length();
							for (int i = 0; i < length; i++) {
								int row = selections.get(i).getRow();
								SelectionProperty property = selectionProperties
										.get(row);

								new FftPowerOneAcquisitionSpectrogramChart(
										mSensorId, property.selectionTime,
										sys2detect, mMinFreq, mMaxFreq,
										mVerticalPanel, mSpectrumBrowser,
										navigation, mWidth, mHeight);
							}
						}
					});
					LineChartOptions options = LineChartOptions.create();
					options.setBackgroundColor("#f0f0f0");
					options.setPointSize(5);
					lineChart.setHeight(mHeight + "px");
					lineChart.setWidth(mWidth + "px");
					options.setHAxis(HAxis.create("Hours since start of day."));
					options.setVAxis(VAxis.create("Occupancy %"));
					lineChart.setStyleName("lineChart");
					lineChart.draw(dataTable, options);
					lineChart.setVisible(true);

					horizontalPanel.add(lineChart);
				} catch (Throwable throwable) {
					logger.log(Level.SEVERE, "Problem parsing json ", throwable);
				}
			}
		});

	}

}
