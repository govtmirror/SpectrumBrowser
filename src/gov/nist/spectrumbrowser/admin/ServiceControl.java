/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others. 
* This software has been contributed to the public domain. 
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain. 
* As a result, a formal license is not needed to use this software.
* 
* This software is provided "AS IS."  
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* this software.
*/
package gov.nist.spectrumbrowser.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;

import gov.nist.spectrumbrowser.common.AbstractSpectrumBrowserWidget;
import gov.nist.spectrumbrowser.common.Defines;
import gov.nist.spectrumbrowser.common.SpectrumBrowserCallback;
import gov.nist.spectrumbrowser.common.SpectrumBrowserScreen;

public class ServiceControl extends AbstractSpectrumBrowserWidget implements
		SpectrumBrowserScreen, SpectrumBrowserCallback<String> {

	private HorizontalPanel titlePanel;
	private Grid grid;
	HTML html;

	private Map<String, TextBox> statusBoxMap;
	private Map<String, Button> stopButtonMap;
	private Map<String, Button> restartButtonMap;

	private static String[] SERVICE_NAMES = Defines.SERVICE_NAMES;
	private static int NUM_SERVICES = Defines.SERVICE_NAMES.length;
	private static int STATUS_CHECK_TIME_SEC = 5;

	private Admin admin;
	private static Logger logger = Logger.getLogger("SpectrumBrowser");

	private static final String END_LABEL = "Service Control";

	public ServiceControl(Admin admin) {
		super();
		try {
			this.admin = admin;
			titlePanel = new HorizontalPanel();
			statusBoxMap = new HashMap<String, TextBox>();
			stopButtonMap = new HashMap<String, Button>();
			restartButtonMap = new HashMap<String, Button>();

			for (String serviceName : SERVICE_NAMES) {
				statusBoxMap.put(serviceName, new TextBox());
				stopButtonMap.put(serviceName, new Button());
				restartButtonMap.put(serviceName, new Button());
			}

			setStatusBox();

		} catch (Throwable th) {
			logger.log(
					Level.SEVERE,
					"Problem contacting server -- check servicecontrol servicce",
					th);
		}
	}

	private void drawMenuItems() {
		HTML title;
		title = new HTML(
				"<h3> Service Status</h3>");
		titlePanel.add(title);
		HTML help = new HTML("<p>Use the stop button to stop the service. "
				+ "Use restart to restart it.</p>");
		titlePanel.add(help);
		verticalPanel.add(titlePanel);
	}

	@Override
	public void draw() {
		try {
			verticalPanel.clear();
			titlePanel.clear();
			drawMenuItems();

			grid = new Grid(NUM_SERVICES + 1, 4);
			grid.setCellSpacing(4);
			grid.setBorderWidth(2);
			verticalPanel.add(grid);

			grid.setText(0, 0, "Service");
			grid.setText(0, 1, "Status");
			grid.setText(0, 2, "Stop");
			grid.setText(0, 3, "Restart");

			for (int i = 0; i < NUM_SERVICES; i++) {
				grid.setText(i + 1, 0, SERVICE_NAMES[i]);
				grid.setWidget(i + 1, 1, statusBoxMap.get(SERVICE_NAMES[i]));

				if (!SERVICE_NAMES[i].equals("servicecontrol")
						&& !SERVICE_NAMES[i].equals("admin")) {
					createStopButton(i);
					createRestartButton(i);
					grid.setWidget(i + 1, 2,
							stopButtonMap.get(SERVICE_NAMES[i]));
					grid.setWidget(i + 1, 3,
							restartButtonMap.get(SERVICE_NAMES[i]));
				} else if (SERVICE_NAMES[i].equals("servicecontrol") || SERVICE_NAMES[i].equals("admin")) {
					grid.setText(i + 1, 2, "N/A");
					grid.setText(i + 1, 3, "N/A");
				} 

			}

		} catch (Throwable th) {
			logger.log(Level.SEVERE, "ERROR drawing service control screen", th);
		}
	}

	private void setStatusBox() {

		final Timer timer = new Timer() {
			@Override
			public void run() {
				Admin.getAdminService().getServicesStatus(
						new SpectrumBrowserCallback<String>() {
							@Override
							public void onSuccess(String result) {
								try {
									JSONObject jsonObj = JSONParser
											.parseLenient(result).isObject();
									JSONObject status = jsonObj.get(
											"serviceStatus").isObject();

									for (String serviceName : SERVICE_NAMES) {
										String serviceStatus = status
												.get(serviceName).isString()
												.stringValue();
										statusBoxMap.get(serviceName).setText(
												serviceStatus);
										if (stopButtonMap.get(serviceName) != null)
											stopButtonMap.get(serviceName).setEnabled(true);
										if(restartButtonMap.get(serviceName) != null) {
											restartButtonMap.get(serviceName)
												.setEnabled(true);
										}
									}
								} catch (Throwable th) {
									logger.log(Level.SEVERE,
											"Error parsing json ", th);
									admin.logoff();
								}

							}

							@Override
							public void onFailure(Throwable throwable) {
								// Window.alert("Error communicating with server -- check servicecontrol service status.");
								cancel();
								admin.logoff();
							}
						});

			}
		};
		timer.scheduleRepeating(STATUS_CHECK_TIME_SEC * 1000);
	}

	private void createStopButton(final int i) {
		final String serviceName = SERVICE_NAMES[i];

		stopButtonMap.get(serviceName).removeStyleName("gwt-Button");
		stopButtonMap.get(serviceName).getElement().getStyle()
				.setBackgroundColor("red");
		stopButtonMap.get(serviceName).setHeight("50px");
		stopButtonMap.get(serviceName).setWidth("50px");
		stopButtonMap.get(serviceName).addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Admin.getAdminService().stopService(SERVICE_NAMES[i],
						new SpectrumBrowserCallback<String>() {

							@Override
							public void onSuccess(String result) {
								JSONObject jsonObj = JSONParser.parseLenient(
										result).isObject();
								if (!(jsonObj.get("status").isString()
										.stringValue().equals("OK"))) {
									String errorMessage = jsonObj
											.get("ErrorMessage").isString()
											.stringValue();
									Window.alert("Error stopping service. Please refresh. Error Message : "
											+ errorMessage);
								}
							}

							@Override
							public void onFailure(Throwable throwable) {
								Window.alert("Error communicating with server");
								admin.logoff();
							}
						});
				stopButtonMap.get(serviceName).setEnabled(false);
			}
		});
	}

	private void createRestartButton(final int i) {
		final String serviceName = SERVICE_NAMES[i];

		restartButtonMap.get(serviceName).removeStyleName("gwt-Button");
		restartButtonMap.get(serviceName).getElement().getStyle()
				.setBackgroundColor("green");
		restartButtonMap.get(serviceName).setHeight("50px");
		restartButtonMap.get(serviceName).setWidth("50px");
		restartButtonMap.get(serviceName).addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Admin.getAdminService().restartService(SERVICE_NAMES[i],
						new SpectrumBrowserCallback<String>() {

							@Override
							public void onSuccess(String result) {
								JSONObject jsonObj = JSONParser.parseLenient(
										result).isObject();
								if (!(jsonObj.get("status").isString()
										.stringValue().equals("OK"))) {
									String errorMessage = jsonObj
											.get("ErrorMessage").isString()
											.stringValue();
									Window.alert("Error restarting service. Please refresh. Error Message : "
											+ errorMessage);
								}
							}

							@Override
							public void onFailure(Throwable throwable) {
								Window.alert("Error communicating with server");
								admin.logoff();
							}
						});
				restartButtonMap.get(serviceName).setEnabled(false);
			}
		});
	}

	@Override
	public String getLabel() {
		return END_LABEL + " >>";
	}

	@Override
	public String getEndLabel() {
		return END_LABEL;
	}

	@Override
	public void onSuccess(String jsonString) {
	}

	@Override
	public void onFailure(Throwable throwable) {
		logger.log(Level.SEVERE, "Error Communicating with server", throwable);
		admin.logoff();
	}

	@Override
	public String toString() {
		return null;
	}

}
