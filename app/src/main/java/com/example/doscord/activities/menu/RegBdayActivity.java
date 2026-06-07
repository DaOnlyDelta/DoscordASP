package com.example.doscord.activities.menu;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doscord.R;
import com.example.doscord.api.RegisterRequest;
import com.example.doscord.api.RegisterResponse;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.RegDataHolder;
import com.example.doscord.utils.SessionManager;
import com.google.gson.Gson;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegBdayActivity extends AppCompatActivity {

    private EditText bdayInput;
    private TextView warningTxt, serverWarningTxt;
    private Button createBtn;
    private boolean midRequest = false, ignoreFirstPass = true;
    private Calendar c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg_bday);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bdayListener();
    }

    private void initViews() {
        bdayInput = findViewById(R.id.regBdayInput);
        warningTxt = findViewById(R.id.regBdayWarning);
        serverWarningTxt = findViewById(R.id.regBdayServerWarning);
        createBtn = findViewById(R.id.crProfileEditBtn);
        c  = Calendar.getInstance();

        if (RegDataHolder.year == null) {
            setCurrentDate();
        } else {
            c.set(RegDataHolder.year, RegDataHolder.month, RegDataHolder.day);
        }

        Helpers.setDate(bdayInput);
        updateButton();
    }

    private void setCurrentDate() {
        RegDataHolder.year = c.get(Calendar.YEAR);
        RegDataHolder.month = c.get(Calendar.MONTH);
        RegDataHolder.day = c.get(Calendar.DAY_OF_MONTH);
    }

    private void bdayListener() {
        bdayInput.setOnClickListener(v -> {
            if (midRequest) { return; }

            // Create the Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    R.style.MyDatePickerStyle,
                    (view, year, month, day) -> {
                        c.set(year, month, day);
                        RegDataHolder.year = year;
                        RegDataHolder.month = month;
                        RegDataHolder.day = day;

                        Helpers.setDate(bdayInput);
                        updateButton();
                    },
                    RegDataHolder.year, RegDataHolder.month, RegDataHolder.day);

            datePickerDialog.show();
        });
    }

    private void updateButton() {
        // Check for age
        if (isOldEnough()) {
            createBtn.setAlpha(1.0f);
            createBtn.setEnabled(true);
        } else {
            createBtn.setAlpha(0.5f);
            createBtn.setEnabled(false);
        }
    }

    private boolean isOldEnough() {
        if (ignoreFirstPass) {
            warningTxt.setVisibility(View.GONE);
            ignoreFirstPass = false;
            return false;
        }

        Calendar minAgeCalendar = Calendar.getInstance();
        minAgeCalendar.add(Calendar.YEAR, -13);
        boolean pass = c.before(minAgeCalendar) || c.equals(minAgeCalendar);
        warningTxt.setVisibility((pass) ? View.GONE : View.VISIBLE);
        return pass;
    }

    public void registerReq(View v) {
        RegDataHolder.errorCode = 0;

        // Set up UI for loading state
        midRequest = true;
        Helpers.startDotsAnimation(this, createBtn, bdayInput, null);

        // Create the request object
        RegisterRequest request = new RegisterRequest();

        // Send to Pi
        RetrofitClient.getApiService().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegDataHolder.id = response.body().getId();
                    String receivedToken = response.body().getToken(); // Grab the 64-char string

                    // Save it to the phone disk
                    SessionManager sessionManager = new SessionManager(getApplicationContext());
                    sessionManager.saveLoginSession(receivedToken);
                    handleRegistrationError(1);
                } else {
                    // Handle Error Codes
                    serverWarningTxt.setVisibility(View.GONE);
                    Helpers.resetUI(RegBdayActivity.this, createBtn, bdayInput, null);
                    midRequest = true;
                    try {
                        // Retrofit won't automatically parse the body on a 400 error,
                        // so we manually convert the errorBody to our object
                        assert response.errorBody() != null;
                        RegisterResponse errorRes = new Gson().fromJson(response.errorBody().charStream(), RegisterResponse.class);
                        handleRegistrationError(errorRes.getErrorCode());
                    } catch (Exception e) {
                        handleRegistrationError(999); // Generic error
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                serverWarningTxt.setVisibility(View.VISIBLE);
                Helpers.resetUI(RegBdayActivity.this, createBtn, bdayInput, null);
            }
        });
    }

    private void handleRegistrationError(int code) {
        RegDataHolder.errorCode = code;

        switch (code) {
            case 1:
                RegDataHolder.registered = true;
            case 102:
                finish();
                break;
            case 999:
                createBtn.setText(R.string.generic_server_error);
                break;
        }
    }

    public void finish(View v) {
        finish();
    }
}