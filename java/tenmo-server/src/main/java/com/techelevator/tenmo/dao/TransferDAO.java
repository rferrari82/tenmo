package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;

import javax.validation.Valid;
import java.math.BigDecimal;

public interface TransferDAO {

    public void transferMoney(Transfer transfer, boolean updateBalances);

    public Transfer[] getTransfersByAccountID(Account account, boolean pendingOnly);

    public void handleApprovalRejection(Transfer transfer);
}
