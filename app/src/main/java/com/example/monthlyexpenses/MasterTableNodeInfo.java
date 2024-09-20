package com.example.monthlyexpenses;

public class MasterTableNodeInfo {
    private int table_id;
    private String table_unique_name, table_casual_name;

    public MasterTableNodeInfo(int table_id, String table_unique_name, String table_casual_name) {
        this.table_id = table_id;
        this.table_unique_name = table_unique_name;
        this.table_casual_name = table_casual_name;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public String getTable_unique_name() {
        return table_unique_name;
    }

    public void setTable_unique_name(String table_unique_name) {
        this.table_unique_name = table_unique_name;
    }

    public String getTable_casual_name() {
        return table_casual_name;
    }

    public void setTable_casual_name(String table_casual_name) {
        this.table_casual_name = table_casual_name;
    }
}
