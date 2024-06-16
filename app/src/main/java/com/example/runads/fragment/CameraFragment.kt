/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.runads.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Camera
import androidx.camera.core.AspectRatio
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.runads.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

import android.media.MediaPlayer
import android.os.Vibrator
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.runads.*
import com.example.runads.models.ResPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.*

import android.app.AlertDialog





class CameraFragment : Fragment(), FaceLandmarkerHelper.LandmarkerListener {

    var avgEAR: Double = 0.0
    private var isSleeping = false
    companion object {
        const val TAG = "Face Landmarker"
        const val CAMERA_FRAGMENT_TAG = "CameraFragment"

    }

    // Biến để lưu trữ AlertDialog
    private var alertDialog: AlertDialog? = null

    private var status: String = "awake" // Default status is awake

    private var sleepingStartTime: Long = 0

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator

    lateinit var prefs: SharedPreferences

    private var lastStatus: String? = null

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            // Replace the current fragment with PermissionsFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PermissionsFragment())
                .commit()
        }

        // Public method to get the TAG value
        fun getFragmentTag(): String {
            return TAG
        }

        // Start the FaceLandmarkerHelper again when users come back
        // to the foreground.
        backgroundExecutor.execute {
            if (faceLandmarkerHelper.isClose()) {
                faceLandmarkerHelper.setupFaceLandmarker()
            }
        }
    }





    override fun onPause() {
        super.onPause()
        if(this::faceLandmarkerHelper.isInitialized) {
            viewModel.setMaxFaces(faceLandmarkerHelper.maxNumFaces)
            viewModel.setMinFaceDetectionConfidence(faceLandmarkerHelper.minFaceDetectionConfidence)
            viewModel.setMinFaceTrackingConfidence(faceLandmarkerHelper.minFaceTrackingConfidence)
            viewModel.setMinFacePresenceConfidence(faceLandmarkerHelper.minFacePresenceConfidence)
            viewModel.setDelegate(faceLandmarkerHelper.currentDelegate)

            // Close the FaceLandmarkerHelper and release resources
            backgroundExecutor.execute { faceLandmarkerHelper.clearFaceLandmarker() }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireActivity().getSharedPreferences("com.example.runads", AppCompatActivity.MODE_PRIVATE)
        IApiService.changeBaseDomain(prefs.getString(PrefManager.KEY_PREF_DOMAIN_API, "").toString())

//        mediaPlayer = MediaPlayer.create(context, R.raw.alert_sound)
        vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Create the FaceLandmarkerHelper that will handle the inference
        backgroundExecutor.execute {
            faceLandmarkerHelper = FaceLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minFaceDetectionConfidence = viewModel.currentMinFaceDetectionConfidence,
                minFaceTrackingConfidence = viewModel.currentMinFaceTrackingConfidence,
                minFacePresenceConfidence = viewModel.currentMinFacePresenceConfidence,
                maxNumFaces = viewModel.currentMaxFaces,
                currentDelegate = viewModel.currentDelegate,
                faceLandmarkerHelperListener = this
            )
        }


    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }


    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectFace(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectFace(imageProxy: ImageProxy) {
        faceLandmarkerHelper.detectLiveStream(
            imageProxy = imageProxy,
            isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after face have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResults(
        resultBundle: FaceLandmarkerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {


                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentCameraBinding.overlay.setResults(
                    resultBundle.result,
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()

                avgEAR = resultBundle.avgEAR

                if (avgEAR < 0.14) {
                    if (!isSleeping) {
                        // If avgEAR is below 0.16 for the first time, start the timer
                        sleepingStartTime = System.currentTimeMillis()
                        isSleeping = true
                    } else {
                        // If avgEAR is still below 0.16, check if 5 seconds have passed
                        val currentTime = System.currentTimeMillis()
                        val timeDifference = currentTime - sleepingStartTime
                        if (timeDifference >= 3000) {
                            // If 5 seconds have passed, set status to "sleep"
                            status = "sleep"
                        }
                    }
                } else {
                    // If avgEAR is above or equal to 0.16, reset the timer and status
                    isSleeping = false
                    sleepingStartTime = 0
                    status = "awake"
                }

                fragmentCameraBinding.statusTextView.text = status
                if (status == "sleep" && lastStatus != "sleep") {
                    showSleepNotification()
                    postHumanEvent() // Gửi yêu cầu chỉ khi trạng thái thay đổi từ "awake" sang "sleep"
                } else if (status == "awake" && lastStatus != "awake") {
                    cancelSleepNotification()
                }

                lastStatus = status // Cập nhật trạng thái cuối cùng

            }
        }
    }



    override fun onEmpty() {
        fragmentCameraBinding.overlay.clear()
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
//                )
//            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showSleepNotification() {
        // Phát âm thanh
        activity?.runOnUiThread {
            // Khởi tạo MediaPlayer nếu cần
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.alert_sound)
                mediaPlayer?.setOnCompletionListener {
                    // Xử lý khi âm thanh hoàn thành nếu cần
                }
            }

            // Phát âm thanh nếu MediaPlayer đã được khởi tạo
            mediaPlayer?.start()

            // Rung
            vibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))


            // Hiển thị popup cảnh báo
            showAlertPopup()

        }

    }

    private fun cancelSleepNotification() {
        activity?.runOnUiThread {

            // Dừng âm thanh nếu đang phát và giải phóng tài nguyên
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null


            // Đổi màu khung về màu trong suốt sau 3 giây
            Handler(Looper.getMainLooper()).postDelayed({
                fragmentCameraBinding.root.setBackgroundColor(Color.TRANSPARENT)

                // Tắt popup cảnh báo
                dismissAlertPopup()
                vibrator.cancel() // Dừng rung
            }, 3000)
        }
    }

    private fun showAlertPopup() {
        activity?.let {
            if (alertDialog == null) {
                val builder = AlertDialog.Builder(it)

                // Inflate custom layout
                val inflater = it.layoutInflater
                val dialogView = inflater.inflate(R.layout.custom_alert_dialog, null)
                builder.setView(dialogView)

                // Set up the dialog
                builder.setCancelable(false)

                // Create and show the dialog
                alertDialog = builder.create()
            }
            alertDialog?.show()
        }
    }

    private fun dismissAlertPopup() {
        alertDialog?.dismiss()
        alertDialog = null
    }

    private fun postHumanEvent() {
        val domainApi = prefs.getString(PrefManager.KEY_PREF_DOMAIN_API, "").toString()
        val appId = prefs.getString(PrefManager.KEY_PREF_APP_ID, "").toString()

        if (domainApi.isNotBlank()) {
            IApiService.changeBaseDomain(domainApi)
        }

        IApiService.apiService.postHumanEvent(
            appId
        ).enqueue(object : Callback<ResPost> {
            override fun onResponse(call: Call<ResPost>, response: Response<ResPost>) {
                if (response.isSuccessful) {
                    val resPost = response.body()
                    println("API SUCCESS: ${resPost?.is_success}")
                } else {
                    println("API ERROR: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResPost>, t: Throwable) {
                println("API FAIL postHumanEvent() ${t.message}")
                t.printStackTrace()
            }
        })
    }
}
