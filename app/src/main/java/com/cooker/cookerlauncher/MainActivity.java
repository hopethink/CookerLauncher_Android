package com.cooker.cookerlauncher;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    View type_message_area;
    ImageView btn_send;
    EditText input_area;
    RelativeLayout msg_list;
    ScrollView scroll;
    private ProgressDialog pDialog;
    Cursor cursor;

    int previousID = -1;

    String hihi = "나라야 안녕!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        type_message_area =  findViewById(R.id.type_message_area);
        btn_send = (ImageView)type_message_area.findViewById(R.id.btn_send);
        input_area = (EditText)type_message_area.findViewById(R.id.input_area);

        msg_list = (RelativeLayout)findViewById(R.id.msg_list);
        scroll = (ScrollView)findViewById(R.id.msg_scroll);

        scroll.getChildAt(0).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input_area.getWindowToken(), 0);
            }
        });
    }

    // 메시지 전송버튼 클릭
    public void onSendBtnClick(View view) {
        String text = input_area.getText().toString();
        if(text.isEmpty()) return;
        View message1 = LayoutInflater.from(this).inflate(R.layout.message1, null);
        TextView msg = (TextView) message1.findViewById(R.id.textView);

        msg_list.addView(message1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int newId = View.generateViewId();
        if(previousID != -1)
            params.addRule(RelativeLayout.BELOW, previousID);
        previousID = newId;
        message1.setLayoutParams(params);
        message1.setId(newId);
        msg.setText(text);
        input_area.setText("");
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });

        // 이름으로 전화걸기
        String nameToFind = text.split(" ")[0];
        findNumberByName(nameToFind);

    }

    private void findNumberByName(final String name) {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Finding " + name + "...");
        pDialog.setCancelable(false);
        pDialog.show();

        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                String phone = getContacts(name);
                pDialog.cancel();
                if (phone == null) {
//                    Toast.makeText(getApplicationContext(), "No Result", Toast.LENGTH_SHORT).show();
                } else {
                    Intent myIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                    startActivity(myIntent);
                }
            }
        }).start();
    }

    public String getContacts(String paramName) {

        String phoneNumber;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));

                if (!isMatchUserName(name, paramName)) continue;

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                if (hasPhoneNumber > 0) {

                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        phoneCursor.close();
                        return phoneNumber;
                    }

                }
            }
        }
        return null;
    }

    private boolean isMatchUserName(String name, String paramName) {
        return name.toLowerCase().indexOf(paramName.toLowerCase()) >= 0 ? true : false;
    }
}
