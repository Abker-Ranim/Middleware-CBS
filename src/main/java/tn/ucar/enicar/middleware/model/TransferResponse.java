package tn.ucar.enicar.middleware.model;


import lombok.Data;

@Data
public class TransferResponse {
    private String status;
    private double fromBalance;
    private double toBalance;
}
