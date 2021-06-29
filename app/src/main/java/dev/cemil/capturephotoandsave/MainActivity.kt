package dev.cemil.capturephotoandsave

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_capture.setOnClickListener {
            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        btn_save.setOnClickListener {
            galleryAddPic()
        }
        btn_delete.setOnClickListener {
            File(currentPhotoPath).delete()
        }
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.resolveActivity(packageManager)
            val file = createImageFile()
            val fileUri = FileProvider.getUriForFile(this,"dev.cemil.capturephotoandsave.fileProvider", file)
            registerTakePicture.launch(fileUri)
        }else {
            Log.e("===>", "Permission Denied")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
        ).apply {
            currentPhotoPath = absolutePath
            Log.e("===>fileabsolute path", absolutePath)
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    private val registerTakePicture = registerForActivityResult(
            ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            Log.e("===>", "TRUE")
            val photo = BitmapFactory.decodeFile(currentPhotoPath)
            imageView.setImageBitmap(photo)
        } else {
            Log.e("===>", "FALSE")
            File(currentPhotoPath).delete()
        }
    }
}