package id.indosw.backgroundremoversample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.indosw.backgroundremover.RemoveBackground

class MainKtActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        imageView = findViewById(R.id.imageView)
        val imageIconUri = getUriFromDrawable(R.drawable.image_icon)
        imageView!!.setImageURI(imageIconUri)
        imageView!!.tag = imageIconUri
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val testImageUri = getUriFromDrawable(R.drawable.test_image)
            RemoveBackground.activity()
                    .src(testImageUri)
                    .bordered()
                    .noCrop()
                    .start(this)
        }
    }

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

    private fun getUriFromDrawable(drawableId: Int): Uri {
        return Uri.parse("android.resource://" + packageName + "/drawable/" + applicationContext.resources.getResourceEntryName(drawableId))
    }
}