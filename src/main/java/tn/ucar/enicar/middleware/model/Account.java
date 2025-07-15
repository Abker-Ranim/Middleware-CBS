package tn.ucar.enicar.middleware.model;

import lombok.Data;

@Data
public class Account {
    private String id;
    private String accountNumber;
    private double balance;
    private String customerId;
}