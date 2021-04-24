package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.BalanceData;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDAO implements AccountDAO{

    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public BalanceData getBalanceGivenAnId(int id) {

        String sql = "SELECT balance from accounts where user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        BalanceData balanceData = new BalanceData();
        if (rowSet.next()) {
            String balance = rowSet.getString("balance");
            BigDecimal balanceBD = new BigDecimal(balance);
            balanceData .setBalance(balanceBD);
        }

        return balanceData;
    }

    @Override
    public Account getAccountByUserId(int id) {
        String sql = "SELECT account_id, user_id, balance from accounts where user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        Account account = null;
        if (rowSet.next()) {
            int accountId = rowSet.getInt("account_id");
            int userId = rowSet.getInt("user_id");
            BigDecimal balance = new BigDecimal(rowSet.getString("balance"));
            account = new Account(accountId, userId, balance);
        }
        return account;
    }

    @Override
    public Account[] getAllAccounts() {
        List<Account> a = new ArrayList<Account>();
        String sql = "SELECT account_id, user_id, balance " +
                "FROM accounts ORDER BY account_id";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
        while (rowSet.next()) {
            Account account = new Account(rowSet.getInt("account_id"), rowSet.getInt("user_id"), new BigDecimal(rowSet.getString("balance")));
            a.add(account);
        }
        Account[] aArray = new Account[a.size()];
        aArray = a.toArray(aArray);
        return aArray;
    }

}
