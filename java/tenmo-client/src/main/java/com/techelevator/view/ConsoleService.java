package com.techelevator.view;


import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import io.cucumber.java.sl.In;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleService {

	private PrintWriter out;
	private Scanner in;

	public ConsoleService(InputStream input, OutputStream output) {
		this.out = new PrintWriter(output, true);
		this.in = new Scanner(input);
	}

	public Object getChoiceFromOptions(Object[] options) {
		Object choice = null;
		while (choice == null) {
			displayMenuOptions(options);
			choice = getChoiceFromUserInput(options);
		}
		out.println();
		return choice;
	}

	private Object getChoiceFromUserInput(Object[] options) {
		Object choice = null;
		String userInput = in.nextLine();
		try {
			int selectedOption = Integer.valueOf(userInput);
			if (selectedOption > 0 && selectedOption <= options.length) {
				choice = options[selectedOption - 1];
			}
		} catch (NumberFormatException e) {
			// eat the exception, an error message will be displayed below since choice will be null
		}
		if (choice == null) {
			out.println(System.lineSeparator() + "*** " + userInput + " is not a valid option ***" + System.lineSeparator());
		}
		return choice;
	}

	private void displayMenuOptions(Object[] options) {
		out.println();
		for (int i = 0; i < options.length; i++) {
			int optionNum = i + 1;
			out.println(optionNum + ") " + options[i]);
		}
		out.print(System.lineSeparator() + "Please choose an option >>> ");
		out.flush();
	}

	public String getUserInput(String prompt) {
		out.print(prompt + ": ");
		out.flush();
		return in.nextLine();
	}

	public Integer getUserInputInteger(String prompt) {
		Integer result = null;
		do {
			out.print(prompt + ": ");
			out.flush();
			String userInput = in.nextLine();
			try {
				result = Integer.parseInt(userInput);
			} catch (NumberFormatException e) {
				out.println(System.lineSeparator() + "*** " + userInput + " is not valid ***" + System.lineSeparator());
			}
		} while (result == null);
		return result;
	}

	public void printBalance(Account account) {
		System.out.println("Your current account balance is: $" + account.getBalance());
	}

	public void printAllUsers(User[] users) {
		System.out.println("-------------------------------------------");
		System.out.println("Users");
		System.out.println("ID\t\t\tName");
		System.out.println("-------------------------------------------");
		for (User u : users) {
			System.out.println(u.getId() + "\t\t" + u.getUsername());
		}
	}

	public void viewTransfers(Transfer[] transfers, Account[] accounts, User[] users, User currentUser) {
		if (transfers.length == 0) {
			System.out.println("You do not have any past transfers. Returning to previous menu.");
			return;
		}
		System.out.println("-------------------------------------------");
		System.out.println("Transfers");
		System.out.printf("%-15s%-21s%s", "ID", "From/To", "Amount");
		System.out.println();
		System.out.println("-------------------------------------------");
		String fromName = "";
		String toName = "";
		Account theAccountFrom = null;
		Account theAccountTo = null;
		for (Transfer t : transfers) {
			for (Account a : accounts ){
				if (t.getAccountFrom() == a.getAccountId()) {
					theAccountFrom = a;
				}
				if (t.getAccountTo() == a.getAccountId()) {
					theAccountTo = a;
				}
			}
			for (User u : users ){
				if (u.getId() == theAccountFrom.getUserId()){
					fromName = u.getUsername();
				}
				if (u.getId() == theAccountTo.getUserId()){
					toName = u.getUsername();
				}
			}
			String status = "";
			if (t.getTransferStatusId() == 1) {
				status += "*";
			} else if ( t.getTransferStatusId() == 3) {
				status += "^";
			}
			if (currentUser.getUsername().equals(toName)) {
				System.out.printf("%-15sFrom: %-15s$%s", t.getTransferId(), fromName + status, new BigDecimal(t.getAmount().toString()).setScale(2, RoundingMode.HALF_UP).toString());
			} else {
				System.out.printf("%-15sTo:   %-15s$%s", t.getTransferId(), toName + status, new BigDecimal(t.getAmount().toString()).setScale(2, RoundingMode.HALF_UP).toString());
			}
			System.out.println();
			theAccountFrom = null;
			theAccountTo = null;
		}
		System.out.println();
		System.out.println("*Transfer is pending.");
		System.out.println("^Transfer has been rejected.");
		System.out.println();
		int transferId = getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
		System.out.println();
		if (transferId == 0) {
			return;
		}
		boolean transferExists = false;
		for (Transfer t : transfers) {
			if (t.getTransferId() == transferId) {
				transferExists = true;
				for (Account a : accounts ){
					if (t.getAccountFrom() == a.getAccountId()) {
						theAccountFrom = a;
					}
					if (t.getAccountTo() == a.getAccountId()) {
						theAccountTo = a;
					}
				}
				for (User u : users ){
					if (u.getId() == theAccountFrom.getUserId()){
						fromName = u.getUsername();
					}
					if (u.getId() == theAccountTo.getUserId()){
						toName = u.getUsername();
					}
				}

				System.out.println("--------------------------------------------");
				System.out.println("Transfer Details");
				System.out.println("--------------------------------------------");
				System.out.println("Id: " + t.getTransferId());
				System.out.println("From: " + fromName);
				System.out.println("To: " + toName);
				switch (t.getTransferTypeId()) {
					case 1: {
						System.out.println("Type: Request");
						break;
					}
					case 2: {
						System.out.println("Type: Send");
						break;
					}
				}
				switch (t.getTransferStatusId()) {
					case 1: {
						System.out.println("Status: Pending");
						break;
					}
					case 2: {
						System.out.println("Status: Approved");
						break;
					}
					case 3: {
						System.out.println("Status: Rejected");
						break;
					}
				}
				System.out.println("Amount: $" + t.getAmount());
				System.out.println("--------------------------------------------");
			}
		}
		if (!transferExists) {
			System.out.println("No transfers match that ID. Returning to previous menu.");
			return;
		}
	}

	public int[] viewPendingTransfers(Transfer[] transfers, Account[] accounts, User[] users, User currentUser) {
		if (transfers.length == 0) {
			System.out.println("You do not have any incoming pending requests. Returning to previous menu.");
			return new int[] {0,0};
		}
		System.out.println("-------------------------------------------");
		System.out.println("Transfers");
		System.out.printf("%-15s%-21s%s", "ID", "From/To", "Amount");
		System.out.println();
		System.out.println("-------------------------------------------");
		String fromName = "";
		String toName = "";
		Account theAccountFrom = null;
		Account theAccountTo = null;
		List<Integer> list = new ArrayList<>();
		for (Transfer t : transfers) {
			list.add(t.getTransferId());
			for (Account a : accounts ){
				if (t.getAccountFrom() == a.getAccountId()) {
					theAccountFrom = a;
				}
				if (t.getAccountTo() == a.getAccountId()) {
					theAccountTo = a;
				}
			}
			for (User u : users ){
				if (u.getId() == theAccountFrom.getUserId()){
					fromName = u.getUsername();
				}
				if (u.getId() == theAccountTo.getUserId()){
					toName = u.getUsername();
				}
			}
			System.out.printf("%-15sTo:   %-15s$%s", t.getTransferId(), toName , new BigDecimal(t.getAmount().toString()).setScale(2, RoundingMode.HALF_UP).toString());
		}
		System.out.println();
		System.out.println();
		int transferId = getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)");
		System.out.println();
		if (transferId == 0) {
			return new int[] {0,0};
		}
		if (!list.contains(transferId)) {
			System.out.println();
			System.out.println("That transaction does not exist. Returning to previous menu.");
			return new int[] {0,0};
		}
		System.out.println("1: Approve");
		System.out.println("2: Reject");
		System.out.println("0: Don't approve or reject");
		System.out.println("-------------------------------------------");
		System.out.println();
		int option = getUserInputInteger("Please choose an option");
		if (option == 0) {
			return new int[] {0,0};
		} else if (option == 1) {
			if (theAccountFrom.getBalance().compareTo(theAccountTo.getBalance()) == -1) {
				System.out.println();
				System.out.println("You do not have enough money to make this transfer. Returning to previous menu.");
				return new int[] {0,0};
			} else {
				return new int[] {2,transferId};
			}
		} else if (option == 2) {
			return new int[] {3,transferId};
		} else {
			System.out.println();
			System.out.println("You entered an invalid option. Returning to previous Menu.");
			return new int[] {0,0};
		}
	}
}
