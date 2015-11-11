package cn.diviniti.toarunophotostool;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.content.Intent;


public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Button button = (Button) this.findViewById(R.id.camera_button);//实例化Button组件对象
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
// 为Button添加单击监听事件
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, TakeCamera.class);//指定intent对象启动的类
                MainActivity.this.startActivity(intent);//启动新的Activity
            }
        });
    }

}