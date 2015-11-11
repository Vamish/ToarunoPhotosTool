package cn.diviniti.toarunophotostool;

import android.app.Activity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Button;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.os.Bundle;
import android.graphics.PixelFormat;
import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;

import java.io.File;

import android.text.format.DateFormat;

import java.security.Policy;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Timer;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import android.graphics.PixelFormat;

import java.util.TimerTask;

import android.view.KeyEvent;


public class TakeCamera extends Activity implements SurfaceHolder.Callback {
    private Button btn_take;//创建一个Button控件对象
    private SurfaceView surfaceView = null;//创建一个空的SurfaceView控件对象
    private SurfaceHolder surfaceHolder = null;//创建一个SurfaceHolder控件对象
    private Camera camera = null;//创建一个空的Camera对象
    private boolean previewRunning = false;//预览状态

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFormat(PixelFormat.TRANSLUCENT);//窗口设置为半透明
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//窗口去掉标题
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//窗口设置为全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//设置不熄屏
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 调用setRequestedOrientation来翻转Preview
        this.setContentView(R.layout.camera);

        surfaceView = (SurfaceView) this.findViewById(R.id.camera_surface);//实例化SurfaceView对象
        surfaceHolder = surfaceView.getHolder();//获取SurfaceHolder
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置缓存类型

        btn_take = (Button) this.findViewById(R.id.take);
        btn_take.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (camera != null) {       //当按下按钮时，执行相机对象的takePicture()方法，该方法有三个回调函数作为入参，不需要时可以设为null
                            camera.takePicture(null, null, jpegCallback);
                        }
                    }
                }, 0, 1000);//put here time 1000 milliseconds=1 second
            }
        });
    }

    /**
     * 延迟方法
     *
     * @param time 毫秒
     */
    public void changeByTime(long time) {
        final Timer timer = new Timer();
        final Handler handle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        stopCamera();//停止Camera方法
                        pepareCamera();//调用初始化Camera方法
                        timer.cancel();//撤销计时器
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handle.sendMessage(message);
            }
        };
        timer.schedule(task, time);//每隔time时间执行TimerTash类中的Run方法
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//判断手机按键按下的是否是拍照键或者轨迹球键
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (camera != null) {
//当按下按钮时，执行相机对象的takePicture()方法，该方法有三个回调函数作为入参，不需要时可以设为null
                camera.takePicture(null, null, jpegCallback);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 开始Camera
     */
    public void startCamera() {
        if (previewRunning)//判断预览开启
        {
            camera.stopPreview();//停止预览
        }
        try {
            Camera.Parameters parameters = camera.getParameters();//获取相机参数对象
            parameters.setPictureFormat(PixelFormat.JPEG);//设置格式
            //设置自动对焦
            parameters.setFocusMode("auto");
            //设置图片保存时的分辨率大小
            parameters.setPictureSize(2048, 1536);
            //MX2 夜间模式，虽然设置不上
            parameters.setSceneMode("night-shot");

            camera.setParameters(parameters);//给相机对象设置刚才设定的参数
            Camera.Parameters te = camera.getParameters();
            List<String> list = te.getSupportedSceneModes();
            for (String item : list) {
                Log.d("VANGO_DEBUG", item);
            }
            camera.setPreviewDisplay(surfaceHolder);//设置用SurfaceView作为承载镜头取景画面的显示
            camera.startPreview();//开始预览
            previewRunning = true;//设置预览状态为true
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化Camera
     */
    public void pepareCamera() {
        camera = Camera.open();//初始化Camera
        try {
            camera.setPreviewDisplay(surfaceHolder);//设置预览
        } catch (IOException e) {
            camera.release();//释放相机资源
            camera = null;//置空Camera对象
        }
    }

    /**
     * 停止Camera
     */
    public void stopCamera()//判断Camera对象不为空
    {
        if (camera != null) {
            camera.stopPreview();//停止预览
            camera.release();//释放摄像头资源
            camera = null;//置空Camera对象
            previewRunning = false;//设置预览状态为false
        }
    }

    /**
     * 拍摄之后的事件
     */
    private PictureCallback jpegCallback = new PictureCallback() {//拍照时调用
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
//获取SD卡的根目录
            String sdCard = Environment.getExternalStorageDirectory().getPath();
//获取相片存放位置目录
            String dirFilePath = sdCard + File.separator + "MyCamera";
//获取当期时间的自定义字符串
            String date = DateFormat.format("yyyy-MM-dd hh-mm-ss", new Date()).toString();
//onPictureTaken传入的第一个参数即为图片的byte,实例化BitMap对象
            Bitmap bitMap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
            try {
//创建相片存放位置的File对象
                File dirFile = new File(dirFilePath);
                if (!dirFile.exists()) {
                    dirFile.mkdir();//创建文件夹
                }
//文件的格式
//创建一个前缀为photo，后缀为.jpg的图片文件，CreateTempFile是为了避免重复冲突
                File file = File.createTempFile("photo-", ".jpg", dirFile);
                BufferedOutputStream bOutputStream = new BufferedOutputStream(new FileOutputStream(file));
//采用压缩文件的方法
                bitMap.compress(Bitmap.CompressFormat.JPEG, 80, bOutputStream);
//清除缓存，更新BufferedOutputStream
                bOutputStream.flush();
//关闭BufferedOutputStream
                bOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 初次实例化，预览界面被创建时，该方法被调用
     */
    public void surfaceCreated(SurfaceHolder arg0) {
        pepareCamera();//调用初始化Camera
    }

    /**
     * 当预览的格式和大小发生改变时，该方法被调用
     */
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        startCamera();//调用开始Camera方法
    }

    /**
     * 预览界面被关闭该方法被调用
     */
    public void surfaceDestroyed(SurfaceHolder arg0) {
        stopCamera();//调用停止Camera方法
    }

}