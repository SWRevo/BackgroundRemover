package id.indosw.backgroundremoversample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import id.indosw.backgroundremover.RemoveBackground;

public class AllActionActivity extends AppCompatActivity {
    private CheckBox setBorderCheck;
    private CheckBox setCropCheck;
    private CheckBox setIntroCheck;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.all_action);
        initialize();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
        else {
            initializeLogic();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            initializeLogic();
        }
    }

    private void initialize() {
        setBorderCheck = findViewById(R.id.setBorderCheck);
        setCropCheck = findViewById(R.id.setCropCheck);
        setIntroCheck = findViewById(R.id.setIntroCheck);
        Button pickImage = findViewById(R.id.pickImage);
        imageView = findViewById(R.id.imageView);
        final Uri imageIconUri = getUriFromDrawable(R.drawable.image_icon);
        imageView.setImageURI(imageIconUri);
        imageView.setTag(imageIconUri);
        pickImage.setOnClickListener(_view -> goTaskRemoveBG());


        setCropCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(setCropCheck.isChecked()){
                setBorderCheck.setChecked(false);
                setIntroCheck.setChecked(false);
            }
        });

        setBorderCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(setCropCheck.isChecked()){
                setCropCheck.setChecked(false);
                setIntroCheck.setChecked(false);
            }
        });

        setIntroCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(setCropCheck.isChecked()){
                setCropCheck.setChecked(false);
                setBorderCheck.setChecked(false);
            }
        });

    }

    private void initializeLogic() {
    }

    @SuppressWarnings({"unused", "ThrowableNotThrown"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RemoveBackground.CUTOUT_ACTIVITY_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = RemoveBackground.getUri(data);
                // Save the image using the returned Uri here
                imageView.setImageURI(imageUri);
                imageView.setTag(imageUri);
            } else if (resultCode == RemoveBackground.CUTOUT_ACTIVITY_RESULT_ERROR_CODE) {
                Exception ex = RemoveBackground.getError(data);
            } else {
                System.out.print("User cancelled the RemoveBackground screen");
            }
        }
    }

    public void goTaskRemoveBG() {
        if (setBorderCheck.isChecked()) {
            RemoveBackground.activity()
                    .bordered()
                    .noCrop()
                    .start(this);
        }
        else {
            if (setCropCheck.isChecked()) {
                RemoveBackground.activity()
                        .bordered()
                        .start(this);
            }
            else {
                if (setIntroCheck.isChecked()) {
                    RemoveBackground.activity()
                            .intro()
                            .noCrop()
                            .start(this);
                }
                else {
                    RemoveBackground.activity()
                            .bordered()
                            .noCrop()
                            .intro()
                            .start(this);
                }
            }
        }
    }

    public Uri getUriFromDrawable(int drawableId) {
        return Uri.parse("android.resource://" + getPackageName() + "/drawable/" + getApplicationContext().getResources().getResourceEntryName(drawableId));
    }
}
