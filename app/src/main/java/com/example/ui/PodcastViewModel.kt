package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PodcastViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = PodcastRepository(database.podcastDao())
    val voiceEngine = DonAurelioVoiceEngine(application)

    // UI state flows
    val episodes: StateFlow<List<PodcastEpisode>> = repository.allEpisodes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentlyPlaying = MutableStateFlow<PodcastEpisode?>(null)
    val currentlyPlaying: StateFlow<PodcastEpisode?> = _currentlyPlaying

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState

    val voicePlaybackState: StateFlow<PlaybackState> = voiceEngine.playbackState

    init {
        seedDatabaseIfNeeded()
    }

    private fun seedDatabaseIfNeeded() {
        viewModelScope.launch {
            val count = database.podcastDao().getEpisodeCount()
            if (count == 0) {
                // Seed 1: El Gran Monarca
                database.podcastDao().insertEpisode(
                    PodcastEpisode(
                        title = "La Revelación del Gran Monarca",
                        description = "Don Aurelio expone las antiguas profecías de San Francisco de Paula y el Ven. Holzhauser sobre el Restaurador prometido.",
                        scriptText = """
                            Laudetur Iesus Christus. Salve, dilectos hermanos míos en la fe verdadera, y sean bienvenidos a una nueva entrega de "La Voz de la Tradición". Os habla su humilde servidor, Don Aurelio, transmitiendo con el corazón rebosante de esperanza en estos tiempos de profunda y misteriosa confusión.

                            Hoy dirigiremos nuestras plegarias y nuestro entendimiento hacia una de las realidades proféticas más consoladoras y hermosas de la cristiandad tradicional, transmitida por grandes santos y doctores pre-conciliares: el advenimiento del Gran Monarca.

                            San Francisco de Paula, en el siglo XV, nos anunciaba la venida de un caudillo escogido por el Altísimo. Este líder temporal, dotado de gran valentía militar y sobre todo de un fervor purísimo, será el encargado de aplastar las corrientes del modernismo e instaurar el reinado social de Cristo Rey sobre las naciones. Es lo que el ilustre exégeta Venerable Bartolomé Holzhauser denominó el período del Quinto Estado de la Iglesia, un tiempo de gran Consolación posterior a la tribulación que hoy aqueja a nuestra amada patria espiritual.

                            No caigamos, por tanto, en el desánimo mundano. El Gran Monarca no es una leyenda secular; es la manifestación temporal de que Dios no abandona a su pueblo fiel. Él unirá los reinos católicos bajo el cetro de la cruz, restaurará el prestigio de la liturgia sagrada del Santo Altar, y devolverá formalmente a Dios lo que es de Dios.

                            Que esta certeza profética inflame vuestros corazones de fe y piedad. Rezad vuestros Santos Rosarios diariamente y ofreced penitencias por la manifestación de este regio siervo celestial.

                            Que la bendición de Dios Omnipotente sea con vuestras familias. Pax Vobis.
                        """.trimIndent(),
                        duration = 110,
                        category = "El Gran Monarca"
                    )
                )

                // Seed 2: El Papa Santo
                database.podcastDao().insertEpisode(
                    PodcastEpisode(
                        title = "El Pastor Angelical: Las Profecías del Papa Santo",
                        description = "Un examen teológico de las revelaciones de santos y el Secreto de Fátima en torno al Pontífice restaurador.",
                        scriptText = """
                            Laudetur Iesus Christus. Sean bienvenidos, mis queridos hermanos en la santa fe tradicional, a una nueva reflexión en "La Voz de la Tradición". Les saluda, con la debida unción, su servidor de siempre, Don Aurelio.

                            En medio de las aguas embravecidas que parecen azotar la barca de Pedro, es menester que los fieles alcemos la mirada hacia las profecías católicas sobre el Papa Santo, conocido en los códices tradicionales como el Pastor Angelicus.

                            Santos anteriores al Concilio Vaticano II contemplaron en visiones la restauración de la Sede Romana. Este excelso varón, lleno de unción y firme contra toda herejía modernista, no cuestionará la Cátedra sagrada, pues respetamos al Papa actual en fidelidad canónica. Su misión divina será purificar el clero, restaurar en todo su esplendor medieval los ritos sagrados de la Tradición Apostólica, y congregar de nuevo a las ovejas dispersas en el rebaño del Señor.

                            Inspirado por el triunfo mariano profetizado en Fátima y La Salette, el Papa Santo re-impondrá la majestad de los Sacramentos tradicionales. Su pontificado gobernará en perfecta armonía junto al Gran Monarca, atando el relativismo modernista y uniendo a todo el orbe cristiano bajo el dogma de la fe indivisible.

                            Por ello, hermanos míos, nuestra respuesta no es la rebelión ni la indiferencia protestante, sino la preservación de la doctrina heredada y la oración constante. Que vuestras preces diarias imploren la consumación de este misterio de gracia y reparación celestial.

                            Que la paz de nuestro Señor Jesucristo permanezca con vosotros. Pax Vobis.
                        """.trimIndent(),
                        duration = 125,
                        category = "El Papa Santo"
                    )
                )

                // Seed 3: San Malaquías y los Últimos Tiempos
                database.podcastDao().insertEpisode(
                    PodcastEpisode(
                        title = "La Profecía de los Papas de San Malaquías",
                        description = "Análisis tradicional del listado de divisas atribuidas al monje medieval sobre la sucesión petrina.",
                        scriptText = """
                            Laudetur Iesus Christus. Bienvenidos, carísimos hermanos en la perseverancia de la fe, a este vuestro pulpito formativo de "La Voz de la Tradición". Os acompaña, bajo la mirada del Altísimo, vuestro servidor Don Aurelio.

                            Hoy abordaremos de forma instructiva y con gran gravedad espiritual las célebres divisas del arzobispo medieval San Malaquías. Se trata de un listado venerable de ciento doce divisas en latín que describen la sucesión de los Romanos Pontífices hasta el fin de los tiempos modernos.

                            Cada lema asignado a cada papa encierra un misterio de incalculable sabiduría. Historiadores católicos tradicionales previos al Concilio Vaticano Segundo siempre examinaron estas profecías no con sensacionalismo estéril de secta protestante, sino con la piedad y ciencia teológica debidas. Tras el transcurso de las centurias, el fin de la lista se avecina, anunciando la llegada del último lema asignado como 'Petrus Romanus', Pedro el Romano, quien apacentará a su grey bajo severas persecuciones.

                            Esta profecía tradicional no pretende incitarnos al pánico secular, sino a la más profunda introspección intelectual y moral. Nos recuerda la transitoriedad cósmica del poder terrenal y el triunfo inconmovible de la Esposa de Cristo. En tiempos de tiniebla, mantengamos encendida la antorcha de la fe dogmática recibida de nuestros santos antepasados.

                            Perseverad en la asiduidad del rezo del Santo Rosario, la gran muralla espiritual de nuestra era.

                            Pido para vosotros la santa protección mariana y divina. Pax Vobis.
                        """.trimIndent(),
                        duration = 135,
                        category = "Fin de los Tiempos"
                    )
                )
            }
        }
    }

    fun playEpisode(episode: PodcastEpisode) {
        _currentlyPlaying.value = episode
        voiceEngine.speak(episode.scriptText)
    }

    fun pauseEpisode() {
        voiceEngine.pause()
    }

    fun resumeEpisode() {
        _currentlyPlaying.value?.let {
            voiceEngine.speak(it.scriptText)
        }
    }

    fun stopEpisode() {
        voiceEngine.stop()
    }

    fun toggleFavorite(episode: PodcastEpisode) {
        viewModelScope.launch {
            repository.setFavorite(episode.id, !episode.isFavorite)
            // Update current playing references if needed
            if (_currentlyPlaying.value?.id == episode.id) {
                _currentlyPlaying.value = _currentlyPlaying.value?.copy(isFavorite = !episode.isFavorite)
            }
        }
    }

    fun deleteEpisode(episode: PodcastEpisode) {
        viewModelScope.launch {
            if (_currentlyPlaying.value?.id == episode.id) {
                stopEpisode()
                _currentlyPlaying.value = null
            }
            repository.deleteEpisode(episode.id)
        }
    }

    fun generateNewEpisode(topic: String) {
        if (topic.isBlank()) return
        
        _generationState.value = GenerationUiState.Loading
        
        viewModelScope.launch {
            try {
                val newEpisode = repository.generatePodcastEpisode(topic)
                _generationState.value = GenerationUiState.Success(newEpisode)
                _currentlyPlaying.value = newEpisode
            } catch (e: Exception) {
                _generationState.value = GenerationUiState.Error(e.localizedMessage ?: "Ocurrió un error inesperado al conectar con el servidor.")
            }
        }
    }

    fun clearGenerationState() {
        _generationState.value = GenerationUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}

sealed class GenerationUiState {
    object Idle : GenerationUiState()
    object Loading : GenerationUiState()
    data class Success(val episode: PodcastEpisode) : GenerationUiState()
    data class Error(val message: String) : GenerationUiState()
}
