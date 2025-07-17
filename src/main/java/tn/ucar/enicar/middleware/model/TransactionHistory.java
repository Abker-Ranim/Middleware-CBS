package tn.ucar.enicar.middleware.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionHistory {
    private List<Transaction> transactions;
    private String error; // Pour stocker un message d'erreur si nécessaire

    public TransactionHistory() {
        this.transactions = new ArrayList<>();
    }

    public TransactionHistory(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}

