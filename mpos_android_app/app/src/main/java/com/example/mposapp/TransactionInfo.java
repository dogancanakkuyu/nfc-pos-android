package com.example.mposapp;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TransactionInfo implements Serializable {
    @SerializedName("amount")
    float amount;
    @SerializedName("state")
    String state;
    @SerializedName("origin_ip")
    String ip;
    @SerializedName("date")
    String date;

}
