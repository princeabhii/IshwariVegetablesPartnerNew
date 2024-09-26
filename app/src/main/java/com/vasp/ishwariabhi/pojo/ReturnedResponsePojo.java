package com.vasp.ishwariabhi.pojo;

public class ReturnedResponsePojo {

    public String success;
    public String message;
    public String User_Id;
    public String Mgmt_Name;
    public String Mobile_No;
    public String Password;
    public String User_Type;
    public String Email_Id;
    public String Attachment;
    public String Day_Total_Profit;
    public String Month_Total_Profit;
    public String Year_Total_Profit;
    public String User_Total;
    public String Delivery_Amount;
    public String Total_Amount;
    public String Remaining_Amount;
    public String Total_Paid;
    public String Order_Id;
    public String Is_Enabled;
    public String Is_Update_Available;

    private CommonPojo[] products;
    private CommonPojo[] vendors;
    private CommonPojo[] Vegetables;
    private CommonPojo[] hotels;
    private CommonPojo[] cart;
    private CommonPojo[] OrderDetails;
    private CommonPojo[] SubDetails;
    private CommonPojo[] Details;

    public CommonPojo[] getDetails() {
        return Details;
    }

    public CommonPojo[] getSubDetails() {
        return SubDetails;
    }

    public CommonPojo[] getOrderDetails() {
        return OrderDetails;
    }

    public CommonPojo[] getCart() {
        return cart;
    }

    public CommonPojo[] getHotels() {
        return hotels;
    }

    public void setHotels(CommonPojo[] hotels) {
        this.hotels = hotels;
    }

    public CommonPojo[] getProducts() {
        return products;

    }

    public void setProducts(CommonPojo[] products) {
        this.products = products;

    }
    public CommonPojo[] getVendors() {
        return vendors;
    }

    public void setVendors(CommonPojo[] vendors) {
        this.vendors = vendors;
    }

    public CommonPojo[] getVegetables() {
        return Vegetables;
    }

    public void setVegetables(CommonPojo[] vegetables) {
        this.Vegetables = vegetables;
    }
}

