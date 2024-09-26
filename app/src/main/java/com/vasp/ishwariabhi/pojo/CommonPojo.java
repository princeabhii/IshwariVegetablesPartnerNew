package com.vasp.ishwariabhi.pojo;

import java.io.Serializable;

public class CommonPojo implements Serializable {

    public String Vendor_Id;
    public String Vendor_Name;
    public String Is_Visible;
    public String Is_Enabled;
    public String Vegetable_Id;
    public String User_Id;
    public String Vegetable_Name;
    public String Mgmt_Name;
    public String Email_Id;
    public String Password;
    public String Mobile_No;
    public String Vegetable_Img;
    public String Attachment;
    public String In_Cart;
    public String Order_Status;
    public String User_Total;
    public String Vendor_Total;
    public String Order_Time;
    public String Added_On;
    public String Delivery_Amount;
    public String Total_Amount;
    public String Order_Id;
    public String Total_Vegetable_Count;
    public String Weight;
    public String Total_Weight;
    public String Selling_Price;
    public String Detail_Id;
    public String Product_Id;
    public String Product_Name;
    public String Product_Price;
    public String Product_Img;
    public String User_Price;
    public String Vendor_Price;
    public String Buying_Price;
    public String Total_Profit;
    public String Payment_Status;
    public String V_Order_Id;
    public String Vegetable_Marathi;
    public String Payment_Id;
    public String Payment_Date;
    public String Payment_Amount;
    public String Payment_Type;
    public String Cheque_No;

    public String getCheque_No() {
        return Cheque_No;
    }

    public String getPayment_Id() {
        return Payment_Id;
    }

    public String getPayment_Date() {
        return Payment_Date;
    }

    public String getPayment_Amount() {
        return Payment_Amount;
    }

    public String getPayment_Type() {
        return Payment_Type;
    }

    public String getVegetable_Marathi() {
        return Vegetable_Marathi;
    }

    public String getV_Order_Id() {
        return V_Order_Id;
    }

    public String getPayment_Status() {
        return Payment_Status;
    }

    private SubCommonPojo[] Vendors;

    public SubCommonPojo[] getVendors() {
        return Vendors;
    }

    public String getTotal_Profit() {
        return Total_Profit;
    }

    public String getTotal_Weight() {
        return Total_Weight;
    }

    public String getBuying_Price() {
        return Buying_Price;
    }

    public String getSelling_Price() {
        return Selling_Price;
    }

    public String getUser_Price() {
        return User_Price;
    }

    public String getVendor_Price() {
        return Vendor_Price;
    }

    public String getDetail_Id() {
        return Detail_Id;
    }

    public String getWeight() {
        return Weight;
    }

    public String getTotal_Vegetable_Count() {
        return Total_Vegetable_Count;
    }

    public String getOrder_Id() {
        return Order_Id;
    }

    public String getOrder_Status() {
        return Order_Status;
    }

    public String getUser_Total() {
        return User_Total;
    }

    public String getVendor_Total() {
        return Vendor_Total;
    }

    public String getOrder_Time() {
        return Order_Time;
    }

    public String getAdded_On() {
        return Added_On;
    }

    public String getDelivery_Amount() {
        return Delivery_Amount;
    }

    public String getTotal_Amount() {
        return Total_Amount;
    }

    public String getIn_Cart() {
        return In_Cart;
    }

    public String getIs_Enabled() {
        return Is_Enabled;
    }

    public String getUser_Id() {
        return User_Id;
    }

    public String getMgmt_Name() {
        return Mgmt_Name;
    }

    public void setMgmt_Name(String mgmt_Name) {
        Mgmt_Name = mgmt_Name;
    }

    public void setUser_Id(String User_Id) {
        User_Id = User_Id;
    }

    public String getEmail_Id() {
        return Email_Id;
    }

    public String getPassword() {
        return Password;
    }

    public String getMobile_No() {
        return Mobile_No;
    }

    public void setAttachment(String attachment) {
        Attachment = attachment;
    }

    public String getVendor_Name() {
        return Vendor_Name;
    }

    public String getVendor_Id() {
        return Vendor_Id;
    }
    public String getIs_Visible() {
        return Is_Visible;
    }
    public String getVegetable_Name() {
        return Vegetable_Name;
    }
    public String getVegetable_Img() {
        return Vegetable_Img;
    }

    public void setVegetable_Img(String Vegetable_Img) {
        this.Vegetable_Img = Vegetable_Img;
    }

    public String getVegetable_Id() {
        return Vegetable_Id;
    }
    public String getAttachment() {
        return Attachment;
    }

    public String getProduct_Id() {
        return Product_Id;
    }

    public void setProduct_Id(String Product_Id) {
        this.Product_Id = Product_Id;
    }

    public String getProduct_Name() {
        return Product_Name;
    }
    public void setProduct_Name(String Product_Name) {
        this.Product_Name = Product_Name;
    }

    public String getProduct_Price() {
        return Product_Price;
    }

    public void setProduct_Price(String Product_Price) {
        this.Product_Price = Product_Price;
    }
    public String getProduct_Img() {
        return Product_Img;
    }

    public void setProduct_Img(String Product_Img) {
        this.Product_Img = Product_Img;
    }



}
