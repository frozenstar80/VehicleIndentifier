package com.example.vehicleindentifier.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import com.ais.plate_req_api.utils.getFileFromUri
import com.example.vehicleindentifier.R
import com.example.vehicleindentifier.model.Data
import com.example.vehicleindentifier.webService.ApiService
import com.example.vehicleindentifier.webService.ApiServiceTwo
import com.example.vehicleindentifier.webService.RetrofitHelper
import com.example.vehicleindentifier.webService.RetrofitHelper2
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_anpr.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class AnprFragment : Fragment() {

    private val API_TOKEN = "2b1a7fe5da430bffe44275ac35c082ff6620702b"

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    var isFrontCamera = false

    var vrn: String = ""
    var countryCode: String = ""
    var vehicleType: String = ""
    var score: String = ""
    var bounding: String = ""
    var userDataList : MutableList<Data>? = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_anpr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        checkPermissions()

viewLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.IO) {
         getData()
}

    }

    private fun checkPermissions() {
        // Request camera permissions

        Dexter.withContext(context)
              .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {


                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    startCamera()
                    initListeners()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }

            })
            .check()



    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initListeners() = try {
        iv_take_photo.setOnClickListener {
            takePhoto()
        }
        iv_switch_camera.setOnClickListener {
            isFrontCamera = !isFrontCamera
            startCamera()
            iv_switch_camera.setImageDrawable(
                resources.getDrawable(
                    if (isFrontCamera)
                        R.drawable.ic_outline_camera_front_24
                    else
                        R.drawable.ic_outline_camera_rear_24
                )
            )
        }
    } catch (e: Exception) {
        Log.d(TAG, "Photo capture failed: ${e.message}")
    }

    @SuppressLint("NewApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector =
                if (isFrontCamera)
                    CameraSelector.DEFAULT_FRONT_CAMERA
                else
                    CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed ${e.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    @SuppressLint("NewApi")
    private fun takePhoto() = try {
        val imageCapture = imageCapture ?: throw IOException("Camera not connected")
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ImageCapture.OutputFileOptions
                .Builder(
                    requireContext().contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).build()
        else
            ImageCapture.OutputFileOptions
                .Builder(
                    requireContext().contentResolver,
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    contentValues
                ).build()
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.d(TAG, "Photo capture failed: ${exc.message}")
                    println("Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: ${output.savedUri}")
                    Toast.makeText(
                        requireContext(),
                        "Photo capture succeeded: ${output.savedUri}",
                        Toast.LENGTH_SHORT
                    ).show()
                    uploadImageToServerAndGetResults(output.savedUri)
                }
            }
        )
    } catch (e: Exception) {
        Log.d(TAG, "Photo capture failed: ${e.message}")
        println("Photo capture failed: ${e.message}")
    }

    private fun uploadImageToServerAndGetResults(savedUri: Uri?) {
        if (savedUri != null) {
            progress_bar.visibility = View.VISIBLE
            val apiService: ApiService = RetrofitHelper.getInstance().create(ApiService::class.java)
            GlobalScope.launch(Dispatchers.IO) {
                val imgFile = requireContext().getFileFromUri(savedUri)
                val compressedImageFile = Compressor.compress(requireContext(), imgFile)
                val imageFilePart = MultipartBody.Part.createFormData(
                    "upload",
                    compressedImageFile.name,
                    RequestBody.create(
                        MediaType.parse("image/*"),
                        compressedImageFile
                    )
                )
                val response =
                    apiService.getNumberPlateDetails(
                        token = "TOKEN $API_TOKEN",
                        imagePart = imageFilePart
                    )
                if (response.isSuccessful && response.body() != null) {
                    if ((response.body()?.results?.size ?: 0) > 0) {
                        withContext(Dispatchers.Main) {
                            progress_bar.visibility = View.GONE
                            // Set variables for vehicle data.
                            vrn = response.body()?.results?.get(0)?.plate.toString().uppercase()
                            countryCode = response.body()?.results?.get(0)?.region?.code.toString()
                                .uppercase()
                            vehicleType = response.body()?.results?.get(0)?.vehicle?.type.toString()
                                .uppercase()

                            score = response.body()?.results?.get(0)?.score.toString()

                            bounding = response.body()?.results?.get(0)?.box.toString()

                            Log.d(TAG, response.body()?.results.toString())

                            updateNumberplate(vrn, countryCode)
                            verifyDetails(vrn)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "No VRN found in image.")
                            progress_bar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private suspend fun getData() {
        val apiService = RetrofitHelper2.getInstance().create(ApiServiceTwo::class.java)

        val response = apiService.getDetails()
        if (response.isSuccessful && response.body() != null) {
            withContext(Dispatchers.Main){

                userDataList = response.body()?.data
                Log.e("data",response.body()?.data.toString())
            }
        }
    }

    private  fun verifyDetails(vrn: String){
            userDataList?.forEach {
                if (it.plate==vrn){
                    tv_vehicle_type.text = "${it.name}  (${it.gender})"
                    tv_vehicle_score.text = "${it.color}"
                    tv_bounding.text = "${it.type}"

                    Navigation.findNavController(requireActivity(), R.id.main_fragment)
                        .navigate(R.id.action_anprFragment_to_userDataFragment,
                            bundleOf("success" to true
                            )
                        )

                    return
                }
            }
        Navigation.findNavController(requireActivity(), R.id.main_fragment)
            .navigate(R.id.action_anprFragment_to_userDataFragment,
                bundleOf("success" to false
                )
            )
    }

    private fun updateNumberplate(vrn: String, country_code: String) {
        tv_numberplate.text = vrn
        tv_country_prefix.text = country_code
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }



}
