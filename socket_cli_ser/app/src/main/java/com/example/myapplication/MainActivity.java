package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.TextView;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    Thread mClientThread = null;
    Thread mServiceThread = null;
    Socket mCliSocket =null;
    OutputStream os = null;
    EditText mEditText = null;
    Button mButton = null;
    private Handler mHandler = null;
    String tag = "ysfl";
    private Handler mMainHandler = null;
    EditText mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("ysfl","oncreate");
        mEditText = (EditText)findViewById(R.id.edit_texit);
        mButton = (Button) findViewById(R.id.button);
        mTextView = (EditText)findViewById(R.id.edit_texit2) ;
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEditText.getText()!= null){
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = mEditText.getText().toString();
                    Log.d(tag,"send msg:"+ msg);
                    if(mHandler!=null){
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });

        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(tag,"serviceTextview..show");
                if(msg!=null && msg.obj!=null){
                    mTextView.setText(null);
                    mTextView.setText(msg.obj.toString());
                }
            }
        };

        mServiceThread = new Thread() {
            ServerSocket ssSocket = null;
            Socket sSocket = null;
            @Override
            public void run() {
                try {
                    Log.d(tag,"mServiceThread: ");
                    ssSocket = new ServerSocket(9000);
                    sSocket = ssSocket.accept();
                    byte buffer[] = new byte[1024 * 4];
                    int temp = 0;
                    // 从InputStream当中读取客户端所发送的数据
                    while ((temp = sSocket.getInputStream().read(buffer)) != -1) {
                        Message msg = new Message();
                        msg.obj =  buffer[0];
                        mMainHandler.sendMessage(msg);
                        Log.d(tag,"mServiceThread: 00 temp: " +buffer[0]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(tag,"service excp...e: " + e);
                }

            }
        };
        mServiceThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread = new Thread(){
            private Looper looper;//取出该子线程的Looper
            @Override
            public void run() {
                Log.d(tag,"clithread..run");
                try {
                    Log.d(tag,"clithread..run..00 InetAddress.getLocalHost: " + InetAddress.getLocalHost());
                    //mCliSocket = new Socket("172.16.99.104", 9000);
                    mCliSocket = new Socket(InetAddress.getLocalHost(), 9000);
                    Log.d(tag,"clithread..run..11");
                    os = mCliSocket.getOutputStream();
                    os.write(1);
                    Log.d(tag,"socket...outstream..");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(tag,"excep...e: " + e);
                }

                Looper.prepare();
                mHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        //完成了获取北京天气的操作；
                        Log.i(tag, "handler msg: " +msg);
                        if(msg!=null){
                            Log.i(tag, "handler msg.obj: " +msg.obj);
                            try {
                                os.write(Integer.valueOf(msg.obj.toString()));
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(tag,"handler socket send..e:"+e);
                            }

                        }
                    }
                };
                Looper.loop();
            }
        };
        mClientThread.start();


    }

}