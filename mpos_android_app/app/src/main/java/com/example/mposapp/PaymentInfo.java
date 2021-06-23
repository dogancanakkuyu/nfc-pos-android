package com.example.mposapp;

public class PaymentInfo {
    private float amount;
    private String state;
    private String origin_ip;
    private String date;

    PaymentInfo(float amount,String state,String origin_ip,String date){
        this.amount=amount;
        this.state=state;
        this.origin_ip=origin_ip;
        this.date=date;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrigin_ip() {
        return origin_ip;
    }

    public void setOrigin_ip(String origin_ip) {
        this.origin_ip = origin_ip;
    }
}
