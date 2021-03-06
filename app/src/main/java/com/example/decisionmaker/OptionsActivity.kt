package com.example.decisionmaker

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_options.*


class OptionsActivity : AppCompatActivity(), View.OnClickListener {

    companion object{
        const val OPTIONS_ACT_ROULETTE_LIST:String = "ROULETTE_LIST"
        const val OPTIONS_ACT_ROULETTE_TITLE:String = "ROULETTE_TITLE"
        const val OPTIONS_ACT_ROULETTE_UPD_OK:Int = 1
        const val TEXT_CONTENT:String = "TEXT_CONTENT"
    }

    //Adapter variable
    private lateinit var myAdapter: OptionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        //Adapter
        myAdapter = OptionsAdapter(intent.extras!!.getStringArrayList(OPTIONS_ACT_ROULETTE_LIST)!!)
        val title = intent.extras!!.getString(OPTIONS_ACT_ROULETTE_TITLE, resources.getString(R.string.EMPTY_STRING))
        if(title.isNotBlank()) editText_RouletteTitle.setText(title)

        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration (this, DividerItemDecoration.VERTICAL))


        editText_addOption.setOnKeyListener(object:View.OnKeyListener{
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    if(editText_addOption.text.isNotBlank()){
                        myAdapter.addOption(editText_addOption.text.trim().toString())
                        editText_addOption.text.clear()
                        editText_addOption.requestFocus()
                        return true
                    }
                }
                return false
            }
        })

        button_addOption.setOnClickListener(this)
        button_clearTitle.setOnClickListener(this)
        floatButton_ready.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.button_addOption -> {
                if(editText_addOption.text.isNotBlank()){
                    myAdapter.addOption(editText_addOption.text.trim().toString())
                    editText_addOption.text.clear()
                }
            }
            R.id.button_clearTitle -> {
                editText_RouletteTitle.text.clear()
            }
            R.id.floatButton_ready -> {
                //Go back to MainActivity and update the Roulette, if the list has at least 2 options
                checkMinOptions()
            }
        }
    }

    //Manage screen orientation changes
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(OPTIONS_ACT_ROULETTE_LIST, myAdapter.getOptions())
        outState.putString(TEXT_CONTENT, editText_addOption.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        myAdapter.loadNewData(savedInstanceState.getStringArrayList(OPTIONS_ACT_ROULETTE_LIST)!!)
        editText_addOption.setText(savedInstanceState.getString(TEXT_CONTENT))
    }

    //Menu overridden methods
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_optionsactivity, menu)
        val menuItem = menu?.findItem(R.id.menu_clear)
        val icon = ResourcesCompat.getDrawable(resources, R.drawable.outline_delete_black_48, theme)
        val styledArray = obtainStyledAttributes(R.style.Theme_DecisionMaker_TextButton, intArrayOf(R.attr.backgroundTint))
        val color = styledArray.getColor(0, Color.CYAN)
        styledArray.recycle()
        icon?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        menuItem?.icon = icon
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_clear -> {
                //Clear options -> List and Edit Text
                editText_addOption.text.clear()
                myAdapter.clear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Check options are at least two (2)
    private fun checkMinOptions(){
        if(myAdapter.itemCount < 2)
            Toast.makeText(this, resources.getString(R.string.MIN_OPTIONS_CONSTRAINT), Toast.LENGTH_LONG).show()
        else{
            if(editText_RouletteTitle.text.isBlank())
                editText_RouletteTitle.setText(resources.getString(R.string.ROULETTE_DEFAULT_TITLE))
            setResultAndReturnToActivity()
        }
    }

    /**
     * For better UX, detect back key pressed
     * and save entered values
     */
    override fun onBackPressed() {
        checkMinOptions()
        super.onBackPressed()
    }

    /**
     * Set listOptions as the activity result,
     * then finish the activity
     */
    private fun setResultAndReturnToActivity(){
        val result = Intent()
        result.putExtra(OPTIONS_ACT_ROULETTE_LIST, myAdapter.getOptions())
        result.putExtra(OPTIONS_ACT_ROULETTE_TITLE, editText_RouletteTitle.text.trim().toString())
        setResult(OPTIONS_ACT_ROULETTE_UPD_OK, result)
        finish()
    }



}


