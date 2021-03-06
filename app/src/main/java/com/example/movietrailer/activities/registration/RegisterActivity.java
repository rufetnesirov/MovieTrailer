package com.example.movietrailer.activities.registration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import com.example.movietrailer.R;
import com.example.movietrailer.models.authentication.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaopiz.kprogresshud.KProgressHUD;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextInputEditText email, password, username;
    private Button signupButton;
    private Context context;
    private static final String TAG = "RegisterActivity";
    private String emailText, usernameText;
    private String passwordText = "";
    private KProgressHUD progressDialog;
    private FirebaseUser firebaseUser;
    private TextView signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;

        firebaseAuth = FirebaseAuth.getInstance();

        initializeWidgets();
        setupProgress();
        clickedSignIn();
        clickedSignUp();

    }

    private void getTexts() {

        emailText = email.getText().toString().trim();
        passwordText = password.getText().toString().trim();
        usernameText = username.getText().toString().trim();

        if (TextUtils.isEmpty(usernameText)) {
            username.setError("Write your username");
        } else if (usernameText.length() < 4) {
            username.setError("Required 4 and more characters");
        } else {
            usernameText = username.getText().toString().trim();
        }

        if (TextUtils.isEmpty(emailText)) {
            email.setError("Write your email");
        }else if (!PatternsCompat.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("It is not valid email");
        } else {
            usernameText = username.getText().toString().trim();
        }



    }

    private void clickedSignUp() {
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide keyboard
                hideKeyboard(v);
                getTexts();
                createUser(firebaseAuth);
            }
        });
    }

    private void hideKeyboard(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    private void initializeWidgets() {

        username = findViewById(R.id.edit_username);
        password = findViewById(R.id.edit_password);
        email = findViewById(R.id.edit_email);
        signupButton = findViewById(R.id.button_signup);
        signIn = findViewById(R.id.txt_signIn);

    }

    private void createUser(FirebaseAuth auth) {

        if (!TextUtils.isEmpty(emailText)
                && !TextUtils.isEmpty(usernameText)
                && passwordText.length() >= 6
                && PatternsCompat.EMAIL_ADDRESS.matcher(usernameText).matches()
                && usernameText.length() < 4) {

            showProgress();

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                hideProgress();
                                firebaseUser = auth.getCurrentUser();
                                sendEmailVerification();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgress();
                            Log.d(TAG, "onFailure: Reason -> " + e.getMessage());
                            Toasty.custom(context, e.getMessage(), R.drawable.ic_info, R.color.red, Toasty.LENGTH_LONG, true, true).show();
                        }
                    });

        }

    }

    private void sendEmailVerification() {

        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addNewUser(emailText, usernameText, passwordText);
                                Toasty.custom(context, getString(R.string.verification_sent), R.drawable.ic_verified, R.color.success, Toasty.LENGTH_LONG, true, true).show();
                            } else {
                                Toasty.custom(context, getString(R.string.verification_not_sent), R.drawable.ic_info, R.color.red, Toasty.LENGTH_LONG, true, true).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.custom(context, e.getMessage(), R.drawable.ic_info, R.color.red, Toasty.LENGTH_LONG, true, true).show();
                        }
                    });
        }

    }

    private void clickedSignIn() {
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, LoginActivity.class));
                finish();
            }
        });
    }

    private void addNewUser(String email, String username, String password) {

        String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        User user = new User(
                uID,
                email,
                password,
                username,
                "#FF6F00"
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: User informations added to database");
                        startActivity(new Intent(context, LoginActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Something went wrong");
                    }
                });

    }

    private void setupProgress() {

        progressDialog = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait...")
                .setCancellable(true)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);

    }

    private void showProgress() {

        progressDialog.show();

    }

    private void hideProgress() {
        progressDialog.dismiss();
    }

}