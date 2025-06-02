package com.example.soilscout.ui.fielddetails

import android.content.Context // Додайте цей імпорт
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soilscout.R
import com.example.soilscout.util.LocaleManager // Додайте цей імпорт
import java.util.Locale // Додайте цей імпорт

class FieldDetailsActivity : AppCompatActivity() {
    private var activityLocale: Locale? = null // Зберігатимемо поточну локаль Activity

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

        setContentView(R.layout.activity_field_details)

        supportActionBar?.let {
            it.title = getString(R.string.field_details_title)
            it.setDisplayHomeAsUpEnabled(true)
        }

        val fieldId = intent.getIntExtra("field_id", -1)
        if (savedInstanceState == null) {
            val fragment = FieldDetailsFragment()
            val bundle = Bundle()
            bundle.putInt("field_id", fieldId)
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_details, fragment)
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