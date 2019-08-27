package com.wmontgom.personsrepo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.wmontgom.personsrepo.viewmodel.PersonsViewModel

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var personsViewModel: PersonsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //create instance of view model factory
        val viewModelFactory = PersonsViewModelFactory()

        //Use view ModelFactory to initialize view model
        personsViewModel = ViewModelProviders.of(this@MainActivity, viewModelFactory).get(PersonsViewModel::class.java)

        //observe viewModel live data
        personsViewModel.personsLiveData.observe(this, Observer {
            //bind your ui here
            it.fill()
            System.out.println("person:" + it)
        })

        fab.setOnClickListener { view ->
            personsViewModel.getRandomPerson()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getNewUser() {

    }

    @Suppress("UNCHECKED_CAST")
    class PersonsViewModelFactory : ViewModelProvider.NewInstanceFactory(){
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PersonsViewModel() as T
        }
    }
}
