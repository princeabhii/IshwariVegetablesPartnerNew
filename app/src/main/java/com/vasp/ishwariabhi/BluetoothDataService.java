package com.vasp.ishwariabhi;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothDataService extends Service {

    final int handlerState = 0;                        //used to identify handler message
    Handler bluetoothIn;
    private BluetoothAdapter btAdapter = null;
    private ConnectingThread mConnectingThread;
    private ConnectedThread mConnectedThread;

    private boolean stopThread;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static String MAC_ADDRESS = "";
    String BILL = "";

    private StringBuilder recDataString = new StringBuilder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BT SERVICE", "SERVICE CREATED");
        stopThread = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BT SERVICE", "SERVICE STARTED");
        bluetoothIn = new Handler() {

            public void handleMessage(android.os.Message msg) {
                Log.d("DEBUG", "handleMessage");
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);

                    Log.d("RECORDED", recDataString.toString());
                    // Do stuff here with your data, like adding it to the database
                }
                recDataString.delete(0, recDataString.length());                    //clear all string data
            }
        };

        if (intent != null) {
            BILL = intent.getStringExtra("BILL");
            MAC_ADDRESS = intent.getStringExtra("MAC_ADDRESS");

            if (mConnectedThread == null) {
                btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
                checkBTState();
            } else {
//            String BILL = "                   VEGETABLES\n\n\n\n" +
//
//             "                INVOICE/CASH MEMO    \n"
//                    + getString(R.string.app_name_print) +
//                    ""+getString(R.string.shop_addr_print) +
//                    ""+getString(R.string.mob_no_print) +
//                    "        Time: " + "strCurrTime" + "   Date: " + "" + "        \n" +
//                    "Hotel : " + "" + "   \n";
//            BILL = BILL
//                    + "-----------------------------------------------\n";
//
//
//            BILL = BILL + String.format("%1$-17s %2$-19s %3$6s", "SrNo.", "Date", "Amount");
//            BILL = BILL + "\n";
//            BILL = BILL
//                    + "-----------------------------------------------";
////                    }
//
//
//            for (int x = 0; x < 7; x++) {
//                try {
//                    BILL = BILL + "\n " + String.format("%1$-13s %2$-16s %3$12s", x + 1 + "", "aaa", "aaaa");
//                } catch (NumberFormatException e) {
//
//                }
//
//            }
//
//            BILL = BILL
//                    + "\n-----------------------------------------------";
//            BILL = BILL + "\n\n ";
//
//            BILL = BILL + "                       Total:" + "     " + "Total_Amount" + "\n";
//            BILL = BILL + "\n " + " ";
//
//            BILL = BILL + "\n " + " ";
//            BILL = BILL + "\n " + " ";
//            BILL = BILL + "\n " + " ";
//            BILL = BILL + "\n " + " ";
//            BILL = BILL + "\n " + " ";
//            BILL = BILL + "\n " + " ";

                if (!BILL.equals("")) {
                    mConnectedThread.write("" + BILL);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothIn.removeCallbacksAndMessages(null);
        stopThread = true;
        if (mConnectedThread != null) {
            mConnectedThread.closeStreams();
        }
        if (mConnectingThread != null) {
            mConnectingThread.closeSocket();
        }
        Log.d("SERVICE", "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if (btAdapter == null) {
            Log.d("BT SERVICE", "BLUETOOTH NOT SUPPORTED BY DEVICE, STOPPING SERVICE");
            stopSelf();
        } else {
            if (btAdapter.isEnabled()) {
                Log.d("DEBUG BT", "BT ENABLED! BT ADDRESS : " + btAdapter.getAddress() + " , BT NAME : " + btAdapter.getName());
                try {
                    BluetoothDevice device = btAdapter.getRemoteDevice(MAC_ADDRESS);
                    Log.d("DEBUG BT", "ATTEMPTING TO CONNECT TO REMOTE DEVICE : " + MAC_ADDRESS);
                    mConnectingThread = new ConnectingThread(device);
                    mConnectingThread.start();
                } catch (IllegalArgumentException e) {
                    Log.d("DEBUG BT", "PROBLEM WITH MAC ADDRESS : " + e.toString());
                    Log.d("BT SEVICE", "ILLEGAL MAC ADDRESS, STOPPING SERVICE");
                    stopSelf();
                }
            } else {
                Log.d("BT SERVICE", "BLUETOOTH NOT ON, STOPPING SERVICE");
                stopSelf();
            }
        }
    }

    // New Class for Connecting Thread
    private class ConnectingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectingThread(BluetoothDevice device) {
            Log.d("DEBUG BT", "IN CONNECTING THREAD");
            mmDevice = device;
            BluetoothSocket temp = null;
            Log.d("DEBUG BT", "MAC ADDRESS : " + MAC_ADDRESS);
            Log.d("DEBUG BT", "BT UUID : " + BTMODULEUUID);
            try {
                temp = mmDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
                Log.d("DEBUG BT", "SOCKET CREATED : " + temp.toString());
            } catch (IOException e) {
                Log.d("DEBUG BT", "SOCKET CREATION FAILED :" + e.toString());
                Log.d("BT SERVICE", "SOCKET CREATION FAILED, STOPPING SERVICE");
                stopSelf();
            }
            mmSocket = temp;
        }

//        @Override
//        public void run() {
//            super.run();
//            Log.d("DEBUG BT", "IN CONNECTING THREAD RUN");
//            // Establish the Bluetooth socket connection.
//            // Cancelling discovery as it may slow down connection
//            btAdapter.cancelDiscovery();
//            try {
//                mmSocket.connect();
//                Log.d("DEBUG BT", "BT SOCKET CONNECTED");
////                mConnectedThread = new ConnectedThread(mmSocket);
////                mConnectedThread.start();
//                Log.d("DEBUG BT", "CONNECTED THREAD STARTED");
//                //I send a character when resuming.beginning transmission to check device is connected
//                //If it is not an exception will be thrown in the write method and finish() will be called
////                mConnectedThread.write("x");
//
//                OutputStream os = mmSocket
//                        .getOutputStream();
//
//                String HEADING = "                 VEGETABLES\n\n\n\n";
//                String ENDING = "  \n";
//                String BILL = "";
//
//                BILL = "                INVOICE/CASH MEMO    \n"
//                        + getString(R.string.app_name_print) +
//                        ""+getString(R.string.shop_addr_print) +
//                        ""+getString(R.string.mob_no_print) +
//                        "        Time: " + "strCurrTime" + "   Date: " + "" + "        \n" +
//                        "Hotel : " +""+ "   \n";
//                BILL = BILL
//                        + "-----------------------------------------------\n";
//
//
//                BILL = BILL + String.format("%1$-17s %2$-19s %3$6s", "SrNo.", "Date", "Amount");
//                BILL = BILL + "\n";
//                BILL = BILL
//                        + "-----------------------------------------------";
////                    }
//
//
//                for (int x = 0; x < 7; x++) {
//                    try {
//                        BILL = BILL + "\n " + String.format("%1$-13s %2$-16s %3$12s", x + 1 + "", "aaa", "aaaa");
//                    } catch (NumberFormatException e) {
//
//                    }
//
//                }
//
//                BILL = BILL
//                        + "\n-----------------------------------------------";
//                BILL = BILL + "\n\n ";
//
//                BILL = BILL + "                       Total:" + "     " + "Total_Amount" + "\n";
//                BILL = BILL + "\n " + " ";
//
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//
//                os.write(new UserMonthlyBillsActivity.Formatter().small().bold().get());
//                os.write(HEADING.getBytes());
//                os.write(new UserMonthlyBillsActivity.Formatter().small().bold().get());
//                os.write(BILL.getBytes());
//
//                os.write(0x1D);
//                os.write(86);
//                os.write(48);
//                os.write(0);
//
//
//                OutputStream os2 = mmSocket
//                        .getOutputStream();
//                os2.write(ENDING.getBytes());
//
//                os2.write(0x1D);
//                os2.write(86);
//                os2.write(48);
//                os2.write(0);
//
//            } catch (IOException e) {
//                try {
//                    Log.d("DEBUG BT", "SOCKET CONNECTION FAILED : " + e.toString());
//                    Log.d("BT SERVICE", "SOCKET CONNECTION FAILED, STOPPING SERVICE");
//                    mmSocket.close();
//                    stopSelf();
//                } catch (IOException e2) {
//                    Log.d("DEBUG BT", "SOCKET CLOSING FAILED :" + e2.toString());
//                    Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
//                    stopSelf();
//                    //insert code to deal with this
//                }
//            } catch (IllegalStateException e) {
//                Log.d("DEBUG BT", "CONNECTED THREAD START FAILED : " + e.toString());
//                Log.d("BT SERVICE", "CONNECTED THREAD START FAILED, STOPPING SERVICE");
//                stopSelf();
//            }
//        }

        @Override
        public void run() {
            super.run();
            Log.d("DEBUG BT", "IN CONNECTING THREAD RUN");
            // Establish the Bluetooth socket connection.
            // Cancelling discovery as it may slow down connection
            btAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                toast(getApplicationContext(), "Printer Connected!");
                sendMessageToActivity("Yes");
                Log.d("DEBUG BT", "BT SOCKET CONNECTED");
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
                Log.d("DEBUG BT", "CONNECTED THREAD STARTED");
                //I send a character when resuming.beginning transmission to check device is connected
                //If it is not an exception will be thrown in the write method and finish() will be called

//                String HEADING = "                 VEGETABLES\n\n\n\n";
//                String ENDING = "  \n";
//                String BILL = "";
//
//                BILL = "                INVOICE/CASH MEMO    \n"
//                        + getString(R.string.app_name_print) +
//                        ""+getString(R.string.shop_addr_print) +
//                        ""+getString(R.string.mob_no_print) +
//                        "        Time: " + "strCurrTime" + "   Date: " + "" + "        \n" +
//                        "Hotel : " + "" + "   \n";
//                BILL = BILL
//                        + "-----------------------------------------------\n";
//
//
//                BILL = BILL + String.format("%1$-17s %2$-19s %3$6s", "SrNo.", "Date", "Amount");
//                BILL = BILL + "\n";
//                BILL = BILL
//                        + "-----------------------------------------------";
////                    }
//
//
//                for (int x = 0; x < 7; x++) {
//                    try {
//                        BILL = BILL + "\n " + String.format("%1$-13s %2$-16s %3$12s", x + 1 + "", "aaa", "aaaa");
//                    } catch (NumberFormatException e) {
//
//                    }
//
//                }
//
//                BILL = BILL
//                        + "\n-----------------------------------------------";
//                BILL = BILL + "\n\n ";
//
//                BILL = BILL + "                       Total:" + "     " + "Total_Amount" + "\n";
//                BILL = BILL + "\n " + " ";
//
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";
//                BILL = BILL + "\n " + " ";

                if (!BILL.equals("")) {
                    mConnectedThread.write("" + BILL);
                }
            } catch (IOException e) {
                try {
                    Log.d("DEBUG BT", "SOCKET CONNECTION FAILED : " + e.toString());
                    Log.d("BT SERVICE", "SOCKET CONNECTION FAILED, STOPPING SERVICE");
                    mmSocket.close();
                    stopSelf();
                } catch (IOException e2) {
                    Log.d("DEBUG BT", "SOCKET CLOSING FAILED :" + e2.toString());
                    Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                    stopSelf();
                    //insert code to deal with this
                }
            } catch (IllegalStateException e) {
                Log.d("DEBUG BT", "CONNECTED THREAD START FAILED : " + e.toString());
                Log.d("BT SERVICE", "CONNECTED THREAD START FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }

        public void closeSocket() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }

    // New Class for Connected Thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final OutputStream os2;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            Log.d("DEBUG BT", "IN CONNECTED THREAD");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d("DEBUG BT", e.toString());
                Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                stopSelf();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            os2 = tmpOut;
        }

        public void run() {
            Log.d("DEBUG BT", "IN CONNECTED THREAD RUN");
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true && !stopThread) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d("DEBUG BT PART", "CONNECTED THREAD " + readMessage);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    Log.d("DEBUG BT", e.toString());
                    Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                    stopSelf();
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(new Formatter().small().bold().get());
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                mmOutStream.write(0x1D);
                mmOutStream.write(86);
                mmOutStream.write(48);
                mmOutStream.write(0);
                String ENDING = "  \n";
                os2.write(ENDING.getBytes());
//                os2.write(intToByteArray(gs));
//
//                os2.write(intToByteArray(h));
//
//                os2.write(intToByteArray(n));
//
//                // Setting Width
//
//                os2.write(intToByteArray(gs_width));
//
//                os2.write(intToByteArray(w));
//
//                os2.write(intToByteArray(n_width));

                os2.write(0x1D);
                os2.write(86);
                os2.write(48);
                os2.write(0);
            } catch (IOException e) {
                //if you cannot write, close the application
                Log.d("DEBUG BT", "UNABLE TO READ/WRITE " + e.toString());
                Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                stopSelf();
            }
        }

        public void closeStreams() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "STREAM CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }

    public void toast(final Context context, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("intentKey");
        // You can also include some extra data.
        intent.putExtra("key", msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    public static class Formatter {
        /**
         * The format that is being build on
         */
        private byte[] mFormat;

        public Formatter() {
            // Default:
            mFormat = new byte[]{27, 33, 0};
        }

        /**
         * Method to get the Build result
         *
         * @return the format
         */
        public byte[] get() {
            return mFormat;
        }

        public Formatter bold() {
            // Apply bold:
            mFormat[2] = ((byte) (0x8 | mFormat[2]));
            return this;
        }

        public Formatter small() {
            mFormat[2] = ((byte) (0x4 | mFormat[2]));
            return this;
        }

        public Formatter height() {
            mFormat[2] = ((byte) (0x10 | mFormat[2]));
            return this;
        }

        public Formatter width() {
            mFormat[2] = ((byte) (0x20 | mFormat[2]));
            return this;
        }

        public Formatter underlined() {
            mFormat[2] = ((byte) (0x80 | mFormat[2]));
            return this;
        }

        public static byte[] rightAlign() {
            return new byte[]{0x1B, 'a', 0x02};
        }

        public static byte[] leftAlign() {
            return new byte[]{0x1B, 'a', 0x00};
        }

        public static byte[] centerAlign() {
            return new byte[]{0x1B, 'a', 0x01};
        }
    }
}