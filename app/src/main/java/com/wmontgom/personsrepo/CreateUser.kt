package com.wmontgom.personsrepo

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.wmontgom.personsrepo.api.DBHelper
import com.wmontgom.personsrepo.model.Person

import kotlinx.android.synthetic.main.activity_create_user.toolbar
import kotlinx.android.synthetic.main.content_create_user.*
import kotlinx.coroutines.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class CreateUser : AppCompatActivity(), CoroutineScope {
    private val CAMERA_SCAN = 1
    private val CAMERA_PERMISSION = 100
    private val READ_EXT_PERMISSION = 101
    private var person : Person? = null

    private val job by lazy { Job() }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        setSupportActionBar(toolbar)

        val cameraAction: (View) -> Unit = throttleFirst(350L, MainScope(), this::checkCameraPermission)
        select_avatar.setOnClickListener(cameraAction)

        val personId = intent.getLongExtra("personId", 0)

        if (personId > 0) {
            // we're editing a person
            loading_person.visibility = View.VISIBLE

            // load user in background thread
            getPerson(personId)
        }
    }

    private fun setupForm() {
        first_name.error = "Please enter a first name"
        last_name.error = "Please enter a last name"
    }

    private fun getPerson(pId: Long) {
        launch { // coroutine on Main
            val query = async(Dispatchers.IO) {
                // insert returned person into db
                DBHelper.getInstance(this@CreateUser)?.personDao()?.let { pd ->
                    person = pd.getPerson(pId)
                }
            }

            query.await()

            // fill inputs
            populateForm()
        }
    }

    private fun populateForm() {
        person?.let { p ->
            first_name.setText(p.firstName)
            last_name.setText(p.lastName)
            address.setText(p.street)
            city.setText(p.city)
            state.setText(p.state)
            zip.setText(p.postcode)
            phone.setText(p.phone)
            cell.setText(p.cell)

            p.avatarLarge?.let {
                Picasso.get().load(it).into(avatar)
                placeholder.visibility = View.GONE
                plus_icon.visibility = View.GONE
                avatar.visibility = View.VISIBLE
            } ?: run {
                avatar.visibility = View.GONE
                placeholder.visibility = View.VISIBLE
                plus_icon.visibility = View.VISIBLE
            }
        }

        loading_person.visibility = View.GONE
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
