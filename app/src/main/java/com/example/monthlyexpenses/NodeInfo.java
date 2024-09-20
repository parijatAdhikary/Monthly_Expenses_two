package com.example.monthlyexpenses;

import android.os.Bundle;

public class NodeInfo {
    private String t_title,t_date,t_type;
    private int t_amount,t_id,balance;

    public NodeInfo(int t_id, String t_title, String t_date, String t_type, int t_amount, int balance) {
        this.t_title = t_title;
        this.balance = balance;
        this.t_date = t_date;
        this.t_type = t_type;
        this.t_amount = t_amount;
        this.t_id = t_id;
    }

    public String getT_type() {
        return t_type;
    }

    public void setT_type(String t_type) {
        this.t_type = t_type;
    }

    public String getT_title() {
        return t_title;
    }

    public void setT_title(String t_title) {
        this.t_title = t_title;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getT_date() {
        return t_date;
    }

    public void setT_date(String t_date) {
        this.t_date = t_date;
    }

    public int getT_amount() {
        return t_amount;
    }

    public void setT_amount(int t_amount) {
        this.t_amount = t_amount;
    }

    public int getT_id() {
        return t_id;
    }

    public void setT_id(int t_id) {
        this.t_id = t_id;
    }
}
