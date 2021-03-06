package com.cooker.cookerlauncher;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    View type_message_area;
    ImageView btn_send;
    EditText input_area;
    RelativeLayout msg_list;
    ScrollView scroll;
    private ProgressDialog pDialog;
    Cursor cursor;
    static Map<String, String> appNameList = new HashMap<>();

    static {
        appNameList.put("카카오톡", "com.kakao.talk");
        appNameList.put("카톡", "com.kakao.talk");
        appNameList.put("ㅋㅌ", "com.kakao.talk");
    }

    int previousID = -1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        type_message_area = findViewById(R.id.type_message_area);
        btn_send = (ImageView) type_message_area.findViewById(R.id.btn_send);
        input_area = (EditText) type_message_area.findViewById(R.id.input_area);

        msg_list = (RelativeLayout) findViewById(R.id.msg_list);
        scroll = (ScrollView) findViewById(R.id.msg_scroll);

        scroll.getChildAt(0).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input_area.getWindowToken(), 0);
            }
        });




        // 텍스트 입력창 변화 감지
        input_area.addTextChangedListener(new TextWatcher() {

            @Override // 입력되는 텍스트에 변화가 있을 때
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().indexOf(" ") >= 0) return;

                if (s.toString().length() >= 2) {
//                    Toast.makeText(getApplicationContext(), s.toString(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override // 입력하기 전에
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override // 입력이 끝났을 때
            public void afterTextChanged(Editable editable) {

            }

        });

    }

    // 메시지 전송버튼 클릭
    public void onSendBtnClick(View view) {
        String text = input_area.getText().toString();
        if (text.isEmpty()) return;
        View message1 = LayoutInflater.from(this).inflate(R.layout.message1, null);
        TextView msg = (TextView) message1.findViewById(R.id.textView);

        msg_list.addView(message1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int newId = View.generateViewId();
        if (previousID != -1)
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

        // 문장 분석
        String[] words = text.split(" ");

        // 지도
        if (text.length() > 10) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.map.daum.net/actions/carRoute?startLoc=%ED%96%A5%EA%B5%B0%EC%9E%A0%EC%8B%A4%ED%83%80%EC%9B%8C&sxEnc=MMLNSO&syEnc=QNOPTRL&endLoc=%EB%B0%A9%EC%9D%B4%EC%82%AC%EA%B1%B0%EB%A6%AC&exEnc=MMRRQU&eyEnc=QNOQMRV&ids=P8145775%2CP11066894&service="));
            startActivity(intent);
            return;
        }

        switch (words.length) {
            case 2:
                switch (words[1]) {
                    case "전화":
                    case "call":
                        // 이름으로 전화걸기
                        String nameToFind = words[0];
                        findNumberByName(nameToFind);
                        break;
                    case "실행":
                        String appName = words[0];
                        if (getPackageList(appName)) {
                            Intent intent = getPackageManager().getLaunchIntentForPackage(appNameList.get(appName));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        break;
                }
                break;
        }
    }

    public boolean getPackageList(String appName) {
        String packageName;
        if ((packageName = appNameList.get(appName)) == null) return false;

        boolean isExist = false;
        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if (mApps.get(i).activityInfo.packageName.startsWith(packageName)) {
                    isExist = true;
                    break;
                }
            }
        } catch (Exception e) {
            isExist = false;
        }
        return isExist;
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
        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                if (!isMatchUserName(name, paramName)) continue;

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {

                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
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
