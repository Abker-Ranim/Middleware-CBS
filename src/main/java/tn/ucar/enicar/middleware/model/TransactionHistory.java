package tn.ucar.enicar.middleware.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionHistory extends ArrayList<Transaction> {
}

@Data
class Transaction {
    private String type;
    private double amount;
    private String date;
}
