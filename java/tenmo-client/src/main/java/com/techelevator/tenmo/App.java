package com.techelevator.tenmo;

import com.techelevator.tenmo.models.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.TenmoService;
import com.techelevator.view.ConsoleService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private TenmoService ts = new TenmoService(API_BASE_URL);

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		console.printBalance(ts.getAccountByUserId(currentUser.getUser().getId(), currentUser));
	}

	private void viewTransferHistory() {
    	Account[] accounts = ts.getAllAccounts(currentUser);
		User[] users = ts.getAllUsers(currentUser);
		console.viewTransfers(ts.getTransfers(currentUser.getUser().getId(), currentUser), accounts, users, currentUser.getUser());
	}

	private void viewPendingRequests() {
		Account[] accounts = ts.getAllAccounts(currentUser);
		User[] users = ts.getAllUsers(currentUser);
		int[] results = null;
		results = console.viewPendingTransfers(ts.getPendingTransfers(currentUser.getUser().getId(), currentUser), accounts, users, currentUser.getUser());
		if (results[0] != 0) {
			ts.sendApprovalOrReject(results[0], results[1], currentUser);
		}
	}

	private void sendBucks() {
		console.printAllUsers(ts.getAllUsers(currentUser));
		System.out.println();
		Integer toUserId = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)");
		if (toUserId.intValue() == 0) {
			return;
		}
		BigDecimal amount = null;
		try {
			amount = new BigDecimal(console.getUserInput("Enter Amount"));
		} catch (NumberFormatException ex) {
			System.out.println();
			System.out.println("Enter numeric data only. Returning to previous menu.");
			return;
		}
		if (amount.compareTo(new BigDecimal("0")) == -1) {
			System.out.println();
			System.out.println("Enter Positive Numeric Values Only. Returning to previous Menu");
			return;
		}
		if (toUserId.intValue() == currentUser.getUser().getId()) {
			System.out.println();
			System.out.println("You cannot send money to yourself. Returning to Previous Menu.");
			return;
		}
		if (amount.compareTo(ts.getUserBalance(currentUser).getBalance()) == 1) {
			System.out.println();
			System.out.println("You do not have enough money in your account. Returning to previous Menu");
			return;
		}
		Account fromAccount = ts.getAccountByUserId(currentUser.getUser().getId(), currentUser);
		Account toAccount = ts.getAccountByUserId(toUserId, currentUser);
		Transfer transfer = null;
		try {
			transfer = new Transfer(1, 2, 2, fromAccount.getAccountId(), toAccount.getAccountId(), amount);

		} catch (NullPointerException ex) {
			System.out.println();
			System.out.println("That user does not exist. Returning to previous menu.");
			return;
		}
		ts.sendMoney(transfer, currentUser);

	}

	private void requestBucks() {
		console.printAllUsers(ts.getAllUsers(currentUser));
		System.out.println();
		Integer toUserId = console.getUserInputInteger("Enter ID of user you are requesting from (0 to cancel)");
		if (toUserId.intValue() == 0) {
			return;
		}
		BigDecimal amount = null;
		try {
			amount = new BigDecimal(console.getUserInput("Enter Amount"));
		} catch (NumberFormatException ex) {
			System.out.println();
			System.out.println("Enter numeric data only. Returning to previous menu.");
			return;
		}
		if (amount.compareTo(new BigDecimal("0")) == -1) {
			System.out.println();
			System.out.println("Enter Positive Numeric Values Only. Returning to previous Menu");
			return;
		}
		if (toUserId.intValue() == currentUser.getUser().getId()) {
			System.out.println();
			System.out.println("You cannot request money from yourself. Returning to Previous Menu.");
			return;
		}
		Account toAccount = ts.getAccountByUserId(currentUser.getUser().getId(), currentUser);
		Account fromAccount = ts.getAccountByUserId(toUserId, currentUser);
		Transfer transfer = null;
		try {
			transfer = new Transfer(1, 1, 1, fromAccount.getAccountId(), toAccount.getAccountId(), amount);

		} catch (NullPointerException ex) {
			System.out.println();
			System.out.println("That user does not exist. Returning to previous menu.");
			return;
		}
		ts.requestMoney(transfer, currentUser);

	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
				System.out.println();
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
				System.out.println();
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println();
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
				System.out.println();
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}
