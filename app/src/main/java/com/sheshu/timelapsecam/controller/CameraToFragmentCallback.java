package com.sheshu.timelapsecam.controller;

import android.media.Image;

/**
 * Created by Sheshu on 7/9/17.
 */
public interface CameraToFragmentCallback {

    public void requestCameraPermission();

    public void saveImages(Image[] images);

    public void showErrorDialog(String errorMessage);

    public void showConfirmationDialog(String confirmationMessage);

    public void captureCompleted();
}
