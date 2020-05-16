package com.songjinghao.apt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.songjinghao.annotation.ARouter;
import com.songjinghao.annotation.BindView;
import com.songjinghao.annotation.HelloWorld;
import com.songjinghao.library.ButterKnife;
import com.songjinghao.library.ViewBinder;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        tv.setText("APT is Cool!");
    }

//    @HelloWorld()
    public void hello() {

    }
}
