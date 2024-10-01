import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.SDL2.*

@Serializable
private data class Message(
    val topic: String,
    val content: String,
)

private val PrettyPrintJson = Json {
    prettyPrint = true
}

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    runBlocking {
        val job = async(Dispatchers.Default) {
            delay(1000)
            println("Kotlin Coroutines World!")
        }
        val message = Message(
            topic = "Kotlin/Native",
            content = "Hello!"
        )
        println(PrettyPrintJson.encodeToString(message))

        val result = SDL_Init(SDL_INIT_EVERYTHING)
        if(result < 0) {
            error("Failed to init SDL")
        }

        var isRunning = true
        val flags = SDL_WINDOW_RESIZABLE
        val window = SDL_CreateWindow("", 0,0,1280,720, flags)
        val renderer = SDL_CreateRenderer(window, 0, 0u)

        SDL_ShowWindow(window)

        while(isRunning) {
            val event = nativeHeap.alloc<SDL_Event>()
            while (SDL_PollEvent(event.ptr) != 0) {
                when (event.type) {
                    SDL_QUIT -> { isRunning = false }
                    SDL_KEYUP -> {
                        if(event.key.keysym.sym == SDLK_ESCAPE.toInt()) {
                            isRunning = false
                        }
                    }
                }
            }

            SDL_RenderClear(renderer)
            SDL_RenderPresent(renderer)
        }

        println("Hello, Kotlin/Native! The SDL2 version " +
                "is ${platform.SDL2.SDL_GetRevision()?.toKString()}")
        job.await()
    }
}