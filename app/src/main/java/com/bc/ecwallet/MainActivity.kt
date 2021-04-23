package com.bc.ecwallet

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.bc.ecwallet.utils.AWSApiCaller
import com.bc.ecwallet.utils.NetworkConnectionChecker


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            val config = AmplifyConfiguration.builder(applicationContext).devMenuEnabled(false).build()
            Amplify.configure(config,applicationContext)

            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }

        Amplify.Auth.fetchAuthSession(
            { result -> Log.i("AmplifyQuickstartSession", result.toString()) },
            { error -> Log.e("AmplifyQuickstartSession", error.toString()) }
        )

        AWSApiCaller.init(this)

        NetworkConnectionChecker.init(this)

        setContentView(R.layout.activity_main)

        val navController= Navigation.findNavController(this,R.id.nav_fragment)
        val navGraph=navController.navInflater.inflate(R.navigation.nav_graph)

        val loginned = Amplify.Auth.currentUser != null
        if (!loginned) {
            navGraph.startDestination=R.id.StartUpFragment
        } else {
            navGraph.startDestination=R.id.mainFragment
        }

        navController.graph = navGraph
    }
}