package com.blundell.woody.core;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

public interface FaceDetectionCamera extends OneShotFaceDetectionListener.Listener {
    void initialise(Listener listener);

    void initialiseWithDebugPreview(Listener listener, Activity activity);

    void initialise(Listener listener, SurfaceHolder holder);

    @Override
    void onFaceDetected(Camera.Face[] faces);

    @Override
    void onFaceTimedOut();

    void recycle();

    interface Listener {
        void onFaceDetected(Camera.Face[] faces, Camera camera);

        void onFaceTimedOut();

        void onFaceDetectionNonRecoverableError();

    }
}
