package id.indosw.backgroundremoversample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import id.indosw.backgroundremover.RemoveBackground

@Suppress("UNUSED_ANONYMOUS_PARAMETER", "SameParameterValue")
class AllActionKtActivity : AppCompatActivity() {
    private var setBorderCheck: CheckBox? = null
    private var setCropCheck: CheckBox? = null
    private var setIntroCheck: CheckBox? = null
    private var imageView: ImageView? = null
    override fun onCreate(_savedInstanceState: Bundle?) {
        super.onCreate(_savedInstanceState)
        setContentView(R.layout.all_action_kt)
        initialize()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1000)
        } else {
            initializeLogic()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            initializeLogic()
        }
    }

    private fun initialize() {
        setBorderCheck = findViewById(R.id.setBorderCheck)
        setCropCheck = findViewById(R.id.setCropCheck)
        setIntroCheck = findViewById(R.id.setIntroCheck)
        val pickImage = findViewById<Button>(R.id.pickImage)
        imageView = findViewById(R.id.imageView)
        val imageIconUri = getUriFromDrawable(R.drawable.image_icon)
        imageView!!.setImageURI(imageIconUri)
        imageView!!.tag = imageIconUri
        pickImage.setOnClickListener { _view: View? -> goTaskRemoveBG() }
        setCropCheck!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (setCropCheck!!.isChecked) {
                setBorderCheck!!.isChecked = false
                setIntroCheck!!.isChecked = false
            }
        }
        setBorderCheck!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (setCropCheck!!.isChecked) {
                setCropCheck!!.isChecked = false
                setIntroCheck!!.isChecked = false
            }
        }
        setIntroCheck!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (setCropCheck!!.isChecked) {
                setCropCheck!!.isChecked = false
                setBorderCheck!!.isChecked = false
            }
        }
    }

    private fun initializeLogic() {}
    @Suppress("UNUSED_VARIABLE")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RemoveBackground.CUTOUT_ACTIVITY_REQUEST_CODE.toInt()) {
            when (resultCode) {
                RESULT_OK -> {
                    val imageUri = RemoveBackground.getUri(data)
                    // Save the image using the returned Uri here
                    imageView!!.setImageURI(imageUri)
                    imageView!!.tag = imageUri
                }
                RemoveBackground.CUTOUT_ACTIVITY_RESULT_ERROR_CODE.toInt() -> {
                    val ex = RemoveBackground.getError(data)
                }
                else -> {
                    print("User cancelled the RemoveBackground screen")
                }
            }
        }
    }

    private fun goTaskRemoveBG() {
        if (setBorderCheck!!.isChecked) {
            RemoveBackground.activity()
                    .bordered()
                    .noCrop()
                    .start(this)
        } else {
            if (setCropCheck!!.isChecked) {
                RemoveBackground.activity()
                        .bordered()
                        .start(this)
            } else {
                if (setIntroCheck!!.isChecked) {
                    RemoveBackground.activity()
                            .intro()
                            .noCrop()
                            .start(this)
                } else {
                    RemoveBackground.activity()
                            .bordered()
                            .noCrop()
                            .intro()
                            .start(this)
                }
            }
        }
    }

    private fun getUriFromDrawable(drawableId: Int): Uri {
        return Uri.parse("android.resource://" + packageName + "/drawable/" + applicationContext.resources.getResourceEntryName(drawableId))
    }
}