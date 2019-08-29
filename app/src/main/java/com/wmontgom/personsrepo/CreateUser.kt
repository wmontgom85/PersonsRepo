package com.wmontgom.personsrepo

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_create_user.*
import java.io.IOException

class CreateUser : AppCompatActivity() {
    private val CAMERA_SCAN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        setSupportActionBar(toolbar)
    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_SCAN) {
            if (data != null) {
                val contentURI = data!!.data
                try {
                    val bitmap = data.extras!!.get("data") as Bitmap

                    // imageview!!.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@CreateUser, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
