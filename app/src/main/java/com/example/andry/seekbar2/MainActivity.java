package com.example.andry.seekbar2;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, OnClickListener {


    private TextView txt_status, txt_data;
    private TextView text_indicator9500A, text_indicator9500B, text_indicator15;
    private Button btn_write, btn_read;
    public Button btn_connect;
    private SeekBar seekBarOver9500A, seekBarOver9500B, seekBarOver15;
    private EditText edit_mac;
    public static final String TAG = "MyLog";
    //    private static final String MAC_ADRESS = "20:16:10:25:32:78";
    private static final String MAC_ADRESS = "94:53:30:03:18:2e"; // pc

    final int RECIEVE_MESSAGE = 1;
    private int stepOver9500A = 500, stepOver9500B = 500, stepOver15 = 1;
    private int maxStepOver9500A = 9500, maxStepOver9500B = 9500, maxStepOver15 = 15;
    private int minStepOver9500A = 2000, minStepOver9500B = 2000, minStepOver15 = 0;
    private BTConnectionManager btManager;
    public Handler mHandler;
    public String inpString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initWidgets();

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.d(MainActivity.TAG, "Хендлер получил сообщение в Активити: " + msg.toString());

                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // если приняли сообщение в Handler
//                      //распарсить сообщение
                        inpString = msg.obj.toString().trim();
                        txt_data.setText("Ответ от Arduino: " + inpString);             // обновляем TextView
//
//                        if (inpString.length() > 6) {
//                            String getString = inpString.substring(1);

                        String splString[] = inpString.split(" ");
                        int hireA = 0;
                        int hireB = 0;
                        int hireC = 0;

                        Log.d(TAG, "!!!!!!!.Принимаем строку без обрезки:" + inpString + "...");
                        try {
                            hireA = Integer.parseInt(splString[0]);
                            hireB = Integer.parseInt(splString[1]);
                            hireC = Integer.parseInt(splString[2]);
                            Log.d(TAG, "...Строка hireA:" + hireA + "...");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                            try {
//                                hireB = Integer.parseInt(splString[1]);
//                                Log.d(TAG, "...Строка hireB:" + hireB + "...");
//                            } catch (Exception e){
//                                e.printStackTrace();
//                            }
//                            try {
//                                hireC = Integer.parseInt(splString[2]);
//                                Log.d(TAG, "...Строка hireC:" + hireC + "...");
//                            } catch (Exception e){
//                                e.printStackTrace();
//                            }
//                            int hireB = Integer.parseInt(splString[1]);
//                            Log.d(TAG, "...Строка hireB:" + hireB + "...");
//
//                            int hireC = Integer.parseInt(splString[2]);
//                            Log.d(TAG, "...Строка hireC:" + hireC + "...");

                        setSeeksBarProgress(hireA, seekBarOver9500A, text_indicator9500A, true);
                        setSeeksBarProgress(hireB, seekBarOver9500B, text_indicator9500B, true);
                        setSeeksBarProgress(hireC, seekBarOver15, text_indicator15, false);

                        Log.d(TAG, "!!!!!!!.Строка:" + inpString + "...");

                        Log.d(TAG, "...Строка:" + inpString + "...");
                        break;
                }
            }

            ;
        };
        btManager = new BTConnectionManager(this, edit_mac.getText().toString().toUpperCase(), MainActivity.this, mHandler);
        checkBt(btManager.getBluetoothAdapter());
    }

    private void initWidgets() {

        edit_mac = (EditText) findViewById(R.id.edit_mac);          // MAC-adress
        edit_mac.setText(MAC_ADRESS);

        txt_status = (TextView) findViewById(R.id.text_status);     // дстрока состояния
        txt_data = (TextView) findViewById(R.id.txt_data);          // для вывода данных, полученного от Arduino

        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(this);

        btn_write = (Button) findViewById(R.id.btn_write);
        btn_write.setOnClickListener(this);

        btn_read = (Button) findViewById(R.id.btn_read);
        btn_read.setOnClickListener(this);

        text_indicator9500A = (TextView) findViewById(R.id.text_indicator9500A);
        text_indicator9500A.setText(String.valueOf(2000));

        text_indicator9500B = (TextView) findViewById(R.id.text_indicator9500B);
        text_indicator9500B.setText(String.valueOf(2000));
        text_indicator15 = (TextView) findViewById(R.id.text_indicator15);
        text_indicator15.setText(String.valueOf(0));

        seekBarOver9500A = (SeekBar) findViewById(R.id.A);
        seekBarOver9500A.setMax((maxStepOver9500A - minStepOver9500A) / stepOver9500A);
        seekBarOver9500A.setOnSeekBarChangeListener(this);

        seekBarOver9500B = (SeekBar) findViewById(R.id.B);
        seekBarOver9500B.setMax((maxStepOver9500B - minStepOver9500B) / stepOver9500B);
        seekBarOver9500B.setOnSeekBarChangeListener(this);

        seekBarOver15 = (SeekBar) findViewById(R.id.C);
        seekBarOver15.setMax((maxStepOver15 - minStepOver15) / stepOver15);

        seekBarOver15.setOnSeekBarChangeListener(this);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.A:
                text_indicator9500A.setText(String.valueOf(minStepOver9500A + (progress * stepOver9500A)));
                break;
            case R.id.B:
                text_indicator9500B.setText(String.valueOf(minStepOver9500B + (progress * stepOver9500B)));
                break;
            case R.id.C:
                text_indicator15.setText(String.valueOf(minStepOver15 + (progress * stepOver15)));
                break;

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                txt_data.setText("btn_connect");
                btManager.connect();
                Log.d(MainActivity.TAG, "Пробуем подключиться...");
                if (btManager.isConnected())
                    txt_status.setText("Подключено");
                Log.d(MainActivity.TAG, "Подключено!");
                break;
            case R.id.btn_write:
                Log.d(MainActivity.TAG, "Пробуем записать...");

                if (btManager.isConnected()) {

                    String message = text_indicator9500A.getText().toString()
                            + " " + text_indicator9500B.getText().toString()
                            + " " + text_indicator15.getText().toString();
                    //btManager.write(message);
                    btManager.write(message);
                    Log.d(MainActivity.TAG, "Записываю..");

                } else {
                    Log.d(MainActivity.TAG, "Попытка неудачная - не подключено");
                    txt_data.setText("Setup connection with device before sending data");
                }
                break;
            case R.id.btn_read:
                Log.d(MainActivity.TAG, "Считать!");

                if (btManager.isConnected()) {
//                    btManager.clearBuffer();
                    btManager.readBtData();
                    btManager.clearBuffer();
                    Log.d(MainActivity.TAG, "Чмтаю..");

                } else {
                    Log.d(MainActivity.TAG, "Попытка неудачная - не подключено");
                    txt_data.setText("Setup connection with device before reading data");
                }
                break;
        }
    }

    private void setSeeksBarProgress(int hire, SeekBar seekBar, TextView txt_bar, Boolean transformVal) {
        txt_bar.setText("" + hire);
        if (transformVal) {
            int val = (hire - 2000) / 500;
            seekBar.setProgress(val);
        } else {
            seekBar.setProgress(hire);
        }
    }

    private void checkBt(BluetoothAdapter btAdapter) {
        if (btAdapter.isEnabled()) {
            txt_data.setText("bt enabled");
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1); // Enable bluetooth on device;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkBt(btManager.getBluetoothAdapter());
    }
}