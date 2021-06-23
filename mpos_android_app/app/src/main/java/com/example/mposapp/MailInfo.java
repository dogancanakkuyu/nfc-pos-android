package com.example.mposapp;

public class MailInfo {

    private String toMail;
    private String amount;
    private String date;

    MailInfo(String toMail,String amount,String date){
        this.toMail=toMail;
        this.amount=amount;
        this.date=date;
    }

    public String getEmail() {
        return toMail;
    }

    public void setEmail(String email) {
        this.toMail = email;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
