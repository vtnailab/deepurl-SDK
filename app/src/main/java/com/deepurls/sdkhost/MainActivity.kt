package com.deepurls.sdkhost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.deepurls.sdkhost.ui.theme.AndroidSdkHostTheme
import com.deepurls.deepurls_sdk.DeepUrls
import android.util.Log
import com.deepurls.sdkhost.navigation.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeepUrls.init(this)
        val params = mapOf("utm_source" to "app", "campaign" to "test123")
        DeepUrls.createLink(route = "home", params = params)
        { success, url, longUrl ->
            if (success) {
                Log.d("MyApp", "Short URL = $url, Long URL = $longUrl")
            } else {
                Log.e("MyApp", "Failed to create deep link")
            }
        }
        enableEdgeToEdge()
        setContent {
            AndroidSdkHostTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidSdkHostTheme {
        Greeting("Android")
    }
}