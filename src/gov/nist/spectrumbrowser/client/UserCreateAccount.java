package gov.nist.spectrumbrowser.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nist.spectrumbrowser.client.SpectrumBrowser;
import gov.nist.spectrumbrowser.common.AbstractSpectrumBrowser;
import gov.nist.spectrumbrowser.common.SpectrumBrowserCallback;
import gov.nist.spectrumbrowser.common.SpectrumBrowserScreen;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Screen for user to create a new login account
 * @author Julie Kub
 *
 */
public class UserCreateAccount implements SpectrumBrowserCallback<String> , SpectrumBrowserScreen {
	
	private VerticalPanel verticalPanel;
	private SpectrumBrowser spectrumBrowser;
	private PasswordTextBox passwordEntry;
	private PasswordTextBox passwordEntryConfirm;
	private TextBox emailEntry;
	private TextBox lastNameEntry;
	private TextBox firstNameEntry;
	private final SpectrumBrowserScreen loginScreen;
	private static Logger logger = Logger.getLogger("SpectrumBrowser");
	public static final String LABEL = "Create Account";
	
	
	private static boolean enablePasswordChecking = true;
	
	
	
	public UserCreateAccount(
			VerticalPanel verticalPanel, SpectrumBrowser spectrumBrowser, SpectrumBrowserScreen loginScreen) {
		logger.finer("UserCreateAccount");
		this.verticalPanel = verticalPanel;
		this.spectrumBrowser = spectrumBrowser;
		this.loginScreen = loginScreen;
				
	}
	
	class SubmitNewAccount implements ClickHandler {


			@Override
			public void onClick(ClickEvent event) {
				String firstName = firstNameEntry.getValue().trim();
				String lastName = lastNameEntry.getValue().trim();
				String password = passwordEntry.getValue();
				String passwordConfirm = passwordEntryConfirm.getValue();
				String emailAddress = emailEntry.getValue().trim();
				logger.finer("SubmitNewAccount: " + emailAddress);
				if (firstName == null || firstName.length() == 0) {
					Window.alert("First Name with at least one character is required.");
					return;
				}
				if (lastName == null || lastName.length() == 0) {
					Window.alert("Last Name with at least one character is required.");
					return;
				}

				if (emailAddress == null || emailAddress.length() == 0) {
					Window.alert("Email is required.");
					return;
				}
				//TODO: JEK: look at http://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method
				// Better to use apache email validator than to use RegEx:
				if (!emailAddress 
						.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")) {
					Window.alert("Please enter a valid email address.");
					return;
				}
				if (password == null || password.length() == 0) {
					Window.alert("Password is required.");
					return;
				}
				if (passwordConfirm == null || passwordConfirm.length() == 0) {
					Window.alert("Re-typed password is required.");
					return;
				}
				if (password != passwordConfirm)
				{
					Window.alert("Password entries must match.");
					return;					
				}
				/* Sadly, this password policy will drive anybody to tears and reduce the 
				 * value of the system. However, it is what it is:
				 * The password policy is:			
				 * At least 14 chars					
				 * Contains at least one digit					
				 * Contains at least one lower alpha char and one upper alpha char					
				 * Contains at least one char within a set of special chars (@#%$^ etc.)					
				 * Does not contain space, tab, etc. Yeah. Osama bin Laden made us do it! 
				 *
					^                 # start-of-string
					(?=.*[0-9])       # a digit must occur at least once
					(?=.*[a-z])       # a lower case letter must occur at least once
					(?=.*[A-Z])       # an upper case letter must occur at least once
					(?=.*[!@#$%^&+=])  # a special character must occur at least once
					.{12,}             # anything, at least 12 digits
					$                 # end-of-string
				 */

				// Password policy check disabled for debugging. Enable this for production.
				if (enablePasswordChecking && !password 
						.matches("((?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])).{12,}$")) {
					Window.alert("Please enter a password with 1) at least 12 characters, 2) a digit, 3) an upper case letter, 4) a lower case letter, and 5) a special character(!@#$%^&+=).");
					return;
				}	
				/*
				if (emailAddress.matches("(.*(\\.gov|\\.mil|\\.GOV|\\.MIL)+)$")){
					//TODO: JEK: if .gov or .mil, automatically create account
					spectrumBrowser.getSpectrumBrowserService().requestNewAccount(firstName,lastName, emailAddress,
							password,AbstractSpectrumBrowser.getBaseUrlAuthority(),UserCreateAccount.this);
					Window.alert("Please check your email for notification");

					return;

				}
				else {
					//TODO: JEK: if not .gov/.mil, email admin to approve/deny account creation
					Window.alert("Your request has been forwarded to admin. Check your mail for notification.");
				}

				*/
				spectrumBrowser.getSpectrumBrowserService().requestNewAccount(firstName,lastName, emailAddress,
						password,AbstractSpectrumBrowser.getBaseUrlAuthority(),UserCreateAccount.this);
				verticalPanel.clear();
				// JEK: the following line would have worked, but instead we passed in loginScreen variable:
				//new LoginScreen(spectrumBrowser).draw();
				loginScreen.draw();
					
				
			};
		

	}


