package dev.afgk.localsound.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import dev.afgk.localsound.R
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository
import dev.afgk.localsound.data.emissordeEventos.Mudancas
import dev.afgk.localsound.databinding.ActivityMainBinding
import dev.afgk.localsound.ui.navigation.NavigationGraph

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaWatcher: Mudancas.MediaWatcher
    private lateinit var audioFilesRepository: AudioFilesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        // Initialize the watchers and repositories
        mediaWatcher = Mudancas.MediaWatcher(this)
        audioFilesRepository = AudioFilesRepository(this)

        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationGraph().setGraph(navController)

        WindowCompat.enableEdgeToEdge(window)

        val rootView: View = findViewById(R.id.main)

        ViewGroupCompat.installCompatInsetsDispatch(rootView)

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

    override fun onStart() {
        super.onStart()
        mediaWatcher.startWatching {
            Log.d("MediaWatcher", "MediaStore changed! Reloading audio files...")
            // Agora, quando uma mudança ocorre, busca a lista de músicas atualizada.
            val newAudioList = audioFilesRepository.loadFiles()
            Log.d("MediaWatcher", "Found ${newAudioList.size} audio files.")
            // PRÓXIMO PASSO: Entregue esta 'newAudioList' para o seu RecyclerView Adapter.
        }
    }

    override fun onStop() {
        super.onStop()
        mediaWatcher.stopWatching()
    }
}