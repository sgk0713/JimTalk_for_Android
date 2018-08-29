package co.example.jimmykim.jimtalk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private Button login;
    private Button signup;

    private EditText id;
    private EditText password;
    //로그인 관리
    private FirebaseAuth firebaseAuth;
    //로그인이 됐는지 안됐는지 체크해주는 부분
    private FirebaseAuth.AuthStateListener authStateListener;

    //원격으로 테마적용하기 위해 있어야함
    private FirebaseRemoteConfig firebaseRemoteConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseAuth.signOut();

        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        id = findViewById(R.id.et_login_id);
        password = findViewById(R.id.et_login_pw);



        login = findViewById(R.id.btn_login);
        signup = findViewById(R.id.btn_signup);

        login.setBackgroundColor(Color.parseColor(splash_background));
        signup.setBackgroundColor(Color.parseColor(splash_background));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEvent();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //로그인상태
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
               }else{
                    //로그아웃상태
                }
            }
        };

    }

    void loginEvent(){
        String idText = id.getText().toString();
        String pwText = password.getText().toString();
        if(idText.equals("")){
            showToast("아이디를 입력해주세요");
            id.requestFocus();
            return;
        }else if(pwText.equals("")){
            showToast("비밀번호를 입력해주세요");
            password.requestFocus();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            System.out.println("로그인 오류 : " + task.getException().getMessage());
                            showToast("아이디와 비밀번호를 다시 확인해주세요");
                            return;
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    void showToast(String str){
        Toast.makeText(LoginActivity.this, str, Toast.LENGTH_SHORT).show();
    }
}
