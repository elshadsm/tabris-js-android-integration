package com.tabris.js.android.integration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.eclipsesource.tabris.android.TabrisFragment
import com.eclipsesource.tabris.android.boot.BootJsLoader
import com.eclipsesource.tabris.android.boot.BootJsResponse
import com.eclipsesource.tabris.android.boot.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

  private val projectPath = "http://192.168.1.104:8080"
//    Use the path below for the production app.
//    private val projectPath = "file:///android_asset/tabris-js-app"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    findViewById<Button>(R.id.button).setOnClickListener {
      launch()
    }
  }

  private fun launch() {
    GlobalScope.launch {
      BootJsLoader(application)
          .load(projectPath)
          .catch { showError(it.message ?: it.toString()) }
          .collect { withContext(Dispatchers.Main) { handleBoothJsResource(it) } }
    }
  }

  private fun showError(message: String) {
    Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show()
  }

  private fun handleBoothJsResource(resource: Resource<BootJsResponse>) {
    when (resource) {
      is Resource.Success -> showTabrisFragment(resource.data)
      is Resource.Failure -> showError("${resource.code} - ${resource.message}")
      else -> Unit
    }
  }

  private fun showTabrisFragment(response: BootJsResponse) {
    val tabrisFragment = newTabrisFragment(response)
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, tabrisFragment, tabrisFragment::class.java.name)
        .addToBackStack(null)
        .commit()
  }

  private fun newTabrisFragment(response: BootJsResponse): TabrisFragment {
    val tabrisFragment = TabrisFragment.newInstance(baseUri = response.baseUri)
    tabrisFragment.onScopeAvailable { scope ->
      scope.onClose { action ->
        println("TabrisFragment.onScopeAvailable: $action")
      }
      scope.boot(response.bootScripts)
    }
    return tabrisFragment
  }

}
