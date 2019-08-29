package com.wmontgom.personsrepo

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.wmontgom.personsrepo.api.DBHelper
import com.wmontgom.personsrepo.dao.PersonDao
import com.wmontgom.personsrepo.dao.PersonDao_Impl
import com.wmontgom.personsrepo.model.Person
import com.wmontgom.personsrepo.viewmodel.PersonsViewModel

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator


// @TODO implement swipe to delete
// @TODO allow manual inputting of a person
// @TODO add ability to edit person
// @TODO implement clear setting
// @TODO implement search
// @TODO implement sorting
// @TODO implement reordering

class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var personsViewModel: PersonsViewModel
    private lateinit var adapter : PersonsAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var persons : List<Person>? = null

    private val job by lazy { Job() }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        refreshPersons()

        linearLayoutManager = LinearLayoutManager(this)
        persons_list.layoutManager = linearLayoutManager

        adapter = PersonsAdapter()
        persons_list.adapter = adapter

        //create instance of view model factory
        val viewModelFactory = PersonsViewModelFactory()

        //Use view ModelFactory to initialize view model
        personsViewModel = ViewModelProviders.of(this@MainActivity, viewModelFactory).get(PersonsViewModel::class.java)

        //observe viewModel live data
        personsViewModel.personsLiveData.observe(this, Observer {
            it?.let {
                refreshPersons(it)
            } ?: run {
                // display error
                val builder = AlertDialog.Builder(this@MainActivity)

                builder.setTitle("Whoops!")
                builder.setMessage("An error has occurred. Please try again.")
                builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

                val dialog: AlertDialog = builder.create()

                loading.visibility = View.GONE
                dialog.show()
            }
        })


        // throttle the fab so that the animation can complete before another click is available
        val menuAction: (View) -> Unit = throttleFirst(350L, MainScope(), this::hideShowActionMenu)
        fab.setOnClickListener(menuAction)

        // throttle person creation actions
        val personActon: (View) -> Unit = throttleFirst(500L, MainScope(), this::createPerson)
        create_action.setOnClickListener(personActon)
        random_action.setOnClickListener(personActon)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // kills all coroutines under the scope
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

    /**
     * Hides or shows the action menu based on its current visibility
     */
    fun hideShowActionMenu(v : View) {
        if (action_menu.width > 0) {
            hideActionMenu()
        } else {
            showActionMenu()
        }
    }

    /**
     * Shows the action menu
     */
    fun showActionMenu() {
        if (action_menu.width == 0) { // do nothing if it's already expanded
            animateActionMenu(0, 140.px(), 0, 100.px(), 0f, -45f)
        }
    }

    /**
     * Hides the action menu
     */
    fun hideActionMenu() {
        if (action_menu.width > 0) { // do nothing if it's already collapsed
            animateActionMenu(140.px(), 0, 100.px(), 0, -45f, 0f)
        }
    }

    /**
     * Animates the expansion or collapsion (yes, i know that's not a word) of the action menu
     */
    fun animateActionMenu(ws: Int, we: Int, hs: Int, he: Int, rs: Float, re: Float) {
        val wa = ValueAnimator.ofInt(ws, we)
        wa.addUpdateListener { action_menu.newWidth(it.animatedValue as Int) }

        val ha = ValueAnimator.ofInt(hs, he)
        ha.addUpdateListener { action_menu.newHeight(it.animatedValue as Int) }

        val ra = ValueAnimator.ofFloat(rs, re)
        ra.addUpdateListener { fab.rotation = it.animatedValue as Float }

        val animset = AnimatorSet()
        animset.play(wa).with(ha).with(ra)
        animset.duration = 300
        animset.start()
    }

    /**
     * Sends the user to a new screen to manually input person data or randomly generates one from the randomuser API
     */
    fun createPerson(v: View) {
        hideActionMenu()

        if (v == create_action) {

        } else {
            // generate random user
            loading.visibility = View.VISIBLE
            personsViewModel.getRandomPerson()
        }
    }

    /**
     * Adds the new person to the DB (if exists) and refreshes the persons list
     * @param Person?
     */
    fun refreshPersons(p : Person? = null) {
        launch { // coroutine on Main
            val query = async(Dispatchers.IO) {
                // insert returned person into db
                DBHelper.getInstance(this@MainActivity)?.personDao()?.let { pd ->
                    p?.let {
                        it.fill()
                        pd.insert(it)
                    }
                    persons = pd.getPeople()
                }
            }

            query.await()

            loading.visibility = View.GONE

            // update recycler view
            adapter.notifyDataSetChanged()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class PersonsViewModelFactory : ViewModelProvider.NewInstanceFactory(){
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PersonsViewModel() as T
        }
    }

    /**
     * RecyclerView Adapter
     */
    inner class PersonsAdapter : RecyclerView.Adapter<PersonHolder>() {
        override fun getItemCount(): Int {
            return persons?.size ?: 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonHolder {
            val inflatedView = parent.inflate(R.layout.recycler_view_holder, false)
            return PersonHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: PersonHolder, position: Int) {
            val p = persons?.get(position)

            p.let { holder.populate(it!!) }
        }
    }

    /**
     * PersonHolder class
     */
    inner class PersonHolder(v: View) : RecyclerView.ViewHolder(v) {
        val avatar : ImageView = v.findViewById(R.id.avatar)
        val name : TextView = v.findViewById(R.id.name)
        val address : TextView  = v.findViewById(R.id.address)
        val phone : TextView  = v.findViewById(R.id.phone)

        fun populate(p : Person) {
            p.avatarLarge?.let {
                Picasso.get().load(p.avatarLarge).into(avatar);
            } ?: run {
                avatar?.visibility = View.INVISIBLE
            }

            name.text = String.format("%s %s", p.firstName?.capitalize(), p.lastName?.capitalize())
            address.text = p.buildAddress().capitalize()
            phone.text = p.phone
        }
    }
}
