package com.wmontgom.personsrepo

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_create_user.toolbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_create_user.*
import kotlinx.coroutines.MainScope
import java.io.IOException

class CreateUser : AppCompatActivity() {
    private val CAMERA_SCAN = 1
    private val CAMERA_PERMISSION = 100
    private val READ_EXT_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        setSupportActionBar(toolbar)

        val cameraAction: (View) -> Unit = throttleFirst(350L, MainScope(), this::checkCameraPermission)
        select_avatar.setOnClickListener(cameraAction)

    }

    private fun setupForm() {
        first_name.error = "Please enter a first name"
        last_name.error = "Please enter a last name"
    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_SCAN) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = data.extras!!.get("data") as Bitmap

                    avatar.setImageBitmap(bitmap)

                    placeholder.visibility = View.GONE
                    plus_icon.visibility = View.GONE
                    avatar.visibility = View.VISIBLE
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@CreateUser, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun presentCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_SCAN)
    }

    private fun checkCameraPermission(v : View) {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        } else {
            checkReadPermission()
        }
    }

    private fun checkReadPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestReadExternalPermission()
        } else {
            presentCamera()
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
    }

    private fun requestReadExternalPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXT_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Permission has been denied by user
                    showMessage("Whoops!", "Please allow camera permission to upload photos")
                } else {
                    // Permission has been granted by user
                    checkReadPermission()
                }
            }
            READ_EXT_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Permission has been denied by user
                    showMessage("Whoops!", "Please allow read permission to upload photos")
                } else {
                    // Permission has been granted by user
                    presentCamera()
                }
            }
        }
    }

    private fun showMessage(title: String, message: String) {
        val builder = AlertDialog.Builder(this@CreateUser)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }
}
