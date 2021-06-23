package com.example.mposapp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TransactionReqInfo implements Serializable {
    @SerializedName("message")
    String transaction_response;
}
