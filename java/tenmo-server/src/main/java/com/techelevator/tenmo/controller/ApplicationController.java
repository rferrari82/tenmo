package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.BalanceData;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class ApplicationController {

    @Autowired
    AccountDAO accountDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    TransferDAO transferDAO;

    @RequestMapping(path = "/get-balance", method = RequestMethod.GET)
    public BalanceData processBalanceRequests(Principal principal) {
        int correspondingUserId = userDAO.findIdByUsername(principal.getName());
        BalanceData balanceObject = accountDAO.getBalanceGivenAnId(correspondingUserId);
        return balanceObject;
    }

    @RequestMapping(path = "/get-all-users", method = RequestMethod.GET)
    public List<User> getAllUsers(Principal principal) {
        int correspondingUserId = userDAO.findIdByUsername(principal.getName());
        List<User> users = userDAO.findAll();
        return users;
    }

    @RequestMapping(path = "/get-all-accounts", method = RequestMethod.GET)
    public Account[] getAllAccounts(Principal principal) {
        return accountDAO.getAllAccounts();
    }

    @RequestMapping(path = "/get-account-by-user-id/{id}", method = RequestMethod.GET)
    public Account getAccountByUserId(@PathVariable int id) {
        return accountDAO.getAccountByUserId(id);
    }

    @RequestMapping(path = "/send-money", method = RequestMethod.POST)
    public void addTransfer(@RequestBody Transfer transfer) {
        logTransaction(transfer.toString());
        Transfer t = transfer;
        transferDAO.transferMoney(t, true);
    }

    @RequestMapping(path = "/request-money", method = RequestMethod.POST)
    public void processRequest(@RequestBody Transfer transfer) {
        logTransaction(transfer.toString());
        Transfer t = transfer;
        transferDAO.transferMoney(t, false);
    }

    @RequestMapping(path = "/get-all-transfers/{id}", method = RequestMethod.GET)
    public Transfer[] getAllTransfersByUserId(@PathVariable int id) {
        Account account = accountDAO.getAccountByUserId(id);
        logTransaction(account.toString());
        Transfer[] t = transferDAO.getTransfersByAccountID(account, false);
        for (Transfer tt : t) {
            logTransaction(tt.toString());
        }
        return (t);
    }

    @RequestMapping(path = "/get-pending-transfers/{id}", method = RequestMethod.GET)
    public Transfer[] getPendingTransfersByUserId(@PathVariable int id) {
        Account account = accountDAO.getAccountByUserId(id);
        logTransaction(account.toString());
        Transfer[] t = transferDAO.getTransfersByAccountID(account, true);
        for (Transfer tt : t) {
            logTransaction(tt.toString());
        }
        return (t);
    }

    @RequestMapping(path = "/approval-rejection", method = RequestMethod.POST)
    public void processApprovalRejection(@RequestBody Transfer transfer) {
        logTransaction(transfer.toString());
        transferDAO.handleApprovalRejection(transfer);
    }

    public static void logTransaction(String m) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter targetFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
        String formattedDateTime = currentDateTime.format(targetFormat);
        String message = "";

        File file = new File("log.txt");

        PrintWriter printWriter = null;
        FileOutputStream outputStream = null;
        if (file.exists()) {
            try {
                outputStream = new FileOutputStream(file, true);
                message += "\n";
            } catch (FileNotFoundException e) {
                System.out.println("File Not Found Exception. Terminating Program.");
                System.exit(1);
            }
            printWriter = new PrintWriter(outputStream);
        } else {
            try {
                printWriter = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                System.out.println("File Not Found Exception. Terminating Program.");
                System.exit(1);
            }

        }
        message += formattedDateTime + " " + m;
        printWriter.append(message);
        printWriter.flush();
        printWriter.close();
    }
}
