package com.gregorypina.delamain.domain

import java.time.Clock
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Local phrase templates keyed by tone. Facts and placeholders stay typed; only style varies. */
internal object LocalPhraseBank {
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun intentVariants(
        intent: LocalIntent,
        tone: PersonalityTone,
        displayName: String?,
        clock: Clock,
    ): List<String> = when (intent) {
        LocalIntent.HELP -> toneVariants(
            tone,
            warm = listOf(
                "À disposição. Experimente: que horas são, bateria, aumente o volume ou abra Spotify.",
                "Posso ajudar com comandos locais. Diga: próxima música, música anterior ou diminua o volume.",
                "Meu repertório é local. Experimente: bateria, abra Maps ou modo mute. Para voltar à fala, diga ativar voz.",
            ),
            direct = listOf(
                "Comandos: que horas são, bateria, aumente o volume, abra Spotify.",
                "Experimente: próxima música, música anterior ou diminua o volume.",
                "Comandos: abra Maps, modo mute ou ativar voz.",
            ),
        )
        LocalIntent.CALL -> toneVariants(
            tone,
            warm = listOf("À disposição.", "Pois não?", "Sim?"),
            direct = listOf("Sim.", "Pronta.", "Diga."),
        )
        LocalIntent.PRESENCE -> presenceVariants(tone, displayName)
        LocalIntent.GREETING -> toneVariants(
            tone,
            warm = listOf(
                "Olá. Pronto para a próxima viagem?",
                "Saudações. À disposição.",
                "Olá. Como posso ajudar?",
            ),
            direct = listOf(
                "Olá. Pronto.",
                "Olá.",
                "Em que posso ajudar?",
            ),
        )
        LocalIntent.THANKS -> toneVariants(
            tone,
            warm = listOf("É um prazer.", "Sempre às ordens.", "Por nada."),
            direct = listOf("De nada.", "Disponível.", "Por nada."),
        )
        LocalIntent.TIME -> {
            val time = LocalTime.now(clock).format(TIME_FORMATTER)
            toneVariants(
                tone,
                warm = listOf("São $time.", "Agora são $time.", "O horário agora é $time."),
                direct = listOf("São $time.", "$time.", "Agora: $time."),
            )
        }
        LocalIntent.BATTERY_STATUS,
        LocalIntent.VOLUME_UP,
        LocalIntent.VOLUME_DOWN,
        LocalIntent.MEDIA_NEXT,
        LocalIntent.MEDIA_PREVIOUS,
        LocalIntent.OPEN_APP,
        -> error("Action responses depend on the observed result")
    }

    fun presenceVariants(tone: PersonalityTone, displayName: String?): List<String> =
        if (displayName == null) {
            toneVariants(
                tone,
                warm = listOf("Sempre a postos.", "Estou aqui.", "Presente."),
                direct = listOf("Aqui.", "Presente.", "Sim."),
            )
        } else {
            toneVariants(
                tone,
                warm = listOf(
                    "Sempre a postos, $displayName.",
                    "Estou aqui, $displayName.",
                    "Presente, $displayName.",
                ),
                direct = listOf(
                    "Aqui, $displayName.",
                    "Presente, $displayName.",
                    "Sim, $displayName.",
                ),
            )
        }

