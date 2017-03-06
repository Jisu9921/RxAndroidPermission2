package com.android.soundlly.intern.jisu.rxandroidpermission;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import com.soundlly.standalone.sdk.Soundlly;
import com.soundlly.standalone.sdk.SoundllyResult;
import com.soundlly.standalone.sdk.SoundllyResultListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="TESTERR";
    private static final String RECORD_DENIED="거부";
    private static final String RECORD_GRANTED="승인";
    private static final String SOUNDLLY_APP_KEY="f01b8843-ac7a-49ff-a0fc-1a31c93ac1a9";
    private boolean isSoundllyInit = false;

    TextView micPermissionState,sdkInitState;
    Button micPermissionBtn,sdkInitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        micPermissionState = (TextView) findViewById(R.id.mic_permission_state);
        sdkInitState = (TextView) findViewById(R.id.sdk_init_state);
        micPermissionBtn = (Button) findViewById(R.id.mic_permission_btn);
        sdkInitBtn = (Button) findViewById(R.id.sdk_init_btn);

        Observable.just(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO))
                .flatMap(result -> {
                    if(result== PackageManager.PERMISSION_GRANTED){
                        return Observable.just(RECORD_GRANTED);
                    }
                        return Observable.just(RECORD_DENIED);
                    })
                .subscribe(text->micPermissionState.setText(text));

       Observable.just(isSoundllyInit)
               .flatMap(result -> {
                   if(result){
                    return Observable.just("성공");
                   }
                   return Observable.just("");
               })
               .subscribe(text->sdkInitState.setText(text));

        sdkInitBtn.setOnClickListener(v->{
            Observable.just(soundllyInit())
                    .subscribe(text->sdkInitState.setText(text));
        });

        micPermissionBtn.setOnClickListener(v -> {
            Observable.just(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO))
                        .flatMap(result->{
                            if(result==PackageManager.PERMISSION_DENIED){
                                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},1);
                                return Observable.just("");
                            }else{
                                return Observable.just("마이크 권한 승인 상태 입니다");
                            }
                        })
                        .filter(no -> no!="")
                        .subscribe(msg->Toast.makeText(this,msg,Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "마이크승인", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "권한 요청 거부 ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private String soundllyInit(){
        int ret = Soundlly.init(this, SOUNDLLY_APP_KEY, true, new SoundllyResultListener() {
            @Override
            public void onInitialized() {
                Log.d("TAG", "SoundllySDK initialized");
                isSoundllyInit = true;
            }

            @Override
            public void onError(int error) {
                Log.d("TAG", "SoundllySDK error : " + error);

            }

            @Override
            public void onStateChanged(int state) {
                switch (state) {
                    case SoundllyResultListener.STARTED:
                        Log.d(TAG, "Soundlly receiving started");
                        break;
                    case SoundllyResultListener.STOPPED:
                        Log.d(TAG, "Soundlly receiving stopped");
                        break;
                    case SoundllyResultListener.DETECT_SHAKING:
                        Log.d(TAG, "Soundlly shaking detected");
                        Soundlly.startDetect();
                        break;
                }

            }

            @Override
            public void onResult(int i, SoundllyResult soundllyResult) {

            }
        });

        if(ret != Soundlly.SUCCESS){
            if(ret == Soundlly.INVALID_ARGUMENTS){
                Log.e(TAG, "Soundlly init error : appkey is null");
                return "INVALIT ARGUMENTS";
            }else if(ret == Soundlly.MIC_PERMISSION_DENIED){
                Log.e(TAG, "Soundlly init error : mic permission denied");
                return "MIC PERMISSION DENIED";
            }
        }else if(ret == Soundlly.SUCCESS){
            return "Success";
        }
        return "faile";
    }
}
