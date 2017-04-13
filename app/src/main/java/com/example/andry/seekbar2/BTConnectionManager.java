package com.example.andry.seekbar2;

/**
 * Created by Andry on 12.11.2016.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;


/**
 * Created by Andry on 12.11.2016.
 */


class BTConnectionManager {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//    private OutputStream outStream;
//    private InputStream inpStream;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private String address;
    private String readed;
    private int count;
    private Context context;
    private Activity mainActivity;
    private boolean connected = false;
    private final Handler mHandler;
    private static int RECIEVE_MESSAGE = 1;
    private ConnectedThread mConnectedThread;

    BTConnectionManager(Context context, String adress, Activity mainActivity, Handler handler) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.mHandler = handler;
        this.address = adress;
        Log.d(MainActivity.TAG, "Менеджер BTConnectionManager получен");
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (btAdapter == null) btAdapter = BluetoothAdapter.getDefaultAdapter();
        return btAdapter;
    }

    private BluetoothSocket getBtSocket() {
        if (btSocket == null) {
            BluetoothDevice device = getBluetoothAdapter().getRemoteDevice(address);
/// Socket!

            try {

                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                // Two things are needed to make a connection:
                //   A MAC address, which we got above.
                //   A Service ID or UUID.  In this case we are using the
                //     UUID for SPP.
            } catch (IOException e) {
                Log.d(MainActivity.TAG, "getBtSocket Exception");

                e.printStackTrace();
            }
        }
        return btSocket;
    }

    private Context getContext() {
        return context;
    }

    private Activity getMainActivity() {
        return mainActivity;
    }

    boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;

    }


    boolean connect() {
        getBluetoothAdapter();
        setConnected(false);

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
//        getBluetoothAdapter().cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(MainActivity.TAG, "Отключение поиска устройств(экономит ресурсы)");

        try {

            getBtSocket().connect();
            Log.d(MainActivity.TAG, "getBtSocket().connected();");

            mConnectedThread = null;
            mConnectedThread = new ConnectedThread(btSocket);
            Log.d(MainActivity.TAG, "mConnectedThread = new ConnectedThread(btSocket);");

            mConnectedThread.start();
            Log.d(MainActivity.TAG, "mConnectedThread.start();");

            setConnected(true);
            Log.d(MainActivity.TAG, "setConnected(true);");

            Log.d(MainActivity.TAG, "Подключено");

        } catch (IOException e) {
            try {
                btSocket.close();
                getMainActivity().recreate();
                Log.d(MainActivity.TAG, "Соединение не установлено - устройство не найдено (Сокет закрыт(IOException e) 7)");
                Toast.makeText(getContext(), "Соединение не установлено - устройство не найдено (Сокет закрыт(IOException e) 7)", Toast.LENGTH_SHORT).show();
                return false;
            } catch (IOException e2) {
                getMainActivity().recreate();
                Log.d(MainActivity.TAG, "Соединение не установлено! Ошибка создания сокета (IOException e) 4");
                Toast.makeText(getContext(), "Соединение не установлено! Ошибка создания сокета (IOException e) 4", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
        // Create a data stream so we can talk to server.


//        try {
//            outStream = btSocket.getOutputStream();
//            setConnected(true);
//
//        } catch (IOException e) {
//            Toast.makeText(getContext(), "Соединение не установлено! Ошибка выходного потока (IOException e) 4", Toast.LENGTH_SHORT).show();
//            getMainActivity().recreate();
//            setConnected(false);
//        }
    }

    boolean write(String message) {
        return mConnectedThread.writeMessage(message);
    }

    void writeQuery() {
        if (mConnectedThread.writeMessage("*")) readBtData();
        Log.d(MainActivity.TAG, "Если записано 1, считать");
    }

    void readBtData() {
        mConnectedThread.read();
    }
    void clearBuffer(){
        if (mConnectedThread != null) mConnectedThread.clearBuffer();
        Log.d(MainActivity.TAG, "mConnectedThread.clearBuffer() - success!");
    }

    private class ConnectedThread extends Thread {
        BluetoothSocket socket;
        InputStream inpStream;
        OutputStream outStream;
        StringBuilder readMessage;

        ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            try {
                inpStream = socket.getInputStream();
                outStream = socket.getOutputStream();
                Log.d(MainActivity.TAG, "Сокеты inpStream outStream потоков получены");

            } catch (IOException e) {
                getMainActivity().recreate();
                Log.d(MainActivity.TAG, "Ошибка получения сокетов inpStream outStream");

                Toast.makeText(getContext(), "Ошибка получения потока из сокета" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void run() {

            readMessage = new StringBuilder();
            Log.d(MainActivity.TAG, "\\readMessage " + readMessage.toString());

            byte[] buffer;
            buffer = new byte[512];
            Log.d(MainActivity.TAG, "\\buffer " + buffer.toString());

            int bytes;
            while (true) {
                try {
                    // try read input data and buld into String
                    bytes = inpStream.read(buffer);
                    String readed = new String(buffer, 0, bytes);
                    Log.d(MainActivity.TAG, "readed " + readed.charAt(0));
                    if (readed.equals(" ")) {
                        count++;
                        Log.d(MainActivity.TAG, "Count="+count);
                        if (count >= 4) {

                            readed = "";
                            readMessage = null;
                            readMessage = new StringBuilder();
                            Log.d(MainActivity.TAG, "Пробел, count="+count);
                            count = 0;
                        }
                    }
                    if (readed.equals("*")) {
                        clearBuffer();

                    }

                    Log.d(TAG, "11Присоединил ");
                    readMessage.append(readed);

//                    Log.d(TAG, "\\readMessage.toString() "+readMessage.toString());
//                    read();
                    // mHandler.obtainMessage(RECIEVE_MESSAGE, readMessage.toString()).sendToTarget();    read in real time
                    Log.d(MainActivity.TAG, "Сообщение прочитао");

                } catch (IOException e) {
                    getMainActivity().recreate();
                    Log.d(MainActivity.TAG, "Ошибка чтения сообщений");

                    Toast.makeText(getContext(), "Соединение утрачено" + e.toString(), Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }

        void read() {
            mHandler.obtainMessage(RECIEVE_MESSAGE, readMessage.toString()).sendToTarget();
            Log.d(MainActivity.TAG, "Отправил сообщение хендлеру: " + readMessage.toString());
        }
        void clearBuffer(){
            readed = "";
            readMessage = null;
            readMessage = new StringBuilder();
            Log.d(MainActivity.TAG, "CLEAR ");
            count = 0;
        }

        boolean writeMessage(String message) {
            byte[] msgBuffer = message.getBytes();
            Log.d(MainActivity.TAG, "Создаем сообщение..");
            try {
                outStream.write(msgBuffer);
                Log.d(MainActivity.TAG, "Сообщение записано!");
            } catch (IOException e) {
                getMainActivity().recreate();
                Toast.makeText(getContext(), "Ошибка записи", Toast.LENGTH_SHORT).show();
                Log.d(MainActivity.TAG, "Ошибка записи сообщения");
            }
            return true;
        }
    }
}