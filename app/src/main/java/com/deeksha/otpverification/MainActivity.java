package com.deeksha.otpverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Button btnGenerateOtp, btnSignIn;
    EditText phoneNumber, Otp;
    TextView timer;
    Spinner spinner;

    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String verificationCodeSent;
    String getPhoneNumber, getOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);
        btnGenerateOtp = findViewById(R.id.btn_generate_otp);
        btnSignIn = findViewById(R.id.btn_sign_in);

        phoneNumber = findViewById(R.id.phoneEditText);
        Otp = findViewById(R.id.otpEditText);
        timer = findViewById(R.id.timer);

        ArrayAdapter<String> countryCodes = new ArrayAdapter<String>(this,
                R.layout.spinner_item, CountryDetails.countryCodes);

        spinner.setAdapter(countryCodes);
        firebaseLogin();

        btnGenerateOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String spinnerText = spinner.getSelectedItem().toString();
                String phone = phoneNumber.getText().toString();

                if (phone == null || phone.trim().isEmpty()) {
                    phoneNumber.setError("Provide Phone Number");
                    return;
                }

                getPhoneNumber = spinnerText + phone;
                btnSignIn.setVisibility(View.VISIBLE);
                Otp.setVisibility(View.VISIBLE);

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        getPhoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        MainActivity.this,
                        callbacks

                );

                startTimer(60 * 1000, 1000);
                btnGenerateOtp.setVisibility(View.INVISIBLE);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOtp = Otp.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(
                        verificationCodeSent,getOtp
                );

                SignInWithPhoneNumber(credential);
            }
        });

    }

    private void SignInWithPhoneNumber(PhoneAuthCredential credential) {

        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            startActivity(new Intent(MainActivity.this,
                                    SignIn.class));
                            finish();
                        }
                        else {
                            Toast.makeText(MainActivity.this,"Incorrect OTp",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startTimer(final long finish, final long tick) {

        timer.setVisibility(View.VISIBLE);
        CountDownTimer countDownTimer;

        countDownTimer = new CountDownTimer(finish, tick) {
            @Override
            public void onTick(long l) {
                long remindSec = l / 1000;
                timer.setText("Retry after" + (remindSec / 60)
                        + ":" + (remindSec % 60));
            }

            @Override
            public void onFinish() {
                btnGenerateOtp.setText("Re-generate OTP");
                btnGenerateOtp.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Finish",
                        Toast.LENGTH_LONG).show();

                timer.setVisibility(View.INVISIBLE);
                cancel();
            }
        }.start();

    }

    private void firebaseLogin() {
        auth = FirebaseAuth.getInstance();

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(MainActivity.this, "Verification Completed",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(MainActivity.this, "Verification Failed",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCodeSent = s;
                Toast.makeText(MainActivity.this, "Code Sent",
                        Toast.LENGTH_LONG).show();

            }
        };
    }
}