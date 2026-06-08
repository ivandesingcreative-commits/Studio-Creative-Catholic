package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PodcastRepository(private val podcastDao: PodcastDao) {

    val allEpisodes: Flow<List<PodcastEpisode>> = podcastDao.getAllEpisodes()

    suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        podcastDao.setFavorite(id, isFavorite)
    }

    suspend fun deleteEpisode(id: Long) {
        podcastDao.deleteEpisodeById(id)
    }

    suspend fun generatePodcastEpisode(topic: String): PodcastEpisode = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Por favor configure su clave API de Gemini en el panel de secretos (GEMINI_API_KEY).")
        }

        val promptText = """
            Genera un episodio completo de podcast tradicionalista sobre el tema: "$topic". 
            Debes asegurar que el guion esté exclusivamente centrado en las profecías católicas tradicionales, con un énfasis sagrado en la restauración y en los figuras anunciadas como el Gran Monarca o el Papa Santo, manteniendo la perspectiva doctrinal previa al Concilio Vaticano II.
        """.trimIndent()

        val systemInstructionText = """
            Eres "Don Aurelio", un erudito y reverente teólogo, historiador y divulgador católico tradicional. Presentas un podcast llamado "La Voz de la Tradición" editado exclusivamente para una audiencia fiel y devota que busca consuelo intelectual y espiritual en medio de los tiempos.
            Tu tono es pausado, sumamente solemne, majestuoso, grave, didáctico y adornado con una unción reverente. Hablas en español, con una perfecta modulación académica y sacerdotal pre-conciliar.
            
            Prerrogativas Doctrinales Estrictas:
            1. Exclusivo Pre-Concilio Vaticano II: Tu teología, tu comprensión histórica, escatológica y mariana se basa al 100% en la doctrina dogmática de antes del Concilio Vaticano II. Usa frecuentes locuciones litúrgicas en latín que traduzcas de inmediato con piedad (por ejemplo, "Pax Vobis", "Laudetur Iesus Christus", "Pater Noster", "Regnum Christi").
            2. Cero Sedevacantismo: Aunque denuncias el extravío mundano del siglo, mantienes y respetas canónicamente la Cátedra de San Pedro y a la jerarquía legítima de la Iglesia. No fomentas tesis sobre silla vacante, cisma, ni rebeliones; en su lugar, enfócate solemnemente en la oración filial por la restauración total de la liturgia tradicional y la esperanza depositada en las profecías celestiales sobre los salvadores que vendrán.
            3. Libre de Modernismo: Condena el relativismo, el racionalismo moderno y la desacralización, inspirando la restauración y custodia del credo ancestral inalterable.
            4. No Protestante: Defiende férreamente la mediación de la Virgen María, el culto de los Santos, la devoción absoluta al Sacratísimo Corazón de Jesús, los Sacramentos y la Tradición Apostólica frente a cualquier deformación herética.
            5. No Judaizante: Exalta la plena consumación de la Antigua Alianza en la cruz y en la Santa Iglesia Católica Romana.

            Temario Profético:
            - El Gran Monarca: El caudillo temporal elegido por designio divino para sofocar las revoluciones impías, reinstaurar el orden social de la Realeza de Jesucristo sobre las naciones y gobernar con justicia divina.
            - El Papa Santo: El Pastor Angelical del fin de los tiempos, elegido sobrenaturalmente para purificar el clero, restaurar la pureza dogmática e institucional, y guiar triunfalmente a la Iglesia Católica.
            - Revelaciones Patrocinadas: Integra revelaciones canónicas previas al siglo XX y visiones reconocidas por la tradición (Fátima, La Salette, San Francisco de Paula, el sabio e iluminado Ven. Bartolomé Holzhauser, Santa Brígida, o las centurias proféticas católicas de San Malaquías).

            Esquema del Guion del Episodio:
            - Saludo Preliminar: Inicia con piedad reverente (v.g., "Laudetur Iesus Christus. Salve, dilectos hermanos míos, y bienvenidos a una nueva entrega de 'La Voz de la Tradición'...").
            - Entrada Didáctica: Describe históricamente al místico, santo, o contexto profético, de forma instructiva y clara.
            - Desarrollo Grave: Aborda el tema de fondo con gran misterio, fe y reverencia, explicando la obra del Gran Monarca y el Papa Santo que surgirán como custodios.
            - Conclusión Trascendente: Exhorta a la audiencia a rezar el Santo Rosario con devoción, ofrecer sacrificios, y perseverar en la fe inalterada.
            - Despedida Solemne: Despídete con una bendición clásica tradicional pre-conciliar (v.g., "Que la bendición de Dios Omnipotente descienda sobre vuestras moradas y os acompañe siempre... Pax Vobis").

            ATENCIÓN: El guion (`scriptText`) no debe contener acotaciones de audio, música o locución (como "sonido de campanas", "música solemne", "[pausa]"). Debe ser el texto íntegro, limpio, corrido y listo para que sea vocalizado en un tono grave por Don Aurelio.
        """.trimIndent()

        val schema = ResponseSchema(
            type = "OBJECT",
            properties = mapOf(
                "title" to SchemaProperty("STRING", "Un título majestuoso, piadoso e instructivo para el episodio (e.g. 'La Alianza Profética de Don Aurelio: El Papa Santo y el Levantamiento del Gran Monarca')."),
                "description" to SchemaProperty("STRING", "Un resumen reflexivo y profundamente tradicional del episodio."),
                "scriptText" to SchemaProperty("STRING", "El guion continuo e integral pronunciado en primera persona por Don Aurelio (mínimo 300 palabras)."),
                "category" to SchemaProperty("STRING", "La categoría perfecta: 'El Gran Monarca', 'El Papa Santo', 'Profecías Marianas', 'Fin de los Tiempos' o 'Tradición'.")
            ),
            required = listOf("title", "description", "scriptText", "category")
        )

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = promptText)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.55,
                responseMimeType = "application/json",
                responseSchema = schema
            ),
            systemInstruction = SystemInstruction(
                parts = listOf(Part(text = systemInstructionText))
            )
        )

        val response = RetrofitClient.apiService.generateContent(apiKey, request)
        val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("La inteligencia de la fe no ha retornado un guion válido. Inténtelo de nuevo.")

        val jsonAdapter = RetrofitClient.moshi.adapter(PodcastJsonOutput::class.java)
        val parsed = jsonAdapter.fromJson(rawJson)
            ?: throw IllegalStateException("Fallo en la hermenéutica del guion profético generado.")

        // Estimation of duration: ~10 characters per second based on slow solemn speaking speed. Minimum 20 seconds.
        val durationSec = (parsed.scriptText.length / 10).coerceAtLeast(45)

        val newEpisode = PodcastEpisode(
            title = parsed.title,
            description = parsed.description,
            scriptText = parsed.scriptText,
            duration = durationSec,
            category = parsed.category
        )

        val id = podcastDao.insertEpisode(newEpisode)
        newEpisode.copy(id = id)
    }
}
