package com.example.urldetectorappwithnotification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID ="Message Channel";
    public static final int REQ_CODE=100;
    public static final int MESSAGE_ID =100;
    EditText url;
    Button detect;
    Button readsms;
    TextView result;
    TextView link;

    private static final int MY_PERMISSION_REQUEST_RECIEVE_SMS = 0;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    TextView messageTV, numberTV;

    MyReceiver receiver = new MyReceiver(){
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            super.onReceive(context, intent);
            messageTV.setText(msg);
            numberTV.setText(phoneNo);

            if(!Python.isStarted())
                Python.start(new AndroidPlatform(context));

            Python py= Python.getInstance();
            final PyObject pyobj = py.getModule("script");   //here we will give name of our python file
            String stringMessage = messageTV.getText().toString();
            String word[] = stringMessage.split(" ");
            boolean flag= false;
            int i;
            for(i=0;i< word.length;i++){
                if(word[i].contains("https")){
                    flag=true;
                }
            }
            if(!flag){
                result.setText("This message contains no URL");
            }
            else {
                PyObject obj=pyobj.callAttr("main",url.getText().toString());
                result.setText(obj.toString());
            }
            Drawable drawable= ResourcesCompat.getDrawable(getResources(), R.drawable.largeicon, null);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap largeIcon = bitmapDrawable.getBitmap();

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification;
            Intent iNotify = new Intent(getApplicationContext(), MainActivity.class);
            iNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(context,REQ_CODE,iNotify,PendingIntent.FLAG_UPDATE_CURRENT);

            //BIG PICTURE STYLE
            Notification.BigPictureStyle bigPictureStyle=new Notification.BigPictureStyle()
                    .bigPicture(((BitmapDrawable)(ResourcesCompat.getDrawable(getResources(),R.drawable.largeicon,null))).getBitmap())
                    .bigLargeIcon(largeIcon)
                    .setBigContentTitle("Image sent by Raman")
                    .setSummaryText("Image Message");

            //INBOX STYLE
            Notification.InboxStyle inboxStyle=new Notification.InboxStyle()
                    .addLine(msg)
                    .setBigContentTitle(result.getText().toString())
                    .setSummaryText("Message From: "+numberTV.getText().toString());

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notification = new Notification.Builder(context)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.drawable.largeicon)
                        .setContentText(result.getText().toString())
                        .setSubText("New message from: "+numberTV.getText().toString())
                        .setContentIntent(pi)
                        .setStyle(inboxStyle)
                        .setChannelId(CHANNEL_ID)
                        .build();
                nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "New Channel", NotificationManager.IMPORTANCE_HIGH ));
            }else{
                notification = new Notification.Builder(context)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.drawable.largeicon)
                        .setContentText(result.getText().toString())
                        .setSubText("New message from: "+numberTV.getText().toString())
                        .setStyle(inboxStyle)
                        .setContentIntent(pi)
                        .build();
            }
            nm.notify(MESSAGE_ID, notification);
        }
    };

        @Override
        protected void onResume() {
            super.onResume();
            registerReceiver(receiver, new IntentFilter(SMS_RECEIVED));
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            unregisterReceiver(receiver);
        }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        messageTV = findViewById(R.id.message);
        numberTV = findViewById(R.id.number);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECEIVE_SMS)){
                //do nothing
            }
            else{
                //a pop up will appear
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSION_REQUEST_RECIEVE_SMS);

            }
        }
        url = findViewById(R.id.url);
        detect = findViewById(R.id.detect);
        result = findViewById(R.id.result);
        readsms = findViewById(R.id.readsms);
        link = findViewById(R.id.link);
        link.setMovementMethod(LinkMovementMethod.getInstance());

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_SMS},PackageManager.PERMISSION_GRANTED);

        if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));

        Python py= Python.getInstance();
        final PyObject pyobj = py.getModule("script");   //here we will give name of our python file

            detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               PyObject obj=pyobj.callAttr("main",url.getText().toString());
               result.setText(obj.toString());
            }
        });
    }

    //after getting the result of permission requests the result will be passed through this method
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        //will check the request code
        switch(requestCode){
            case MY_PERMISSION_REQUEST_RECIEVE_SMS:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Thank you for permitting!",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "Well I can't do anything untill you permit me", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void Read_SMS(View view){

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms"),null,null,null,null);
        cursor.moveToFirst();
        if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));

        Python py= Python.getInstance();
        final PyObject pyobj = py.getModule("script");   //here we will give name of our python file
        boolean flag=false;
        String stringMessage = cursor.getString(12);
        String word[] = stringMessage.split(" ");
        int i;
        for(i=0;i< word.length;i++){
            if(word[i].contains("https")){
                url.setText(word[i]);
                flag = true;
            }
        }
        if(!flag){
            result.setText("This message contains no URL");
        }
        else {
            PyObject obj=pyobj.callAttr("main",url.getText().toString());
            result.setText(obj.toString());
        }
    }
}