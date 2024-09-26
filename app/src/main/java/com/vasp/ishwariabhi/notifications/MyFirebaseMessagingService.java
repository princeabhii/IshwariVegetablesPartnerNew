package com.vasp.ishwariabhi.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vasp.ishwariabhi.R;
import com.vasp.ishwariabhi.admin.OrderDetailsNotificationActvity;
import com.vasp.ishwariabhi.session.UserSession;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private NotificationUtils notificationUtils;
    private NotificationManager notifManager;
    NotificationCompat.Builder builder;
    String title = "", Message = "",Key = "", Admin_Id = "", V_Id="", messageGlobal="",Driver_Id = "", key="", User_Id="", UserType="",
            Order_Id="", Order_Status="", User_Name="";
    String id = "default_channel_id";
    UserSession userSession;
    String Pagename="Notification";
    MediaPlayer mp;
    public static final String KEY_NOTIFICATION="key_notification";
    public static final String KEY_MGMTID="key_mgmtid";
    public static final String KEY_VID="key_vid";
    public static final String KEY_DriverID="key_driverid";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        System.out.println(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size()>0) {

            title = remoteMessage.getData().get("title");
            Message = remoteMessage.getData().get("message");
            key = remoteMessage.getData().get("key");
            User_Id = remoteMessage.getData().get("User_Id");
            UserType = remoteMessage.getData().get("UserType");
            Order_Id = remoteMessage.getData().get("Order_Id");
            User_Name = remoteMessage.getData().get("User_Name");

            Log.e(TAG, "onMessageReceived: title =   "+title );
            Log.e(TAG, "onMessageReceived: Message =   "+Message );
            Log.e(TAG, "onMessageReceived: key =   "+key );
            Log.e(TAG, "onMessageReceived: User_Id =   "+User_Id );
            Log.e(TAG, "onMessageReceived: UserType =   "+UserType );
            Log.e(TAG, "onMessageReceived: Order_Id =   "+Order_Id );
            Log.e(TAG, "onMessageReceived: User_Name =   "+User_Name );
            Log.e(TAG, "onMessageReceived: data =   "+remoteMessage.getData() );

//            adminSession = new AdminSession(getApplicationContext());
//            final HashMap<String, String> Admin = adminSession.getAdminDetails();
//            Admin_Id = adminSession.getAdmin_Id();


//            if(key.equals("placeorderAdmin")) {
//                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//                notificationUtils.playNotificationSound();
//                Intent resultIntent = new Intent(getApplicationContext(), AdminDashboard.class);
//                Bundle bundle = new Bundle();
//                bundle.putString(KEY_NOTIFICATION, Pagename);
//                bundle.putString(KEY_MGMTID, User_Id);
//                resultIntent.putExtras(bundle);
//            }

            showNotification(UserType);

        }

    }

    private void showNotification(String UserType){
        int notificationId=new Random().nextInt(101);
        String ChannelId = "notification_channel_5";
        NotificationManager notifManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Log.e("IDK", "showNotification 1 : " );
        Intent intent = null;
        if(UserType.equals("Admin")) {
            intent = new Intent(getApplicationContext(), OrderDetailsNotificationActvity.class);
            intent.putExtra("Order_Id",Order_Id);
            intent.putExtra("User_Name",User_Name);
            intent.putExtra("User_Id",User_Id);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
//        if(UserType.equals("User")) {
//            intent = new Intent(getApplicationContext(), UserDashboardActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, iUniqueId, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.playNotificationSound();

        Log.e("IDK", "showNotification 2 : " );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,ChannelId)
                .setSmallIcon(R.mipmap.ic_ishwari_logo_round)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(null)
                .setContentText(Message)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (notifManager != null && notifManager.getNotificationChannel(ChannelId)==null){
                NotificationChannel notificationChannel = new NotificationChannel(ChannelId,"Notification Channel 5",NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("I Am Done");
                notificationChannel.enableVibration(true);
                notificationChannel.enableLights(true);
                notifManager.createNotificationChannel(notificationChannel);
                Log.e("IDK", "showNotification 3 : " );

            }

        }
        Notification notification=builder.build();
        if (notifManager != null){
            notifManager.notify(notificationId,notification);
        }

        Log.e("IDK", "showNotification 4 : " );
    }

}
