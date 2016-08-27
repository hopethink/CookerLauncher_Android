package com.cooker.cookerlauncher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    View type_message_area;
    ImageView btn_send;
    EditText input_area;
    RelativeLayout msg_list;
    ScrollView scroll;

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

        btn_send.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String text = input_area.getText().toString();
        if(text.isEmpty())
            return;
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
    }
}
