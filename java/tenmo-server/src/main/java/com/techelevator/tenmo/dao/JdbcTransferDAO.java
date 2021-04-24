package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.BalanceData;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDAO implements TransferDAO {

    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void transferMoney(Transfer transfer, boolean updateBalances) {
        String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount)" +
                " VALUES (?, ?, ?, ?, ?)";
        int transferType = transfer.getTransferTypeId();
        int transferStatusID = transfer.getTransferStatusId();
        int accountFrom = transfer.getAccountFrom();
        int accountTo = transfer.getAccountTo();
        BigDecimal amount = transfer.getAmount();
        jdbcTemplate.update(sql, transferType, transferStatusID, accountFrom, accountTo, Double.parseDouble(amount.setScale(2, RoundingMode.HALF_UP).toString()));

        if (updateBalances) {
            sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            jdbcTemplate.update(sql, Double.parseDouble(amount.setScale(2, RoundingMode.HALF_UP).toString()), accountFrom);

            sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            jdbcTemplate.update(sql, Double.parseDouble(amount.setScale(2, RoundingMode.HALF_UP).toString()), accountTo);
        }
    }

    @Override
    public Transfer[] getTransfersByAccountID(Account account, boolean onlyPending) {
        List<Transfer> t = new ArrayList<Transfer>();
        String sql = null;
        SqlRowSet rowSet = null;
        if (!onlyPending) {
            sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                    "FROM transfers WHERE account_from = ? or account_to = ? ORDER BY transfer_id";
            rowSet = jdbcTemplate.queryForRowSet(sql, account.getAccountId(), account.getAccountId());
        } else {
            sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                    "FROM transfers WHERE (account_from = ?) and transfer_status_id = 1 ORDER BY transfer_id";
            rowSet = jdbcTemplate.queryForRowSet(sql, account.getAccountId());
        }
        while (rowSet.next()) {
            Transfer transfer = new Transfer(rowSet.getInt("transfer_id"), rowSet.getInt("transfer_type_id"), rowSet.getInt("transfer_status_id"), rowSet.getInt("account_from"), rowSet.getInt("account_to"), new BigDecimal(rowSet.getString("amount")));
            t.add(transfer);
        }
        Transfer[] tArray = new Transfer[t.size()];
        tArray = t.toArray(tArray);
        return tArray;
    }

    @Override
    public void handleApprovalRejection(Transfer transfer) {
        String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
        jdbcTemplate.update(sql, transfer.getTransferStatusId(), transfer.getTransferId());
        sql = "SELECT account_from, account_to, amount FROM transfers WHERE transfer_id = ?";
        int accountFrom = 0;
        int accountTo = 0;
        BigDecimal amount = null;
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, transfer.getTransferId());
        if (rowSet.next()) {
            accountFrom = rowSet.getInt("account_from");
            accountTo = rowSet.getInt("account_to");
            amount = new BigDecimal(rowSet.getString("amount"));
        }
        if (transfer.getTransferStatusId() == 2) {
            sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            jdbcTemplate.update(sql, Double.parseDouble(amount.setScale(2, RoundingMode.HALF_UP).toString()), accountFrom);

            sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            jdbcTemplate.update(sql, Double.parseDouble(amount.setScale(2, RoundingMode.HALF_UP).toString()), accountTo);
        }
    }
}

