package com.example.soilscout.ui.createeditfield

import android.content.Context // Додайте цей імпорт
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soilscout.R
import com.example.soilscout.util.LocaleManager // Додайте цей імпорт
import java.util.Locale // Додайте цей імпорт

class CreateEditFieldActivity : AppCompatActivity() {
    private var activityLocale: Locale? = null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let {
            activityLocale = Locale(LocaleManager.getLanguage())
            LocaleManager.applyLocale(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activityLocale == null) {
            activityLocale = Locale(LocaleManager.getLanguage())
        }

        setContentView(R.layout.activity_create_edit_field)

        supportActionBar?.let {
            if (intent.getIntExtra("field_id", -1) == -1) {
                it.title = getString(R.string.create_field_title)
            } else {
                it.title = getString(R.string.edit_field_title)
            }
            it.setDisplayHomeAsUpEnabled(true)
        }

        val fieldId = intent.getIntExtra("field_id", -1)
        if (savedInstanceState == null) {
            val fragment = CreateEditFieldFragment()
            val bundle = Bundle()
            bundle.putInt("field_id", fieldId)
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        val persistedLanguage = LocaleManager.getLanguage()
        if (activityLocale != null && activityLocale?.language != persistedLanguage) {
            recreate()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}