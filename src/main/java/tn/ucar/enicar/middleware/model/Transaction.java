package tn.ucar.enicar.middleware.model;

import lombok.Data;

@Data
public class Transaction {
    private String type;
    private double amount;
    private String date;
}
