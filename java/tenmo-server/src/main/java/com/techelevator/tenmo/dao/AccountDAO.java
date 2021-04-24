package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.BalanceData;

public interface AccountDAO {

    public BalanceData getBalanceGivenAnId(int id);

    public Account getAccountByUserId(int id);

    public Account[] getAllAccounts();
}
