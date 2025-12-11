package dev.afgk.localsound.ui

import android.content.ComponentName
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.common.util.concurrent.MoreExecutors
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.ActivityMainBinding
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.navigation.NavigationGraph
import dev.afgk.localsound.ui.sync.SyncTracksViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val _TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private val tracksRepository = MyApplication.appModule.tracksRepository

    private val syncTracksViewModel: SyncTracksViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels {
        viewModelFactory {
            PlayerViewModel(tracksRepository)
        }
    }

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Uses view binding generated class for activity_main.xml, inflates it and then get the
         * .root view to set as content view.
         *
         * [More about view binding](https://developer.android.com/topic/libraries/view-binding?hl=en)
         */

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        /**
         * Setting up NavGraph for navigation
         *
         * [More about navigation](https://developer.android.com/guide/navigation/design)
         */

        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        navController = navHostFragment.navController

        NavigationGraph().setGraph(navController)

        setupPlayer()
        setupMiniPlayer()

        /**
         * Enable edge-to-edge and ensure that the app content won't be behind
         * system bars (i.e. status bar, navigation bar and captions bar), applying padding
         * to the root view (R.id.main).
         *
         * [More about edge-to-edge](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
         */

        WindowCompat.enableEdgeToEdge(window)

        val rootView: View = findViewById(R.id.main)

        /**
         * Used for backwards compatibility for SDK < 30.
         */
        ViewGroupCompat.installCompatInsetsDispatch(rootView)

        /**
         * This method sets a callback that will be called when window insets changes. It returns
         * WindowInsetsCompat.CONSUMED, to avoid padding in child views.
         */
        ViewCompat.setOnApplyWindowInsetsListener(rootView) insetsCb@{ v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            return@insetsCb WindowInsetsCompat.CONSUMED
        }
    }

    override fun onResume() {
        super.onResume()
        syncTracksIfChanged()
    }

    fun setupPlayer() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                playerViewModel.setPlayer(controllerFuture.get())
            },
            MoreExecutors.directExecutor()
        )
    }

    fun setupMiniPlayer() {
        val miniPlayer = binding.miniPlayer

        navController.addOnDestinationChangedListener { _, destination, _ ->
            miniPlayer.currentRoute = destination.route
        }

        lifecycleScope.launch {
            miniPlayer.bindViewModel(playerViewModel)
        }
    }

    private var lastExternalStorageGeneration: Long? = null
    private var lastExternalStorageVersion: String? = null

    fun syncTracksIfChanged() {
        val currentGeneration = MediaStore.getGeneration(
            this,
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )

        val currentVersion = MediaStore.getVersion(this)

        if (lastExternalStorageVersion != currentVersion || lastExternalStorageGeneration != currentGeneration)
            syncTracksViewModel.sync(this)

        if (lastExternalStorageGeneration != currentGeneration)
            lastExternalStorageGeneration = currentGeneration

        if (lastExternalStorageVersion != currentVersion)
            lastExternalStorageVersion = currentVersion
    }
}