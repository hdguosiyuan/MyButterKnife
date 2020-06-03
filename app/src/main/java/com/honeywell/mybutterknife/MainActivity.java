package com.honeywell.mybutterknife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.honeywell.annotation.BindView;
import com.honeywell.annotation.OnClick;
import com.honeywell.annotation.BindString;
import com.honeywell.butterknife.MyButterKnife;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bt1)
    Button bt1;

    @BindView(R.id.tv_test1)
    TextView tvTest1;

    @BindView(R.id.bt2)
    Button bt2;

    @BindView(R.id.tv_test2)
    TextView tvTest2;

    @BindString(R.string.test_text1)
    String test_text1;

    @BindString(R.string.test_text2)
    String test_text2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnife.bind(this);
    }

    @OnClick({R.id.bt1,R.id.bt2,R.id.bt3})
    public void onViewClicked(View view) {
        Log.d("gsy","onViewClicked");
        switch (view.getId()){
            case R.id.bt1:
                tvTest1.setText(test_text1);
                break;
            case R.id.bt2:
                tvTest2.setText(test_text2);
                break;
            case R.id.bt3:
                Intent intent = new Intent(this,TwoActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
