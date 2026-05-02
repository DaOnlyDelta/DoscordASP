package com.example.doscord.activities.chatroom;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.doscord.R;
import com.example.doscord.api.ApiService;
import com.example.doscord.api.PfpRequest;
import com.example.doscord.api.RetrofitClient;
import com.example.doscord.utils.Helpers;
import com.example.doscord.utils.RegDataHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CrPfpActivity extends AppCompatActivity {

    private ImageButton mainPfpBtn; // Changed to ImageButton to match your XML
    private String currentSelectedPath = "defaults/defaults0.png";
    private TableLayout tableLayout;
    private Uri selectedImageUri = null; // Stores the local phone path
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cr_pfp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLauncher();
        initViews();
        setupAvatarGrid();
    }

    private void initLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();

                        // Check file size (e.g., limit to 5MB)
                        if (isImageTooLarge(selectedImageUri)) {
                            // Error: Image is too large (over 5MB)
                            selectedImageUri = null;
                            return;
                        }

                        // Update UI and class variable
                        currentSelectedPath = "custom"; // Mark that we aren't using a default icon
                        Glide.with(this)
                                .load(selectedImageUri)
                                .circleCrop()
                                .into(mainPfpBtn);
                    }
                }
        );
    }

    private void initViews() {
        mainPfpBtn = findViewById(R.id.crPfpBtn);
        tableLayout = findViewById(R.id.crPfpTableLayout);
        nextBtn = findViewById(R.id.crPfpNextBtn);

        // Set initial placeholder for the big button
        Glide.with(this)
                .load(R.drawable.icon)
                .circleCrop()
                .into(mainPfpBtn);
    }

    private void setupAvatarGrid() {
        int totalAvatars = 8;
        int columns = 4;
        TableRow currentRealRow = null;

        for (int i = 1; i <= totalAvatars; i++) {
            if ((i - 1) % columns == 0) {
                currentRealRow = new TableRow(this);
                currentRealRow.setGravity(Gravity.CENTER);
                tableLayout.addView(currentRealRow);
            }

            ImageView imageView = new ImageView(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    convertDpToPx(55),
                    convertDpToPx(55)
            );
            params.setMargins(40, 40, 40, 40);
            imageView.setLayoutParams(params);

            // Fetch drawable by name (defaults1, defaults2, etc.)
            int drawableId = getResources().getIdentifier("defaults" + i, "drawable", getPackageName());

            Glide.with(this)
                    .load(drawableId)
                    .circleCrop()
                    .into(imageView);

            final String pfpPath = "defaults/defaults" + i + ".png";
            final int selectedDrawableId = drawableId; // Final copy for the inner click listener

            imageView.setOnClickListener(v -> {
                // 1. Update the big preview button at the top
                Glide.with(this)
                        .load(selectedDrawableId)
                        .circleCrop()
                        .into(mainPfpBtn);

                // 2. Save selection to the class-level variable
                currentSelectedPath = pfpPath;
                RegDataHolder.defaultPfpDrawable = selectedDrawableId;

                // 3. Clear other borders and set the new one
                clearAllBorders(tableLayout);
                v.setPadding(convertDpToPx(3), convertDpToPx(3), convertDpToPx(3), convertDpToPx(3));
                v.setBackgroundResource(R.drawable.pfp_border);
            });

            currentRealRow.addView(imageView);
        }
    }

    private int convertDpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void clearAllBorders(TableLayout table) {
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                row.getChildAt(j).setBackground(null);
                row.getChildAt(j).setPadding(0, 0, 0, 0);
            }
        }
    }

    public void skip(View v) {
        Intent intent = new Intent(this, CrMainActivity.class);
        startActivity(intent);
        finish();
    }

    public void openLauncher(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private boolean isImageTooLarge(Uri uri) {
        try {
            AssetFileDescriptor fd = getContentResolver().openAssetFileDescriptor(uri, "r");
            assert fd != null;
            long fileSize = fd.getLength();
            fd.close();
            return fileSize > 5 * 1024 * 1024; // 5MB limit
        } catch (Exception e) {
            return true;
        }
    }

    public void sendPfpToServer(View v) {
        Helpers.startDotsAnimation(this, nextBtn);
        ApiService apiService = RetrofitClient.getApiService();
        String userId = String.valueOf(RegDataHolder.id);

        // Case 1: User hasn't changed anything (still defaults0)
        // We just treat this as a skip.
        if (currentSelectedPath.equals("defaults/defaults0.png")) {
            skip(null);
            return;
        }

        if (currentSelectedPath.equals("custom")) {
            // --- CUSTOM GALLERY UPLOAD ---
            if (selectedImageUri == null) {
                // Error: No image to upload
                Helpers.resetUI(this, nextBtn, null, null);
                return;
            }

            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                if (is == null) return; // Error: Image gone
                byte[] bytes = getBytes(is);

                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("pfp", "upload.jpg", requestFile);
                RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), userId);

                apiService.updatePfpCustom(userIdPart, body).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            RegDataHolder.selectedImageUri = selectedImageUri;
                            skip(null); // Success! Move to next screen
                        } else {
                            // Error: Server returned status like 500
                            Helpers.resetUI(CrPfpActivity.this, nextBtn, null, null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        // Error: Network failure
                        Helpers.resetUI(CrPfpActivity.this, nextBtn, null, null);
                    }
                });
            } catch (Exception e) {
                // Error: Critical file access error
                Helpers.resetUI(this, nextBtn, null, null);
            }

        } else {
            // --- DEFAULT AVATAR SELECTION ---
            PfpRequest request = new PfpRequest(userId, currentSelectedPath);
            apiService.updatePfpDefault(request).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        skip(null); // Success! Move to next screen
                    } else {
                        // Error: Server rejected selection
                        Helpers.resetUI(CrPfpActivity.this, nextBtn, null, null);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    // Error: Connection lost
                    Helpers.resetUI(CrPfpActivity.this, nextBtn, null, null);
                }
            });
        }
    }

    // Helper to convert stream to bytes
    private byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}