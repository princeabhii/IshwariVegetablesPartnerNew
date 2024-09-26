package com.vasp.ishwariabhi.api;

import com.vasp.ishwariabhi.pojo.ReturnedResponsePojo;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AllApi {


//    @FormUrlEncoded
//    @POST("SendNotification.php")
//    Call<ReturnedResponsePojo> insertpushnotification(@Field("Title") String Title,
//                                                      @Field("Message") String Message);

//    @GET("GetPushNotification.php")
//    Call<ReturnedResponsePojo> viewpushnotification();

    @FormUrlEncoded
    @POST("GetActivationStatus.php")
    Call<ReturnedResponsePojo> GetActivationStatus(@Field("User_Id") String User_Id,
                                                       @Field("Version") String Version,
                                                       @Field("Token_Id") String Token_Id,
                                                       @Field("Android_Version") String Android_Version,
                                                       @Field("Device_Name") String Device_Name,
                                                       @Field("Version_Code") String Version_Code);

    @FormUrlEncoded
    @POST("AdminLogin.php")
    Call<ReturnedResponsePojo> AdminLogin(@Field("Email_Id") String Email_Id,
                                          @Field("Password") String Password,
                                          @Field("Version") String Version,
                                          @Field("Token_Id") String Token_Id);

    @FormUrlEncoded
    @POST("ConfirmOrder.php")
    Call<ReturnedResponsePojo> ConfirmOrder(@Field("Order_Id") String Order_Id,
                                            @Field("Order_Status") String Order_Status,
                                            @Field("User_Total") String User_Total,
                                            @Field("Vendor_Total") String Vendor_Total,
                                            @Field("Delivery_Amount") String Delivery_Amount,
                                            @Field("Total_Amount") String Total_Amount,
                                            @Field("User_Quantity") String User_Quantity,
                                            @Field("User_Price") String User_Price,
                                            @Field("Vendor_Price") String Vendor_Price);

    @FormUrlEncoded
    @POST("UpdateOrder.php")
    Call<ReturnedResponsePojo> UpdateOrder(@Field("Order_Id") String Order_Id,
                                           @Field("Order_Status") String Order_Status,
                                           @Field("User_Total") String User_Total,
                                           @Field("Vendor_Total") String Vendor_Total,
                                           @Field("Delivery_Amount") String Delivery_Amount,
                                           @Field("Total_Amount") String Total_Amount,
                                           @Field("User_Quantity") String User_Quantity,
                                           @Field("User_Price") String User_Price,
                                           @Field("Vendor_Price") String Vendor_Price);

    @FormUrlEncoded
    @POST("SetVegetablePrices.php")
    Call<ReturnedResponsePojo> SetVegetablePrices(@Field("Added_On") String Added_On,
                                                  @Field("User_Price") String User_Price,
                                                  @Field("Vendor_Price") String Vendor_Price,
                                                  @Field("OrderList") String OrderList);

    @FormUrlEncoded
    @POST("SetVendorVegetablePrices.php")
    Call<ReturnedResponsePojo> SetVendorVegetablePrices(@Field("Added_On") String Added_On,
                                                        @Field("User_Price") String User_Price,
                                                        @Field("Vendor_Price") String Vendor_Price,
                                                        @Field("OrderList") String OrderList);

    @FormUrlEncoded
    @POST("SetUserVegetablePrices.php")
    Call<ReturnedResponsePojo> SetUserVegetablePrices(@Field("Added_On") String Added_On,
                                                      @Field("User_Price") String User_Price,
                                                      @Field("Vendor_Price") String Vendor_Price);

    @FormUrlEncoded
    @POST("UpdateUserVegetablePrices.php")
    Call<ReturnedResponsePojo> UpdateUserVegetablePrices(@Field("Added_On") String Added_On,
                                                         @Field("User_Price") String User_Price,
                                                         @Field("Vendor_Price") String Vendor_Price);

    @FormUrlEncoded
    @POST("UpdateVendorOrder.php")
    Call<ReturnedResponsePojo> UpdateVendorOrder(@Field("V_Order_Id") String V_Order_Id,
                                                 @Field("Vegetable_Id") String Vegetable_Id,
                                                 @Field("Weight") String Weight,
                                                 @Field("Vendor_Price") String Vendor_Price,
                                                 @Field("Vendor_Id") String Vendor_Id,
                                                 @Field("Total_Amount") String Total_Amount,
                                                 @Field("Added_On") String Added_On);

    @FormUrlEncoded
    @POST("InsertVendorOrder.php")
    Call<ReturnedResponsePojo> InsertVendorOrder(@Field("Vegetable_Id") String Vegetable_Id,
                                                 @Field("Weight") String Weight,
                                                 @Field("Vendor_Price") String Vendor_Price,
                                                 @Field("Vendor_Id") String Vendor_Id,
                                                 @Field("Total_Amount") String Total_Amount,
                                                 @Field("Added_On") String Added_On);

    @FormUrlEncoded
    @POST("UpdateVegetablePrices.php")
    Call<ReturnedResponsePojo> UpdateVegetablePrices(@Field("Added_On") String Added_On,
                                                     @Field("User_Price") String User_Price,
                                                     @Field("Vendor_Price") String Vendor_Price,
                                                     @Field("OrderList") String OrderList);

    @FormUrlEncoded
    @POST("UpdateVendorVegetablePrices.php")
    Call<ReturnedResponsePojo> UpdateVendorVegetablePrices(@Field("Added_On") String Added_On,
                                                           @Field("User_Price") String User_Price,
                                                           @Field("Vendor_Price") String Vendor_Price,
                                                           @Field("OrderList") String OrderList);

    @FormUrlEncoded
    @POST("ChangeHotelStatus.php")
    Call<ReturnedResponsePojo> ChangeHotelStatus(@Field("User_Id") String User_Id,
                                                 @Field("Is_Enabled") String Is_Enabled);

    @FormUrlEncoded
    @POST("ChangeAdminStatus.php")
    Call<ReturnedResponsePojo> ChangeAdminStatus(@Field("User_Id") String User_Id,
                                                 @Field("Is_Enabled") String Is_Enabled);

    @Multipart
    @POST("AddHotel.php")
    Call<ReturnedResponsePojo> AddHotel(@Part("Mgmt_Name") RequestBody Mgmt_Name,
                                        @Part("Email_Id") RequestBody Email_Id,
                                        @Part("Mobile_No") RequestBody Mobile_No,
                                        @Part("Password") RequestBody Password,
                                        @Part MultipartBody.Part Attachment);

    @Multipart
    @POST("AddAdmin.php")
    Call<ReturnedResponsePojo> AddAdmin(@Part("Mgmt_Name") RequestBody Mgmt_Name,
                                        @Part("Email_Id") RequestBody Email_Id,
                                        @Part("Mobile_No") RequestBody Mobile_No,
                                        @Part("Password") RequestBody Password,
                                        @Part MultipartBody.Part Attachment);

    @GET("GetProducts.php")
    Call<ReturnedResponsePojo> GetProductsDetails();

    @GET("GetVendors.php")
    Call<ReturnedResponsePojo> GetVendors();

    @FormUrlEncoded
    @POST("GetVegetablesVendorWise.php")
    Call<ReturnedResponsePojo> GetVegetablesVendorWise(@Field("Vendor_Id") String Vendor_Id);

    @FormUrlEncoded
    @POST("GetVegetablesNotInVendor.php")
    Call<ReturnedResponsePojo> GetVegetablesNotInVendor(@Field("Vendor_Id") String Vendor_Id);

    @FormUrlEncoded
    @POST("GetPendingOrders.php")
    Call<ReturnedResponsePojo> GetPendingOrders(@Field("Order_Time") String Order_Time);

    @FormUrlEncoded
    @POST("GetOrdersUser.php")
    Call<ReturnedResponsePojo> GetOrdersUser(
            @Field("User_Id") String User_Id,
            @Field("Order_Time_End") String Order_Time_End,
            @Field("Order_Time_Start") String Order_Time_Start);

    @Multipart
    @POST("AddVendor.php")
    Call<ReturnedResponsePojo> AddVendor(
            @Part("Vendor_Name") RequestBody Vendor_Name);

    @Multipart
    @POST("AddVegetable.php")
    Call<ReturnedResponsePojo> AddVegetable(
            @Part("Vegetable_Name") RequestBody Vegetable_Name,
            @Part("Vegetable_Marathi") RequestBody Vegetable_Marathi,
            @Part MultipartBody.Part Attachment);

    @FormUrlEncoded
    @POST("DeleteVendor.php")
    Call<ReturnedResponsePojo> DeleteVendor(@Field("Vendor_Id") String Vendor_Id);

    @FormUrlEncoded
    @POST("DeleteVegetable.php")
    Call<ReturnedResponsePojo> DeleteVegetable(@Field("Vegetable_Id") String Vegetable_Id);

    @FormUrlEncoded
    @POST("AddExistingVegetable.php")
    Call<ReturnedResponsePojo> AddExistingVegetable(@Field("Vegetable_Id") String Vegetable_Id,
                                                    @Field("Vendor_Id") String Vendor_Id);

    @FormUrlEncoded
    @POST("DeleteVegetablesVendor.php")
    Call<ReturnedResponsePojo> DeleteVegetablesVendor(@Field("Vegetable_Id") String Vegetable_Id,
                                                      @Field("Vendor_Id") String Vendor_Id);

    @Multipart
    @POST("UpdateVendor.php")
    Call<ReturnedResponsePojo> UpdateVendor(
            @Part("Vendor_Name") RequestBody Vendor_Name,
            @Part("Vendor_Id") RequestBody Vendor_Id);


    @FormUrlEncoded
    @POST("DeleteHotel.php")
    Call<ReturnedResponsePojo> DeleteHotel(@Field("User_Id") String User_Id);

    @FormUrlEncoded
    @POST("DeleteAdmin.php")
    Call<ReturnedResponsePojo> DeleteAdmin(@Field("User_Id") String User_Id);

    @FormUrlEncoded
    @POST("GetAllVegetablesUser.php")
    Call<ReturnedResponsePojo> GetAllVegetablesUser(@Field("User_Id") String User_Id);

    @FormUrlEncoded
    @POST("GetDailyWeight.php")
    Call<ReturnedResponsePojo> GetDailyWeight(@Field("Added_On") String Added_On);

    @FormUrlEncoded
    @POST("GetVegetableWeightWise.php")
    Call<ReturnedResponsePojo> GetVegetableWeightWise(@Field("Added_On") String Added_On,
                                                      @Field("Vegetable_Id") String Vegetable_Id);

    @FormUrlEncoded
    @POST("GetVendorOrdersVegetableWise.php")
    Call<ReturnedResponsePojo> GetVendorOrdersVegetableWise(@Field("Added_On") String Added_On,
                                                            @Field("Vegetable_Id") String Vegetable_Id);

    @FormUrlEncoded
    @POST("GetVendorsVegetableWise.php")
    Call<ReturnedResponsePojo> GetVendorsVegetableWise(@Field("Added_On") String Added_On,
                                                       @Field("Vegetable_Id") String Vegetable_Id);

    @FormUrlEncoded
    @POST("GetUserProfile.php")
    Call<ReturnedResponsePojo> GetUserProfile(@Field("User_Id") String User_Id);

    @FormUrlEncoded
    @POST("GetCartVegetables.php")
    Call<ReturnedResponsePojo> GetCartVegetables(@Field("User_Id") String User_Id,
                                                 @Field("Order_Time") String Order_Time);

    @FormUrlEncoded
    @POST("CheckUserStatus.php")
    Call<ReturnedResponsePojo> CheckUserStatus(@Field("User_Id") String User_Id,
                                               @Field("Version") String Version,
                                               @Field("Token_Id") String Token_Id);

    @FormUrlEncoded
    @POST("GetOrdersHistory.php")
    Call<ReturnedResponsePojo> GetOrdersHistory(@Field("Order_Id") String Order_Id);

    @FormUrlEncoded
    @POST("GetOrderDetailsUserWise.php")
    Call<ReturnedResponsePojo> GetOrderDetailsUserWise(@Field("User_Id") String User_Id,
                                                       @Field("Order_Time") String Order_Time);

    @FormUrlEncoded
    @POST("GetProfitDatewise.php")
    Call<ReturnedResponsePojo> GetProfitDatewise(@Field("From_Date") String From_Date,
                                                 @Field("To_Date") String To_Date);

    @FormUrlEncoded
    @POST("GetOrderHistoryAdmin.php")
    Call<ReturnedResponsePojo> GetOrderHistoryAdmin(@Field("Order_Id") String Order_Id);


    @FormUrlEncoded
    @POST("GetDailyWeightAdmin.php")
    Call<ReturnedResponsePojo> GetDailyWeightAdmin(@Field("Added_On") String Added_On,
                                                   @Field("Yesterday_Added_On") String Yesterday_Added_On);

    @FormUrlEncoded
    @POST("GetUserDailyWeight.php")
    Call<ReturnedResponsePojo> GetUserDailyWeight(@Field("Added_On") String Added_On,
                                                  @Field("Yesterday_Added_On") String Yesterday_Added_On);

    @FormUrlEncoded
    @POST("GetVegetablePrices.php")
    Call<ReturnedResponsePojo> GetVegetablePrices(@Field("Added_On") String Added_On);

    @FormUrlEncoded
    @POST("GetUserVegetablesPrices.php")
    Call<ReturnedResponsePojo> GetUserVegetablesPrices(@Field("Added_On") String Added_On);

    @FormUrlEncoded
    @POST("GetVegetablesNotOrdered.php")
    Call<ReturnedResponsePojo> GetVegetablesNotOrdered(@Field("Order_Id") String Order_Id);

    @FormUrlEncoded
    @POST("DeleteCart.php")
    Call<ReturnedResponsePojo> DeleteCart(@Field("User_Id") String User_Id,
                                          @Field("Vegetable_Id") String Vegetable_Id);

    @FormUrlEncoded
    @POST("DeleteVegetableFromOrder.php")
    Call<ReturnedResponsePojo> DeleteVegetableFromOrder(@Field("Detail_Id") String Detail_Id);

    @FormUrlEncoded
    @POST("PlaceOrder.php")
    Call<ReturnedResponsePojo> PlaceOrder(@Field("User_Id") String User_Id,
                                          @Field("Order_Time") String Order_Time,
                                          @Field("Order") String Order);

    @FormUrlEncoded
    @POST("GetDayProfit.php")
    Call<ReturnedResponsePojo> GetDayProfit(@Field("Day_Order_Time") String Day_Order_Time,
                                            @Field("Month_Order_Time") String Month_Order_Time,
                                            @Field("Year_Order_Time") String Year_Order_Time,
                                            @Field("End_Order_Time") String End_Order_Time);

    @FormUrlEncoded
    @POST("AddVegetableInOrder.php")
    Call<ReturnedResponsePojo> AddVegetableInOrder(@Field("User_Id") String User_Id,
                                                   @Field("Order_Id") String Order_Id,
                                                   @Field("Added_On") String Added_On,
                                                   @Field("Weight") String Weight,
                                                   @Field("Vegetable_Id") String Vegetable_Id);

    @FormUrlEncoded
    @POST("AddVegetableInEditOrder.php")
    Call<ReturnedResponsePojo> AddVegetableInEditOrder(@Field("User_Id") String User_Id,
                                                       @Field("Order_Id") String Order_Id,
                                                       @Field("Added_On") String Added_On,
                                                       @Field("Weight") String Weight,
                                                       @Field("User_Price") String User_Price,
                                                       @Field("Vendor_Price") String Vendor_Price,
                                                       @Field("Vegetable_Id") String Vegetable_Id);

    @FormUrlEncoded
    @POST("EditOrderByUser.php")
    Call<ReturnedResponsePojo> EditOrderByUser(@Field("Order_Details") String Order_Details);

    @FormUrlEncoded
    @POST("AddToCart.php")
    Call<ReturnedResponsePojo> AddToCart(@Field("User_Id") String User_Id,
                                         @Field("Vegetable_Id") String Vegetable_Id);

    @FormUrlEncoded
    @POST("DeleteHotelPayment.php")
    Call<ReturnedResponsePojo> DeleteHotelPayment(@Field("Payment_Id") String Payment_Id);

    @FormUrlEncoded
    @POST("DeleteVendorPayment.php")
    Call<ReturnedResponsePojo> DeleteVendorPayment(@Field("Payment_Id") String Payment_Id);

    @FormUrlEncoded
    @POST("InsertHotelPayment.php")
    Call<ReturnedResponsePojo> InsertHotelPayment(@Field("User_Id") String User_Id,
                                                  @Field("Payment_Date") String Payment_Date,
                                                  @Field("Payment_Amount") String Payment_Amount,
                                                  @Field("Payment_Type") String Payment_Type,
                                                  @Field("Hotel_Id") String Hotel_Id,
                                                  @Field("Cheque_No") String Cheque_No);
    @FormUrlEncoded
    @POST("InsertVendorPayment.php")
    Call<ReturnedResponsePojo> InsertVendorPayment(@Field("User_Id") String User_Id,
                                                  @Field("Payment_Date") String Payment_Date,
                                                  @Field("Weight") String Weight,
                                                  @Field("Amount") String Amount,
                                                  @Field("Total") String Total,
                                                  @Field("Payment_Type") String Payment_Type,
                                                  @Field("Vendor_Id") String Vendor_Id,
                                                  @Field("Cheque_No") String Cheque_No);

    @FormUrlEncoded
    @POST("GetOrdersDateWise.php")
    Call<ReturnedResponsePojo> GetOrdersDateWise(@Field("User_Id") String User_Id,
                                                 @Field("From_Date") String From_Date,
                                                 @Field("To_Date") String To_Date,
                                                 @Field("Payment_Status") String Payment_Status);

    @FormUrlEncoded
    @POST("GetHotelPayments.php")
    Call<ReturnedResponsePojo> GetHotelPayments(@Field("User_Id") String User_Id,
                                                @Field("From_Date") String From_Date,
                                                @Field("To_Date") String To_Date,
                                                @Field("Payment_Type") String Payment_Type);

    @FormUrlEncoded
    @POST("GetVendorPayments.php")
    Call<ReturnedResponsePojo> GetVendorPayments(@Field("Vendor_Id") String Vendor_Id,
                                                @Field("From_Date") String From_Date,
                                                @Field("To_Date") String To_Date,
                                                @Field("Payment_Type") String Payment_Type);

    @FormUrlEncoded
    @POST("GetVendorOrdersDateWise.php")
    Call<ReturnedResponsePojo> GetVendorOrdersDateWise(@Field("Vendor_Id") String Vendor_Id,
                                                       @Field("From_Date") String From_Date,
                                                       @Field("To_Date") String To_Date,
                                                       @Field("Payment_Status") String Payment_Status);

    @FormUrlEncoded
    @POST("GetVendorOrderDetails.php")
    Call<ReturnedResponsePojo> GetVendorOrderDetails(@Field("Vendor_Id") String Vendor_Id,
                                                     @Field("Vegetable_Id") String Vegetable_Id,
                                                     @Field("From_Date") String From_Date,
                                                     @Field("To_Date") String To_Date,
                                                     @Field("Payment_Status") String Payment_Status);

    @FormUrlEncoded
    @POST("GetHotelsForOrder.php")
    Call<ReturnedResponsePojo> GetHotelsForOrder(@Field("Order_Time") String Order_Time);

    @FormUrlEncoded
    @POST("GetHotelsForReceipt.php")
    Call<ReturnedResponsePojo> GetHotelsForReceipt(@Field("Order_Time") String Order_Time);

    @FormUrlEncoded
    @POST("GetHotelsForMonthlyReceipt.php")
    Call<ReturnedResponsePojo> GetHotelsForMonthlyReceipt(@Field("From_Date") String From_Date,
                                                          @Field("To_Date") String To_Date,
                                                          @Field("Payment_Status") String Payment_Status);

    @FormUrlEncoded
    @POST("GetHotelsForPayments.php")
    Call<ReturnedResponsePojo> GetHotelsForPayments(@Field("From_Date") String From_Date,
                                                    @Field("To_Date") String To_Date);

    @FormUrlEncoded
    @POST("GetVendorsForPayments.php")
    Call<ReturnedResponsePojo> GetVendorsForPayments(@Field("From_Date") String From_Date,
                                                    @Field("To_Date") String To_Date);

    @FormUrlEncoded
    @POST("GetVendorsForMonthlyReceipt.php")
    Call<ReturnedResponsePojo> GetVendorsForMonthlyReceipt(@Field("From_Date") String From_Date,
                                                           @Field("To_Date") String To_Date,
                                                           @Field("Payment_Status") String Payment_Status);

    @FormUrlEncoded
    @POST("UpdateHotelPaymentStatus.php")
    Call<ReturnedResponsePojo> UpdateHotelPaymentStatus(@Field("From_Date") String From_Date,
                                                        @Field("To_Date") String To_Date,
                                                        @Field("Payment_Status") String Payment_Status,
                                                        @Field("User_Id") String User_Id);

    @FormUrlEncoded
    @POST("UpdateHotelPaymentStatusNew.php")
    Call<ReturnedResponsePojo> UpdateHotelPaymentStatusNew(@Field("From_Date") String From_Date,
                                                           @Field("To_Date") String To_Date,
                                                           @Field("Payment_Status") String Payment_Status,
                                                           @Field("Hotel_Id") String Hotel_Id,
                                                           @Field("Payment_Amount") String Payment_Amount,
                                                           @Field("Cheque_No") String Cheque_No,
                                                           @Field("User_Id") String User_Id,
                                                           @Field("Payment_Date") String Payment_Date,
                                                           @Field("Payment_Type") String Payment_Type);

    @FormUrlEncoded
    @POST("UpdateVendorPaymentStatus.php")
    Call<ReturnedResponsePojo> UpdateVendorPaymentStatus(@Field("From_Date") String From_Date,
                                                         @Field("To_Date") String To_Date,
                                                         @Field("Payment_Status") String Payment_Status,
                                                         @Field("Vendor_Id") String Vendor_Id,
                                                         @Field("Vegetable_Id") String Vegetable_Id);

    @GET("GetHotels.php")
    Call<ReturnedResponsePojo> GetHotels();

    @GET("GetAdmins.php")
    Call<ReturnedResponsePojo> GetAdmins();

    @GET("GetAllVegetables.php")
    Call<ReturnedResponsePojo> GetAllVegetables();

    @Multipart
    @POST("UpdateHotel.php")
    Call<ReturnedResponsePojo> UpdateHotel(
            @Part("User_Id") RequestBody User_Id,
            @Part("Mgmt_Name") RequestBody Mgmt_Name,
            @Part("Password") RequestBody Password,
            @Part("Email_Id") RequestBody Email_Id,
            @Part("Mobile_No") RequestBody Mobile_No,
            @Part MultipartBody.Part Attachment);

    @Multipart
    @POST("UpdateHotel.php")
    Call<ReturnedResponsePojo> UpdateHotel(
            @Part("User_Id") RequestBody User_Id,
            @Part("Mgmt_Name") RequestBody Mgmt_Name,
            @Part("Password") RequestBody Password,
            @Part("Email_Id") RequestBody Email_Id,
            @Part("Mobile_No") RequestBody Mobile_No);

    @Multipart
    @POST("UpdateAdmin.php")
    Call<ReturnedResponsePojo> UpdateAdmin(
            @Part("User_Id") RequestBody User_Id,
            @Part("Mgmt_Name") RequestBody Mgmt_Name,
            @Part("Password") RequestBody Password,
            @Part("Email_Id") RequestBody Email_Id,
            @Part("Mobile_No") RequestBody Mobile_No,
            @Part MultipartBody.Part Attachment);

    @Multipart
    @POST("UpdateAdmin.php")
    Call<ReturnedResponsePojo> UpdateAdmin(
            @Part("User_Id") RequestBody User_Id,
            @Part("Mgmt_Name") RequestBody Mgmt_Name,
            @Part("Password") RequestBody Password,
            @Part("Email_Id") RequestBody Email_Id,
            @Part("Mobile_No") RequestBody Mobile_No);

    @Multipart
    @POST("UpdateVegetable.php")
    Call<ReturnedResponsePojo> UpdateVegetable(
            @Part("Vegetable_Id") RequestBody Vegetable_Id,
            @Part("Vegetable_Name") RequestBody Vegetable_Name,
            @Part("Vegetable_Marathi") RequestBody Vegetable_Marathi,
            @Part("Vendor_Id") RequestBody Vendor_Id,
            @Part MultipartBody.Part Attachment);

    @Multipart
    @POST("UpdateVegetable.php")
    Call<ReturnedResponsePojo> UpdateVegetable(
            @Part("Vegetable_Id") RequestBody Vegetable_Id,
            @Part("Vegetable_Name") RequestBody Vegetable_Name,
            @Part("Vegetable_Marathi") RequestBody Vegetable_Marathi,
            @Part("Vendor_Id") RequestBody Vendor_Id);

    @Multipart
    @POST("UpdateUserProfile.php")
    Call<ReturnedResponsePojo> UpdateUserProfile(
            @Part("User_Id") RequestBody User_Id,
            @Part("Mgmt_Name") RequestBody Mgmt_Name,
            @Part("Email_Id") RequestBody Email_Id,
            @Part("Mobile_No") RequestBody Mobile_No,
            @Part("Password") RequestBody Password,
            @Part MultipartBody.Part Attachment);

    @Multipart
    @POST("UpdateUserProfile.php")
    Call<ReturnedResponsePojo> UpdateUserProfile(
            @Part("User_Id") RequestBody User_Id,
            @Part("Mgmt_Name") RequestBody Mgmt_Name,
            @Part("Email_Id") RequestBody Email_Id,
            @Part("Mobile_No") RequestBody Mobile_No,
            @Part("Password") RequestBody Password);
}
