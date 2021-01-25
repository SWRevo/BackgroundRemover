@file:Suppress("DEPRECATION")

package id.indosw.backgroundremover

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Pair
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

internal class SaveDrawingTask(activity: RemoveBackgroundActivity) : AsyncTask<Bitmap?, Void?, Pair<File?, Exception?>>() {
    private val activityWeakReference: WeakReference<RemoveBackgroundActivity> = WeakReference(activity)
    override fun onPreExecute() {
        super.onPreExecute()
        activityWeakReference.get()!!.loadingModal!!.visibility = View.VISIBLE
    }

    override fun doInBackground(vararg bitmaps: Bitmap?): Pair<File?, Exception?> {
        try {
            val file = File.createTempFile(SAVED_IMAGE_NAME, SAVED_IMAGE_FORMAT, activityWeakReference.get()!!.applicationContext.cacheDir)
            FileOutputStream(file).use { out ->
                bitmaps[0]!!.compress(Bitmap.CompressFormat.PNG, 95, out)
                return Pair(file, null)
            }
        } catch (e: IOException) {
            return Pair(null, e)
        }
    }

    override fun onPostExecute(result: Pair<File?, Exception?>) {
        super.onPostExecute(result)
        val resultIntent = Intent()
        if (result.first != null) {
            val uri = Uri.fromFile(result.first)
            resultIntent.putExtra(RemoveBackground.CUTOUT_EXTRA_RESULT, uri)
            activityWeakReference.get()!!.setResult(Activity.RESULT_OK, resultIntent)
            activityWeakReference.get()!!.finish()
        } else {
            activityWeakReference.get()!!.exitWithError(result.second)
        }
    }

    companion object {
        private const val SAVED_IMAGE_FORMAT = "png"
        private const val SAVED_IMAGE_NAME = "cutout_tmp"
    }

}