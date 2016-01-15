package com.blundell.woody.core;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;

import java.io.IOException;

public class PreLollipopFaceDetectionCamera implements FaceDetectionCamera {

    private static final int PORTRAIT = 90;
    private static final int PREVIEW_WIDTH = 200;
    private static final int PREVIEW_HEIGHT = 200;

    private final Camera camera;

    private Listener listener;

    public PreLollipopFaceDetectionCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Use this to detect faces without an on screen preview
     *
     * @param listener the {@link PreLollipopFaceDetectionCamera.Listener} for when faces are detected
     */
    @Override
    public void initialise(Listener listener) {
        initialise(listener, new DummySurfaceHolder());
    }

    /**
     * Use this to detect faces but also show a debug camera preview for testing
     *
     * @param listener the {@link PreLollipopFaceDetectionCamera.Listener} for when faces are detected
     * @param activity the activity which will have the preview overlaid
     */
    @Override
    public void initialiseWithDebugPreview(Listener listener, Activity activity) {
        DebugCameraSurfaceView debugCameraSurfaceView = new DebugCameraSurfaceView(activity, this, listener);
        debugCameraSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        camera.setDisplayOrientation(PORTRAIT);
        ((FrameLayout) activity.findViewById(android.R.id.content)).addView(debugCameraSurfaceView);
    }

    /**
     * Use this to detect faces when you have a custom surface to display upon
     *
     * @param listener the {@link PreLollipopFaceDetectionCamera.Listener} for when faces are detected
     * @param holder   the {@link android.view.SurfaceHolder} to display upon
     */
    @Override
    public void initialise(Listener listener, SurfaceHolder holder) {
        this.listener = listener;
        try {
            camera.stopPreview();
        } catch (Exception swallow) {
            // ignore: tried to stop a non-existent preview
        }
        try {
            camera.setDisplayOrientation(PORTRAIT);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.setFaceDetectionListener(new OneShotFaceDetectionListener(this));
            camera.startFaceDetection();
        } catch (IOException e) {
            this.listener.onFaceDetectionNonRecoverableError();
        }
    }

    @Override
    public void onFaceDetected(Camera.Face[] faces) {
        listener.onFaceDetected(faces, camera);
    }

    @Override
    public void onFaceTimedOut() {
        listener.onFaceTimedOut();
    }

    @Override
    public void recycle() {
        if (camera != null) {
            camera.release();
        }
    }

}
