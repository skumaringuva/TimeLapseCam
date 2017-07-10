package com.sheshu.timelapsecam.controller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sheshu.timelapsecam.view.AutoFitTextureView;
import com.sheshu.timelapsecam.view.Camera2Base;
import com.sheshu.timelapsecam.R;
import com.sheshu.timelapsecam.model.TesseractHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Sheshu on 7/9/17.
 */
public class CameraHolderFragment extends Fragment implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback, CameraToFragmentCallback {
    private TesseractHelper mTessHelper;
    private Camera2Base mCamera2Base;
    TextView mTextView;
    ImageButton infoButton;
    private AutoFitTextureView mTextureView;
    /**
     * This is the output file for our picture.
     */
    private File mFile;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private Activity mActivity;

    public static CameraHolderFragment newInstance() {

        Bundle args = new Bundle();

        CameraHolderFragment fragment = new CameraHolderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = getActivity();
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        return view;
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        view.findViewById(R.id.take_picture).setOnClickListener(this);
        mTessHelper = new TesseractHelper(mActivity);
        mTessHelper.initTesseract();

        infoButton = (ImageButton) view.findViewById(R.id.info);
        infoButton.setOnClickListener(this);
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mTextView = (TextView) view.findViewById(R.id.converted_text);

        view.findViewById(R.id.clear).setOnClickListener(this);

        mCamera2Base = new Camera2Base(getActivity(), mTextureView, this);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");
    }
    @Override
    public void onResume() {
        super.onResume();
        mCamera2Base.onResume();
    }
    @Override
    public void onPause() {
        mCamera2Base.onPause();
        super.onPause();
    }
    private class TessAsyncTask extends AsyncTask<Image, Void, String> {
        @Override
        protected String doInBackground(Image... params) {
            return mTessHelper.processImageView(params[0]);
        }
        @Override
        protected void onPostExecute(String s) {
            mTextView.setText(s);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_picture: {
                //takePicture("pic.jpg");
                mCamera2Base.takePictures(1000, 1);
                break;
            }
            case R.id.info: {
                if (null != mActivity) {
                    new AlertDialog.Builder(mActivity)
                            .setMessage(R.string.intro_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                break;

            }
            case R.id.clear:
            {
                mTextView.setText("");
            }

        }
    }
    void postBitmapUri(String path, Activity activity) {
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            final String text = mTessHelper.processImageView(bitmap);
        }
    }
    public void requestCameraPermission() {
        if (FragmentCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(mActivity.getFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }
    @Override
    public void saveImages(Image[] images) {
        new TessAsyncTask().execute(images);

    }
    @Override
    public void showErrorDialog(String errorMessage) {
        ErrorDialog.newInstance(errorMessage)
                .show(mActivity.getFragmentManager(), FRAGMENT_DIALOG);
    }
    @Override
    public void showConfirmationDialog(String confirmationMessage) {
    }
    @Override
    public void captureCompleted() {

      //  showToast("Saved: " + mFile);
        //Log.d(TAG, mFile.toString());

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(mActivity.getFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        Activity mImageSaverActivity;

        public ImageSaver(Image image, File file, Activity activity) {
            mImage = image;
            mFile = file;
            mImageSaverActivity = activity;
        }

        @Override
        public void run() {


            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
                output.close();
                //postBitmapUri(mFile.getAbsolutePath(),mImageSaverActivity);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }




}
