package com.techelevator.tenmo.services;

import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.view.ConsoleService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class TenmoService {

    public static String AUTH_TOKEN = "";
    private final String BASE_URL;
    private final RestTemplate restTemplate = new RestTemplate();

    public TenmoService(String url) {
        BASE_URL = url;
    }
    
    public Account getUserBalance(AuthenticatedUser currentUser) {
        HttpEntity entity = makeAuthEntity(currentUser.getToken());
        Account account = null;
        try {
            account = restTemplate.exchange(BASE_URL + "/get-balance", HttpMethod.GET, entity, Account.class).getBody();
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
           System.out.println(message);
        }
        return account;
    }

    public User[] getAllUsers(AuthenticatedUser currentUser) {
        HttpEntity entity = makeAuthEntity(currentUser.getToken());
        User[] users = null;
        try {
            users = restTemplate.exchange(BASE_URL + "/get-all-users", HttpMethod.GET, entity, User[].class).getBody();
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
            System.out.println(message);
        }
        return users;
    }

    public Account[] getAllAccounts(AuthenticatedUser currentUser) {
        HttpEntity entity = makeAuthEntity(currentUser.getToken());
        Account[] accounts = null;
        try {
            accounts = restTemplate.exchange(BASE_URL + "/get-all-accounts", HttpMethod.GET, entity, Account[].class).getBody();
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
            System.out.println(message);
        }
        return accounts;
    }



    public Account getAccountByUserId(int userID, AuthenticatedUser currentUser) {
        HttpEntity entity = makeAuthEntity(currentUser.getToken());
        Account account = null;
        try {
            account = restTemplate.exchange(BASE_URL + "/get-account-by-user-id/" + userID, HttpMethod.GET, entity, Account.class).getBody();
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
            System.out.println(message);
        }
        return account;
    }

    public void sendMoney(Transfer t, AuthenticatedUser currentUser) {
        HttpEntity entity = makeTransferEntity(t, currentUser.getToken());
        try {
            restTemplate.exchange(BASE_URL + "/send-money", HttpMethod.POST, entity, Transfer.class);
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
            System.out.println(message);
        }
    }

    public void requestMoney(Transfer t, AuthenticatedUser currentUser) {
        HttpEntity entity = makeTransferEntity(t, currentUser.getToken());
        try {
            restTemplate.exchange(BASE_URL + "/request-money", HttpMethod.POST, entity, Transfer.class);
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
            System.out.println(message);
        }
    }

    public Transfer[] getTransfers(int userId, AuthenticatedUser currentUser) {
        HttpEntity entity = makeAuthEntity(currentUser.getToken());
        Transfer[] transfers = null;
        try {
            transfers = restTemplate.exchange(BASE_URL + "/get-all-transfers/" + userId, HttpMethod.GET, entity, Transfer[].class).getBody();
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
            System.out.println(message);
        }
        return transfers;
    }

    public Transfer[] getPendingTransfers(int userId, AuthenticatedUser currentUser) {
        HttpEntity entity = makeAuthEntity(currentUser.getToken());
        Transfer[] transfers = null;
        try {
            transfers = restTemplate.exchange(BASE_URL + "/get-pending-transfers/" + userId, HttpMethod.GET, entity, Transfer[].class).getBody();
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
            System.out.println(message);
        }
        return transfers;
    }

    public void sendApprovalOrReject(int status, int transferId, AuthenticatedUser currentUser) {
        Transfer t = new Transfer();
        t.setTransferId(transferId);
        t.setTransferStatusId(status);
        HttpEntity entity = makeTransferEntity(t, currentUser.getToken());
        try {
            restTemplate.exchange(BASE_URL + "/approval-rejection", HttpMethod.POST, entity, Transfer.class);
        } catch (RestClientResponseException ex) {
            String message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
            System.out.println(message);
        }
    }

    /**
     * Returns an {HttpEntity} with the `Authorization: Bearer:` header
     * @return {HttpEntity}
     */
    private HttpEntity makeAuthEntity(String AUTH_TOKEN) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(AUTH_TOKEN);
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer, String AUTH_TOKEN) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(AUTH_TOKEN);
        HttpEntity<Transfer> entity = new HttpEntity<>(transfer, headers);
        return entity;
    }

}

