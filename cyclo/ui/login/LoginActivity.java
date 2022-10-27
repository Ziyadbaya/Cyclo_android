package com.dev.cyclo.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;
import com.dev.cyclo.Menu;
import com.dev.cyclo.R;
import com.dev.cyclo.realm.MongoRequest;

import java.io.ByteArrayOutputStream;


/**
 * Manage the login page
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean online = true; //if the user want to login with mongoDB
    public static MongoRequest mongo = null;
    private static final int RESULT_LOAD_IMAGE = 1;
    private Button loginButton;
    private Button createAccountButton;
    private Button sendPasswordButton;
    private LinearLayout loginLayout;
    private LinearLayout createAccountLayout;

    private LoginViewModel loginViewModel;

    private Logger logger;

    Button bUpload;
    ImageView iUpload;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        logger = new Logger(this.getClass());
        logger.log(Logger.Severity.Debug, "new login activity");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        online = true;

        setContentView(R.layout.activity_login);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        loginButton = findViewById(R.id.login);
        createAccountButton = findViewById(R.id.createAccount);
        createAccountButton.setEnabled(true);
        sendPasswordButton = findViewById(R.id.sendPassword);
        loginLayout = findViewById(R.id.loginLayout);
        createAccountLayout = findViewById(R.id.createAccountLayout);
        ImageView fond = findViewById(R.id.fond);
        iUpload = findViewById(R.id.iUpload);
        bUpload = findViewById(R.id.bUpload);

        EditText usernameEditText = findViewById(R.id.username);
        EditText passwordEditText = findViewById(R.id.password);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        fond.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        });

        createAccountLayout.setOnClickListener(view -> {
            createAccountLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
            createAccountButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            iUpload.setVisibility(View.VISIBLE);
            bUpload.setVisibility(View.VISIBLE);
        });
        loginLayout.setOnClickListener(view -> {
            iUpload.setVisibility(View.GONE);
            bUpload.setVisibility(View.GONE);
            loginLayout.setVisibility(View.GONE);
            createAccountLayout.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            if (passwordEditText.getVisibility() == View.VISIBLE) {
                createAccountButton.setVisibility(View.GONE);
            } else {
                passwordEditText.setVisibility(View.VISIBLE);
                sendPasswordButton.setVisibility(View.GONE);
            }
        });

        createAccountButton.setOnClickListener(view -> addUser());

        iUpload.setOnClickListener(this);
        bUpload.setOnClickListener(this);


        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
            }
            setResult(Activity.RESULT_OK);

            //Complete and destroy login activity once successful
            finish();
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnKeyListener((v, keyCode, event) -> event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER);


        loginButton.setOnClickListener(view -> {
            startMongo();
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });
    }

    /** provide a local account
     * @param v the LoginActivity view
     */
    public void connectDefaultUser(View v) {
        online = false; //the user won't use the database
        loginViewModel.login("Cyclome", "JavaVelo");
    }

    /**
     * Create a new user on the database
     */
    private void addUser() {
        startMongo();

        EditText usernameEditText = findViewById(R.id.username);
        EditText passwordEditText = findViewById(R.id.password);
        ImageView iUpload = findViewById(R.id.iUpload);
        Bitmap image = ((BitmapDrawable) iUpload.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);


        if(mongo.addUser(encodedImage, usernameEditText.getText().toString(), passwordEditText.getText().toString())){
            loginViewModel.login(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            logger.log(Logger.Severity.Info, "Add " + usernameEditText.getText().toString() + " to database");
        }
        else {
            Toast.makeText(this, "Pseudo déj    à utilisé", Toast.LENGTH_LONG).show();
            usernameEditText.getText().clear();
            passwordEditText.getText().clear();
            }

    }

    public void onClick(View v){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        switch(v.getId()){
            case R.id.iUpload:
                break;
            case R.id.bUpload:
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            iUpload.setImageURI(selectedImage);
        }
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName() + " !";

        // If the login result is success :
        Intent intent = new Intent(this, Menu.class);
        intent.putExtra("UserView", model);
        startActivity(intent);

        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();

        //if the login failed, the login activity is restarted
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Set up the mongoDB client
     */
    private synchronized void startMongo() {

        if (mongo == null) {
            logger.log(Logger.Severity.Debug, "Start a new mongoDB client");
            mongo = new MongoRequest(Main.mainContext);
            logger.log(Logger.Severity.Debug, "end of starting mongo");
        }

    }

    /**
     * change the return button when the loginActivity is created by the menu (not by the main) after logout
     * normally, return button recreate the menu so to avoid this : close the app
     */
    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }
}