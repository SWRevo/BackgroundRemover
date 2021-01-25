package id.indosw.easyphotopicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@SuppressWarnings({"unused", "FieldCanBeLocal", "ResultOfMethodCallIgnored"})
public class EasyImage implements EasyImageConfig {

    private static final boolean SHOW_GALLERY_IN_CHOOSER = false;

    public enum ImageSource {
        GALLERY, DOCUMENTS, CAMERA
    }

    public interface Callbacks {
        void onImagePickerError(Exception e, ImageSource source, int type);

        void onImagePicked(File imageFile, ImageSource source, int type);

        void onCanceled(ImageSource source, int type);
    }

    private static final String KEY_PHOTO_URI = "pl.aprilapps.easyphotopicker.photo_uri";
    private static final String KEY_LAST_CAMERA_PHOTO = "pl.aprilapps.easyphotopicker.last_photo";
    private static final String KEY_TYPE = "pl.aprilapps.easyphotopicker.type";

    private static Uri createCameraPictureFile(Context context) throws IOException {
        File imagePath = EasyImageFiles.getCameraPicturesLocation(context);
        String packageName = context.getApplicationContext().getPackageName();
        String authority = packageName + ".easyphotopicker.fileprovider";
        Uri uri = FileProvider.getUriForFile(context, authority, imagePath);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_PHOTO_URI, uri.toString());
        editor.putString(KEY_LAST_CAMERA_PHOTO, imagePath.toString());
        editor.apply();
        return uri;
    }

    private static Intent createDocumentsIntent(Context context, int type) {
        storeType(context, type);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        return intent;
    }

    private static Intent createGalleryIntent(Context context, int type) {
        storeType(context, type);
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    private static Intent createCameraIntent(Context context, int type) {
        storeType(context, type);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri capturedImageUri = createCameraPictureFile(context);
            //We have to explicitly grant the write permission since Intent.setFlag works only on API Level >=20
            grantWritePermission(context, intent, capturedImageUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return intent;
    }

    private static void revokeWritePermission(Context context, Uri uri) {
        context.revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    private static void grantWritePermission(Context context, Intent intent, Uri uri) {
        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    private static Intent createChooserIntent(Context context, String chooserTitle, int type) throws IOException {
        return createChooserIntent(context, chooserTitle, SHOW_GALLERY_IN_CHOOSER, type);
    }

    private static Intent createChooserIntent(Context context, String chooserTitle, boolean showGallery, int type) throws IOException {
        storeType(context, type);

        Uri outputFileUri = createCameraPictureFile(context);
        List<Intent> cameraIntents = new ArrayList<>();
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = context.getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> camList = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : camList) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            grantWritePermission(context, intent, outputFileUri);
            cameraIntents.add(intent);
        }
        Intent galleryIntent;

        if (showGallery) {
            galleryIntent = createGalleryIntent(context, type);
        } else {
            galleryIntent = createDocumentsIntent(context, type);
        }

        Intent chooserIntent = Intent.createChooser(galleryIntent, chooserTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[0]));

        return chooserIntent;
    }

    private static void storeType(Context context, int type) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_TYPE, type).apply();
    }

    private static int restoreType(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_TYPE, 0);
    }

    public static void openChooserWithDocuments(Activity activity, String chooserTitle, int type) {
        try {
            Intent intent = createChooserIntent(activity, chooserTitle, type);
            activity.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooserWithDocuments(Fragment fragment, String chooserTitle, int type) {
        try {
            Intent intent = createChooserIntent(fragment.getActivity(), chooserTitle, type);
            fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooserWithDocuments(android.app.Fragment fragment, String chooserTitle, int type) {
        try {
            Intent intent = createChooserIntent(fragment.getActivity(), chooserTitle, type);
            fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooserWithGallery(Activity activity, String chooserTitle, int type) {
        try {
            Intent intent = createChooserIntent(activity, chooserTitle, true, type);
            activity.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooserWithGallery(Fragment fragment, String chooserTitle, int type) {
        try {
            Intent intent = createChooserIntent(fragment.getActivity(), chooserTitle, true, type);
            fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooserWithGallery(android.app.Fragment fragment, String chooserTitle, int type) {
        try {
            Intent intent = createChooserIntent(fragment.getActivity(), chooserTitle, true, type);
            fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openDocuments(Activity activity, int type) {
        Intent intent = createDocumentsIntent(activity, type);
        activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_DOCUMENTS);
    }

    public static void openDocuments(Fragment fragment, int type) {
        Intent intent = createDocumentsIntent(fragment.getContext(), type);
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_DOCUMENTS);
    }

    public static void openDocuments(android.app.Fragment fragment, int type) {
        Intent intent = createDocumentsIntent(fragment.getActivity(), type);
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_DOCUMENTS);
    }

    public static void openGallery(Activity activity, int type) {
        Intent intent = createGalleryIntent(activity, type);
        activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openGallery(Fragment fragment, int type) {
        Intent intent = createGalleryIntent(fragment.getContext(), type);
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openGallery(android.app.Fragment fragment, int type) {
        Intent intent = createGalleryIntent(fragment.getActivity(), type);
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openCamera(Activity activity, int type) {
        Intent intent = createCameraIntent(activity, type);
        activity.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }

    public static void openCamera(Fragment fragment, int type) {
        Intent intent = createCameraIntent(fragment.getActivity(), type);
        fragment.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }

    public static void openCamera(android.app.Fragment fragment, int type) {
        Intent intent = createCameraIntent(fragment.getActivity(), type);
        fragment.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }

    @Nullable
    private static File takenCameraPicture(Context context) {
        String lastCameraPhoto = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LAST_CAMERA_PHOTO, null);
        if (lastCameraPhoto != null) {
            return new File(lastCameraPhoto);
        } else {
            return null;
        }
    }

    public static void handleActivityResult(int requestCode, int resultCode, Intent data, Activity activity, Callbacks callbacks) {
        if (requestCode == EasyImageConfig.REQ_SOURCE_CHOOSER || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY || requestCode == EasyImageConfig.REQ_TAKE_PICTURE || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
                    onPictureReturnedFromDocuments(data, activity, callbacks);
                } else if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY) {
                    onPictureReturnedFromGallery(data, activity, callbacks);
                } else if (requestCode == EasyImageConfig.REQ_TAKE_PICTURE) {
                    onPictureReturnedFromCamera(activity, callbacks);
                } else if (data == null || data.getData() == null) {
                    onPictureReturnedFromCamera(activity, callbacks);
                } else {
                    onPictureReturnedFromDocuments(data, activity, callbacks);
                }
            } else {
                if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
                    callbacks.onCanceled(ImageSource.DOCUMENTS, restoreType(activity));
                } else if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY) {
                    callbacks.onCanceled(ImageSource.GALLERY, restoreType(activity));
                } else if (requestCode == EasyImageConfig.REQ_TAKE_PICTURE) {
                    callbacks.onCanceled(ImageSource.CAMERA, restoreType(activity));
                } else if (data == null || data.getData() == null) {
                    callbacks.onCanceled(ImageSource.CAMERA, restoreType(activity));
                } else {
                    callbacks.onCanceled(ImageSource.DOCUMENTS, restoreType(activity));
                }
            }
        }
    }

    public static boolean willHandleActivityResult(int requestCode, int resultCode, Intent data) {
        return requestCode == EasyImageConfig.REQ_SOURCE_CHOOSER || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY || requestCode == EasyImageConfig.REQ_TAKE_PICTURE || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS;
    }

    /**
     * @param context context
     * @return File containing lastly taken (using camera) photo. Returns null if there was no photo taken or it doesn't exist anymore.
     */
    public static File lastlyTakenButCanceledPhoto(Context context) {
        String filePath = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LAST_CAMERA_PHOTO, null);
        if (filePath == null) return null;
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    private static void onPictureReturnedFromDocuments(Intent data, Activity activity, Callbacks callbacks) {
        try {
            Uri photoPath = data.getData();
            File photoFile = EasyImageFiles.pickedExistingPicture(activity, photoPath);
            callbacks.onImagePicked(photoFile, ImageSource.DOCUMENTS, restoreType(activity));
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onImagePickerError(e, ImageSource.DOCUMENTS, restoreType(activity));
        }
    }

    private static void onPictureReturnedFromGallery(Intent data, Activity activity, Callbacks callbacks) {
        try {
            Uri photoPath = data.getData();
            File photoFile = EasyImageFiles.pickedExistingPicture(activity, photoPath);
            callbacks.onImagePicked(photoFile, ImageSource.GALLERY, restoreType(activity));
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onImagePickerError(e, ImageSource.GALLERY, restoreType(activity));
        }
    }

    private static void onPictureReturnedFromCamera(Activity activity, Callbacks callbacks) {
        try {

            String lastImageUri = PreferenceManager.getDefaultSharedPreferences(activity).getString(KEY_PHOTO_URI, null);
            if (!TextUtils.isEmpty(lastImageUri)) {
                revokeWritePermission(activity, Uri.parse(lastImageUri));
            }

            File photoFile = EasyImage.takenCameraPicture(activity);

            if (photoFile == null) {
                Exception e = new IllegalStateException("Unable to get the picture returned from camera");
                callbacks.onImagePickerError(e, ImageSource.CAMERA, restoreType(activity));
            } else {
                callbacks.onImagePicked(photoFile, ImageSource.CAMERA, restoreType(activity));
            }

            PreferenceManager.getDefaultSharedPreferences(activity)
                    .edit()
                    .remove(KEY_LAST_CAMERA_PHOTO)
                    .remove(KEY_PHOTO_URI)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onImagePickerError(e, ImageSource.CAMERA, restoreType(activity));
        }
    }

    public static void clearPublicTemp(Context context) {
        List<File> tempFiles = new ArrayList<>();
        File[] files = EasyImageFiles.publicTempDir(context).listFiles();
        for (File file : Objects.requireNonNull(files)) {
            file.delete();
        }
    }


    /**
     * Method to clear configuration. Would likely be used in onDestroy(), onDestroyView()...
     *
     * @param context context
     */
    public static void clearConfiguration(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .remove(BundleKeys.FOLDER_NAME)
                .remove(BundleKeys.FOLDER_LOCATION)
                .remove(BundleKeys.PUBLIC_TEMP)
                .apply();
    }

    public static Configuration configuration(Context context) {
        return new Configuration(context);
    }

    public static class Configuration {
        private final Context context;

        private Configuration(Context context) {
            this.context = context;
        }

        public Configuration setImagesFolderName(String folderName) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putString(BundleKeys.FOLDER_NAME, folderName)
                    .apply();
            return this;
        }

        public Configuration saveInRootPicturesDirectory() {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(BundleKeys.FOLDER_LOCATION, EasyImageFiles.publicRootDir().toString())
                    .apply();
            return this;
        }

        public Configuration saveInAppExternalFilesDir() {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(BundleKeys.FOLDER_LOCATION, EasyImageFiles.publicAppExternalDir(context).toString())
                    .apply();
            return this;
        }


        /**
         * Use this method if you want your picked gallery or documents pictures to be duplicated into public, other apps accessible, directory.
         * You'll have to take care of removing that file on your own after you're done with it. Use EasyImage.clearPublicTemp() method for that.
         * If you don't delete them they could show up in user galleries.
         *
         * @return modified Configuration object
         */
        public Configuration setCopyExistingPicturesToPublicLocation(boolean copyToPublicLocation) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(BundleKeys.PUBLIC_TEMP, copyToPublicLocation)
                    .apply();
            return this;
        }
    }
}