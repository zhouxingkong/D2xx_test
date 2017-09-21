package com.android.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    USBUtil usbUtil;
    Button btn_open;
    Button btn_read;
    Button btn_write;
    EditText dataOut;
    TextView terminal;
    char [] term_data;
    int term_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        usbUtil=new USBUtil();
        usbUtil.getInstance(this);
        term_data=new char[10000];
        term_size=0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_open=(Button)findViewById(R.id.search);
        btn_read=(Button)findViewById(R.id.read);
        btn_write=(Button)findViewById(R.id.write);
        dataOut=(EditText)findViewById(R.id.data_out);
        terminal=(TextView)findViewById(R.id.terminal);

        btn_open.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                usbUtil.openDevice(MainActivity.this,mHandler);
            }
        });
        btn_read.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                usbUtil.readDevice(mHandler);
                term_size=0;
                SetText();
            }
        });
        btn_write.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                byte [] b=dataOut.getText().toString().getBytes();
                //byte b=(byte)0xaa;
                //System.out.println("获取byte="+b[0]);
//                int l=(b.length)/2;
//                byte [] out_data=new byte[l];
//                for(int i=0;i<l;i++) {
//                    out_data[i] = (byte) ((trans(b[2*i]) << 4) + trans(b[2*i+]));
//                    //System.out.println("得到的数字是"+o);
//                }
                byte [] out_data=new byte[4];
                out_data[0]=(byte)0xaa;
                out_data[1]=(byte)0xab;
                out_data[2]=(byte)0xac;
                out_data[3]=(byte)0xad;
                //System.out.println(out_data.length);
                usbUtil.writeDevice(out_data);
                thread.start();
            }
        });


        IntentFilter filter = new IntentFilter();	//注册监听USB事件的广播
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        this.registerReceiver(mUsbReceiver, filter);
    }
    public byte trans(byte c){
        byte b=0;
        if(c<58){
            b=(byte)(c-48);
        }
        else{
            b=(byte)(c-87);
        }
        return b;
    }
    public char byte2char(byte b){
        char c=0;
        if(b<10){
            c=(char)(b+48);
        }
        else{
            c=(char)(b+87);
        }
        return c;
    }

    protected void onDestroy() {
        this.unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();

    }
    public void SetText(){
        byte[] read_data=usbUtil.getByte();
        for(int i=0;i<usbUtil.get_length();i++){
            term_data[term_size]=byte2char((byte)(read_data[i]>>4));
            term_data[term_size+1]=byte2char((byte)(read_data[i]&0x0f));
            term_data[term_size+2]=' ';
            term_size+=3;
        }
        terminal.setText(term_data,0,term_size);
    }

    Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {//覆盖handleMessage方法
            switch (msg.what) {//根据收到的消息的what类型处理
                case 0:
                    SetText();
                    break;
                default:
                    super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
                    break;
            }
        }
    };

    /***********USB broadcast receiver*******************************************/
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String TAG = "FragL";
            String action = intent.getAction();
            if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.i(TAG,"DETACHED...");

            }
            if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.i(TAG,"接入广播...");
            }
        }
    };

    Thread thread=new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            while(true){
//                System.out.println("一秒传输"+usbUtil.actual_length);
                Log.e("shit", "一秒传输"+usbUtil.actual_length);
                usbUtil.actual_length=0;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    });

}
