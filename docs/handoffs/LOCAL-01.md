# LOCAL-01 — núcleo de comandos locais

Status: implementação e testes unitários concluídos em 2026-09-15; validação funcional pelo proprietário ainda pendente.

## Resumo

Foi adicionado um motor Kotlin puro que reconhece chamada pelo nome, presença, saudação, agradecimento e consulta de hora. O reconhecimento compara frases completas depois de normalizar caixa, acentos, pontuação e espaços. Um único prefixo opcional “Delamain” é aceito. Entradas desconhecidas, negativas ou compostas retornam um resultado desconhecido explícito.

O motor não usa Android, rede, interface ou armazenamento. A hora vem de `java.time.Clock`, injetável nos testes. Cada intenção percorre deterministicamente duas respostas em rotação própria: primeira variante, segunda variante, primeira variante novamente. Assim, não repete a resposta anterior enquanto houver outra variante.

Frases normalizadas aceitas, além do nome isolado `delamain`:

- presença: `esta ai`, `voce esta ai`;
- saudação: `oi`, `ola`, `bom dia`, `boa tarde`, `boa noite`;
- agradecimento: `obrigado`, `obrigada`, `muito obrigado`, `muito obrigada`;
- hora: `que horas sao`, `que hora e`, `qual e a hora`.

Cada frase da lista também aceita um único prefixo `delamain`. A normalização permite diferenças de caixa, acentos, pontuação e espaços, mas não acrescenta reconhecimento aproximado.

## Arquivos

- `app/src/main/java/com/gregorypina/delamain/domain/LocalIntent.kt`
- `app/src/main/java/com/gregorypina/delamain/domain/LocalCommandResult.kt`
- `app/src/main/java/com/gregorypina/delamain/domain/LocalCommandEngine.kt`
- `app/src/test/java/com/gregorypina/delamain/domain/LocalCommandEngineTest.kt`
- `docs/handoffs/LOCAL-01.md`

## Interface estável

```kotlin
enum class LocalIntent { CALL, PRESENCE, GREETING, THANKS, TIME }

sealed interface LocalCommandResult {
    data class Recognized(
        val intent: LocalIntent,
        val response: String,
    ) : LocalCommandResult

    data object Unknown : LocalCommandResult
}

class LocalCommandEngine(
    clock: Clock = Clock.systemDefaultZone(),
    configuredName: String? = null,
) {
    fun process(input: String): LocalCommandResult
}
```

Exemplos:

```kotlin
LocalCommandEngine().process("Delamain, está aí?")
// Recognized(PRESENCE, "Sempre a postos.")

LocalCommandEngine(configuredName = "Ana").process("Está aí?")
// Recognized(PRESENCE, "Sempre a postos, Ana.")

LocalCommandEngine().process("Não, obrigado")
// Unknown
```

O consumidor deve tratar exaustivamente `Recognized` e `Unknown`. Só `Recognized` contém texto para exibição.

## Validação

- `git diff --check` nos arquivos da tarefa: passou.
- Primeira tentativa de `:app:testDebugUnitTest --no-daemon`: Gradle não chegou a iniciar porque `JAVA_HOME` e `java` não estavam disponíveis naquela sessão.
- Execução coordenada por BUILD-01 com o JDK localizado: `assembleDebug testDebugUnitTest assembleRelease` passou (`BUILD SUCCESSFUL`, 92 tarefas). Os 10 testes de `LocalCommandEngineTest` passaram.

Os 10 testes JUnit cobrem nome isolado, prefixo opcional, presença, saudações, agradecimento, hora, normalização de acentos/caixa/pontuação, entradas desconhecidas, negações, pedidos compostos, fuso do relógio injetado, alternância de variantes e nome configurado ou ausente.

## Limitações e decisões

- O catálogo é deliberadamente fechado; frases aproximadas continuam desconhecidas.
- O nome configurado é apenas um argumento em memória e não cria persistência.
- A alternância de frases fica no estado da instância e é independente por intenção.
- `Unknown` não encaminha texto para IA nem executa ação.

## Próximo responsável

DEBUG-01 pode consumir a interface acima para a entrada manual de desenvolvimento. QA-01 deve validar o conjunto e o proprietário ainda precisa validar o comportamento no aparelho antes que a experiência seja considerada funcionalmente aprovada.