    fun variants(key: String, tone: PersonalityTone): List<String> = when (key) {
        LocalCommandEngine.KEY_BATTERY_UNAVAILABLE -> toneVariants(
            tone,
            warm = listOf(
                "Não consigo ler a bateria neste momento.",
                "O status da bateria está indisponível agora.",
                "Sem leitura de bateria por enquanto.",
            ),
            direct = listOf(
                "Bateria indisponível agora.",
                "Sem leitura de bateria.",
                "Não consigo ler a bateria.",
            ),
        )
        LocalCommandEngine.KEY_BATTERY_CHARGING -> toneVariants(
            tone,
            warm = listOf(
                "Bateria em {percent}% e carregando.",
                "Restam {percent}% de bateria, carregando agora.",
                "{percent}% de bateria, com carga em andamento.",
            ),
            direct = listOf(
                "{percent}% e carregando.",
                "Bateria em {percent}%, carregando.",
                "{percent}%, em carga.",
            ),
        )
        LocalCommandEngine.KEY_BATTERY_LEVEL -> toneVariants(
            tone,
            warm = listOf(
                "Bateria em {percent}%.",
                "Restam {percent}% de bateria.",
                "Nível de bateria: {percent}%.",
            ),
            direct = listOf(
                "Bateria em {percent}%.",
                "{percent}%.",
                "Restam {percent}%.",
            ),
        )
        LocalCommandEngine.KEY_VOLUME_UP_CHANGED -> toneVariants(
            tone,
            warm = listOf("Volume aumentado.", "Subi o volume.", "Volume um pouco mais alto."),
            direct = listOf("Volume maior.", "Volume aumentado.", "Mais alto."),
        )
        LocalCommandEngine.KEY_VOLUME_DOWN_CHANGED -> toneVariants(
            tone,
            warm = listOf("Volume reduzido.", "Abaixei o volume.", "Volume um pouco mais baixo."),
            direct = listOf("Volume menor.", "Volume reduzido.", "Mais baixo."),
        )
        LocalCommandEngine.KEY_VOLUME_UP_AT_LIMIT -> toneVariants(
            tone,
            warm = listOf(
                "O volume já está no máximo.",
                "Não há mais volume para aumentar.",
                "Já estamos no volume máximo.",
            ),
            direct = listOf(
                "Volume no máximo.",
                "Já está no máximo.",
                "Sem mais volume para subir.",
            ),
        )
        LocalCommandEngine.KEY_VOLUME_DOWN_AT_LIMIT -> toneVariants(
            tone,
            warm = listOf(
                "O volume já está no mínimo.",
                "Não há mais volume para reduzir.",
                "Já estamos no volume mínimo.",
            ),
            direct = listOf(
                "Volume no mínimo.",
                "Já está no mínimo.",
                "Sem mais volume para baixar.",
            ),
        )
        LocalCommandEngine.KEY_VOLUME_FIXED -> toneVariants(
            tone,
            warm = listOf(
                "O volume deste dispositivo é fixo.",
                "Este aparelho não permite ajuste de volume.",
                "O volume aqui é fixo, não consigo alterar.",
            ),
            direct = listOf(
                "Volume fixo neste aparelho.",
                "Não consigo ajustar o volume aqui.",
                "Volume fixo.",
            ),
        )
        LocalCommandEngine.KEY_VOLUME_UNAVAILABLE -> toneVariants(
            tone,
            warm = listOf(
                "Controle de volume indisponível.",
                "Não consigo ajustar o volume agora.",
                "O controle de volume está indisponível.",
            ),
            direct = listOf(
                "Volume indisponível.",
                "Sem controle de volume agora.",
                "Não consigo ajustar o volume.",
            ),
        )
        LocalCommandEngine.KEY_VOLUME_DENIED -> toneVariants(
            tone,
            warm = listOf(
                "Sem permissão para ajustar o volume.",
                "Não tenho permissão para mudar o volume.",
                "O sistema não autorizou o ajuste de volume.",
            ),
            direct = listOf(
                "Sem permissão de volume.",
                "Volume não autorizado.",
                "Permissão de volume negada.",
            ),
        )
        LocalCommandEngine.KEY_VOLUME_FAILURE -> toneVariants(
            tone,
            warm = listOf(
                "Não consegui confirmar o ajuste de volume.",
                "O volume não respondeu como esperado.",
                "Não houve confirmação do ajuste de volume.",
            ),
            direct = listOf(
                "Volume sem confirmação.",
                "Ajuste de volume falhou.",
                "Volume não respondeu.",
            ),
        )
        LocalCommandEngine.KEY_APP_UNAVAILABLE -> toneVariants(
            tone,
            warm = listOf(
                "Abertura de aplicativos indisponível.",
                "Não consigo abrir aplicativos agora.",
                "O recurso de abrir apps está indisponível.",
            ),
            direct = listOf(
                "Abrir apps indisponível.",
                "Apps indisponíveis agora.",
                "Sem abertura de apps.",
            ),
        )
        LocalCommandEngine.KEY_APP_FAILURE -> toneVariants(
            tone,
            warm = listOf(
                "Não consegui abrir o aplicativo.",
                "A abertura do aplicativo falhou.",
                "Não foi possível iniciar o aplicativo.",
            ),
            direct = listOf(
                "Falha ao abrir o app.",
                "App não abriu.",
                "Não abri o aplicativo.",
            ),
        )
        LocalCommandEngine.KEY_APP_LAUNCHED -> toneVariants(
            tone,
            warm = listOf("Abrindo {app}.", "Iniciando {app}.", "Vou abrir o {app}."),
            direct = listOf("Abrindo {app}.", "Iniciando {app}.", "Abrindo {app} agora."),
        )
        LocalCommandEngine.KEY_APP_NOT_INSTALLED -> toneVariants(
            tone,
            warm = listOf(
                "{app} não está instalado.",
                "Não encontrei o {app} neste aparelho.",
                "Parece que o {app} não está instalado.",
            ),
            direct = listOf(
                "{app} não instalado.",
                "Sem {app} neste aparelho.",
                "{app} não encontrado.",
            ),
        )
        LocalCommandEngine.KEY_MEDIA_UNAVAILABLE -> toneVariants(
            tone,
            warm = listOf(
                "Controle de mídia indisponível.",
                "Não consigo controlar a mídia agora.",
                "Os comandos de mídia estão indisponíveis.",
            ),
            direct = listOf(
                "Mídia indisponível.",
                "Sem controle de mídia.",
                "Mídia indisponível agora.",
            ),
        )
        LocalCommandEngine.KEY_MEDIA_FAILURE -> toneVariants(
            tone,
            warm = listOf(
                "Não consegui enviar o comando de mídia.",
                "O comando de mídia não foi enviado.",
                "Falha ao enviar o comando de mídia.",
            ),
            direct = listOf(
                "Comando de mídia falhou.",
                "Mídia não respondeu.",
                "Falha no comando de mídia.",
            ),
        )
        LocalCommandEngine.KEY_MEDIA_NEXT_DISPATCHED -> toneVariants(
            tone,
            warm = listOf(
                "Comando de próxima faixa enviado.",
                "Pedido de próxima faixa encaminhado.",
                "Solicitei a próxima faixa.",
            ),
            direct = listOf(
                "Próxima faixa solicitada.",
                "Pedido de próxima faixa enviado.",
                "Solicitei a próxima faixa.",
            ),
        )
        LocalCommandEngine.KEY_MEDIA_PREVIOUS_DISPATCHED -> toneVariants(
            tone,
            warm = listOf(
                "Comando de faixa anterior enviado.",
                "Pedido de faixa anterior encaminhado.",
                "Solicitei a faixa anterior.",
            ),
            direct = listOf(
                "Faixa anterior solicitada.",
                "Pedido de faixa anterior enviado.",
                "Solicitei a faixa anterior.",
            ),
        )
        else -> error("Unknown phrase key: $key")
    }

    private fun toneVariants(
        tone: PersonalityTone,
        warm: List<String>,
        direct: List<String>,
    ): List<String> = when (tone) {
        PersonalityTone.WARM -> warm
        PersonalityTone.DIRECT -> direct
    }
}
