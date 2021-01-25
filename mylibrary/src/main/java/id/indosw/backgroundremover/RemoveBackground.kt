package id.indosw.backgroundremover

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri

object RemoveBackground {
    const val CUTOUT_ACTIVITY_REQUEST_CODE: Short = 368
    const val CUTOUT_ACTIVITY_RESULT_ERROR_CODE: Short = 3680
    const val CUTOUT_EXTRA_SOURCE = "CUTOUT_EXTRA_SOURCE"
    const val CUTOUT_EXTRA_RESULT = "CUTOUT_EXTRA_RESULT"
    const val CUTOUT_EXTRA_BORDER_COLOR = "CUTOUT_EXTRA_BORDER_COLOR"
    const val CUTOUT_EXTRA_CROP = "CUTOUT_EXTRA_CROP"
    const val CUTOUT_EXTRA_INTRO = "CUTOUT_EXTRA_INTRO"
    @JvmStatic
    fun activity(): ActivityBuilder {
        return ActivityBuilder()
    }

    /**
     * Reads the [Uri] from the result data. This Uri is the path to the saved PNG
     *
     * @param data Result data to get the Uri from
     */
    @JvmStatic
    fun getUri(data: Intent?): Uri? {
        return data?.getParcelableExtra(CUTOUT_EXTRA_RESULT)
    }

    /**
     * Gets an Exception from the result data if the [RemoveBackgroundActivity] failed at some point
     *
     * @param data Result data to get the Exception from
     */
    @JvmStatic
    fun getError(data: Intent?): Exception? {
        return if (data != null) data.getSerializableExtra(CUTOUT_EXTRA_RESULT) as Exception? else null
    }

    /**
     * Builder used for creating RemoveBackground Activity by user request.
     */
    @Suppress("unused")
    class ActivityBuilder {
        private var source // The image to crop source Android uri
                : Uri? = null
        private var bordered = false
        private var crop = true // By default the cropping activity is started
        private var intro = false
        private var borderColor = Color.WHITE // Default border color is no border color is passed

        /**
         * Get [RemoveBackgroundActivity] intent to start the activity.
         */
        private fun getIntent(context: Context): Intent {
            val intent = Intent()
            intent.setClass(context, RemoveBackgroundActivity::class.java)
            if (source != null) {
                intent.putExtra(CUTOUT_EXTRA_SOURCE, source)
            }
            if (bordered) {
                intent.putExtra(CUTOUT_EXTRA_BORDER_COLOR, borderColor)
            }
            if (crop) {
                intent.putExtra(CUTOUT_EXTRA_CROP, true)
            }
            if (intro) {
                intent.putExtra(CUTOUT_EXTRA_INTRO, true)
            }
            return intent
        }

        /**
         * By default the user can select images from camera or gallery but you can also call this method to load a pre-saved image
         *
         * @param source [Uri] instance of the image to be loaded
         */
        fun src(source: Uri?): ActivityBuilder {
            this.source = source
            return this
        }

        /**
         * This method adds a white border around the final PNG image
         */
        fun bordered(): ActivityBuilder {
            bordered = true
            return this
        }

        /**
         * This method adds a border around the final PNG image
         *
         * @param borderColor The border color. You can pass any [Color]
         */
        fun bordered(borderColor: Int): ActivityBuilder {
            this.borderColor = borderColor
            return bordered()
        }

        /**
         * Disables the cropping screen shown before the background removal screen
         */
        fun noCrop(): ActivityBuilder {
            crop = false
            return this
        }

        /**
         * Shows an introduction to the activity, explaining every button usage. The intro is show only once.
         */
        fun intro(): ActivityBuilder {
            intro = true
            return this
        }

        /**
         * Start [RemoveBackgroundActivity].
         *
         * @param activity activity to receive result
         */
        fun start(activity: Activity) {
            activity.startActivityForResult(getIntent(activity), CUTOUT_ACTIVITY_REQUEST_CODE.toInt())
        }
    }
}