	public void draw() {
		

		verticalPanel.clear();
		HTML title = new HTML("<h1>Department of Commerce Spectrum Monitor</h1><h2>Create Account </h2>");
		verticalPanel.add(title);
	
		
		if (!enablePasswordChecking) {
			HTML warning = new HTML("<h3>Debug Mode: password restrictions are off!</h3>");
			verticalPanel.add(warning);
		}
		HorizontalPanel firstNameField = new HorizontalPanel();
		Label firstNameLabel = new Label("First Name");
		firstNameLabel.setWidth("150px");
		firstNameField.add(firstNameLabel);
		firstNameEntry = new TextBox();
		firstNameEntry.setWidth("150px");
		firstNameEntry.setText("");
		firstNameField.add(firstNameEntry);
		verticalPanel.add(firstNameField);
		
		HorizontalPanel lastNameField = new HorizontalPanel();
		Label lastNameLabel = new Label("Last Name");
		lastNameLabel.setWidth("150px");
		lastNameField.add(lastNameLabel);
		lastNameEntry = new TextBox();
		lastNameEntry.setWidth("150px");
		lastNameEntry.setText("");
		lastNameField.add(lastNameEntry);
		verticalPanel.add(lastNameField);
		
		HorizontalPanel emailField = new HorizontalPanel();
		Label emailLabel = new Label("Email Address");
		emailLabel.setWidth("150px");
		emailField.add(emailLabel);
		emailEntry = new TextBox();
		emailEntry.setWidth("150px");
		emailEntry.setText("");
		emailField.add(emailEntry);
		verticalPanel.add(emailField);
		
		HorizontalPanel passwordField = new HorizontalPanel();
		Label passwordLabel = new Label("Choose a Password (at least 12 chars, uppercase, lowercase, numeric, and special character(!@#$%^&+=))");
		passwordLabel.setWidth("150px");
		passwordField.add(passwordLabel);
		passwordEntry = new PasswordTextBox();
		passwordEntry.setWidth("150px");
		passwordEntry.setText("");
		passwordField.add(passwordEntry);
		verticalPanel.add(passwordField);
		
		HorizontalPanel passwordFieldConfirm = new HorizontalPanel();
		Label passwordLabelConfirm = new Label("Re-type password");
		passwordLabelConfirm.setWidth("150px");
		passwordFieldConfirm.add(passwordLabelConfirm);
		passwordEntryConfirm = new PasswordTextBox();
		passwordEntryConfirm.setWidth("150px");
		passwordEntryConfirm.setText("");
		passwordFieldConfirm.add(passwordEntryConfirm);
		verticalPanel.add(passwordFieldConfirm);
		
		Grid buttonGrid = new Grid(1,2);
		verticalPanel.add(buttonGrid);

		Button buttonNewAccount = new Button("Submit");

		buttonNewAccount.addStyleName("sendButton");
		buttonGrid.setWidget(0,0,buttonNewAccount);
		buttonNewAccount.addClickHandler( new SubmitNewAccount());
		
		Button cancelButton = new Button("Cancel");
		buttonGrid.setWidget(0, 1, cancelButton);
		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				verticalPanel.clear();
				UserCreateAccount.this.loginScreen.draw();
			}});
	}

	@Override
	public void onSuccess(String result) {
		JSONObject jsonObject = JSONParser.parseLenient(result).isObject();
		String status = jsonObject.get("status").isString().stringValue();
		if ( status.equals("OK")) {
			Window.alert("Your request has been submitted and approved - please check your email to activate your account.");
		} 
		else if ( status.equals("EXISTING")) {
			Window.alert("An account already exists for this email address. Please contact the web administrator.");	
		}
		else if ( status.equals("INVALPASS")) {
			Window.alert("Your new password is invalid.");	
		}
		else if ( status.equals("INVALFNAME")) {
			Window.alert("Your first name is invalid.");	
		}
		else if ( status.equals("INVALLNAME")) {
			Window.alert("Your last name is invalid.");	
		}
		else if (status.equals("FORWARDED")) {
			Window.alert("Your request has been forwarded for approval. Please check your email within 24 hours for further action.");
		} 
		else if (status.equals("PENDING")) {
			Window.alert("A request for a new account with this email address is already pending.");
		} 
		else {
			Window.alert("There was an issue creating your account. Please contact the web administrator.");
		}
		
	}

	@Override
	public void onFailure(Throwable throwable) {
		logger.log(Level.SEVERE, "Error occured when contacting server in UserCreateAccount",throwable);
		Window.alert("Error occured contacting server in UserCreateAccount.");
		
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getEndLabel() {
		// TODO Auto-generated method stub
		return null;
	}
}
