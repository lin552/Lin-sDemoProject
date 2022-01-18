package com.lin552.linsdemoproject.Media

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import com.lin552.linsdemoproject.databinding.MediaTestLayoutBinding
import java.io.File
import java.io.IOException
import java.io.InputStream

class MediaTestActivity : Activity() {

    lateinit var imageUri: Uri

    private lateinit var binding: MediaTestLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MediaTestLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //选择文件
        binding.choseFilePath.setOnClickListener {
            chosePhoto()
        }

        //相机拍照
        binding.capturePhoto.setOnClickListener {
            capturePhoto("testImageFile")
        }

        //裁剪图片
        binding.clipImage.setOnClickListener {
            clipPhoto(imageUri)
        }
    }

    /**
     * Copy Uri 到本地目录
     */
//    @Throws(IOException::class)
//    private fun copyFileStream(dest: File, uri: Uri, context: Context) {
//        var inputStream: InputStream? = null
//        var sink: BufferedSink? = null
//        var source: BufferedSource? = null
//        val startTime = System.currentTimeMillis()
//        try {
//            //这里速度受限于手机
//
//            sink = Okio.buffer(Okio.sink(dest))
//            inputStream = context.contentResolver.openInputStream(uri)
////            VLogger.COMMON.d("cost", "${System.currentTimeMillis() - startTime}")
//
//            source = Okio.buffer(Okio.source(inputStream))
//
//            sink.writeAll(source)
//            sink?.flush()
//            val cost = System.currentTimeMillis() - startTime
////            VLogger.COMMON.d("cost", "$cost")
//
//        } catch (e: Exception) {
////            VLogger.COMMON.wtf("copyFileStream", e, true)
//        } finally {
//
//            sink?.close()
//            source?.close()
//            inputStream?.close()
//        }
//    }

    /**
     *  调用系统中自带的图片剪裁
     */
    private fun clipPhoto(uri: Uri) {
        Intent("com.android.camera.action.CROP").apply {
            flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            setDataAndType(uri, "image/*")
            // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
            putExtra("crop", "true")
            // aspectX aspectY 是宽高的比例
            putExtra("aspectX", 1)
            putExtra("aspectY", 1)
            // outputX outputY 是裁剪图片宽高
            putExtra("outputX", 150)
            putExtra("outputY", 150)
            putExtra("return-data", true)
            val file = File(Environment.getExternalStorageDirectory(), "image_output.jpg")
            putExtra(
                MediaStore.EXTRA_OUTPUT,
                Uri.parse("file:////sdcard/image_output.jpg")
            )
            startActivityForResult(this, REQUEST_IMAGE_CLIP)
        }
    }

//    private fun gotoCrop(sourceUri: Uri) {
//        imageCropFile = FileUtils.createImageFile(this, true)
//        if (imageCropFile != null) {
//            val intent = Intent("com.android.camera.action.CROP")
//            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//            intent.putExtra("crop", "true")
//            intent.putExtra("aspectX", 1) //X方向上的比例
//            intent.putExtra("aspectY", 1) //Y方向上的比例
//            intent.putExtra("outputX", 256) //裁剪区的宽
//            intent.putExtra("outputY", 256) //裁剪区的高
//            intent.putExtra("scale ", true) //是否保留比例
//            intent.putExtra("return-data", false)
//            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
//            intent.setDataAndType(sourceUri, "image/*") //设置数据源
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtils.uri)
//            } else {
//                val imgCropUri = Uri.fromFile(imageCropFile)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgCropUri)
//            }
//            startActivityForResult(intent, REQUEST_IMAGE_CLIP)
//        }
//    }

    /**
     * 选择图片
     */
    private fun chosePhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_GET_CONTENT)
    }

    private fun capturePhoto(targetFilename: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(
                MediaStore.EXTRA_OUTPUT,
                Uri.withAppendedPath(imageUri, targetFilename)
            )
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, Companion.REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            val thumbnail: Bitmap? = data.getParcelableExtra("data")
            // Do other work with full size photo saved in locationForPhotos
//            ...
        } else if (requestCode == REQUEST_IMAGE_GET_CONTENT && resultCode == RESULT_OK) {
            if (data.data != null) {
                clipPhoto(data.data!!)
            }
        } else if (requestCode == REQUEST_IMAGE_CLIP && resultCode == RESULT_OK) {
            imageUri = data.data!!
        }

    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_GET_CONTENT = 2
        const val REQUEST_IMAGE_CLIP = 3
    }

}