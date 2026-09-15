# LOCAL-02 — presença informal e volume de mídia

## Status

Implementação e testes automáticos concluídos em 2026-09-15. A validação do ajuste de volume em aparelho permanece pendente com o proprietário.

## Resumo

O catálogo fechado passou a reconhecer formas informais explícitas de presença e comandos locais de aumentar ou diminuir o volume. A correspondência continua por igualdade da frase inteira após a normalização existente; negações, pedidos compostos, palavras soltas e variantes não catalogadas retornam `Unknown` sem executar ação.

O domínio ganhou uma allowlist com apenas `VOLUME_UP` e `VOLUME_DOWN`, uma porta injetável e resultados tipados. Sem porta injetada, o motor retorna `Unavailable`, portanto nunca confirma uma ação inexistente. O painel debug injeta o adaptador Android usando o `applicationContext`; sua estrutura visual e a implementação vazia da variante release foram preservadas.

## Catálogo normalizado

Presença:

- `esta ai`
- `ta ai`
- `voce esta ai`
- `voce ta ai`
- `vc esta ai`
- `vc ta ai`
- `ce ta ai` (forma normalizada de “cê tá aí”)

Aumentar volume:

- `aumente o volume`
- `aumentar o volume`
- `aumenta o volume`

Diminuir volume:

- `diminua o volume`
- `diminuir o volume`
- `diminui o volume`
- `abaixa o volume`
- `abaixe o volume`
- `abaixar o volume`
- `baixe o volume`

Todas essas frases também aceitam um único prefixo `delamain`, conforme o contrato anterior.

## Interfaces e resultados

`LocalCommandEngine` agora recebe a porta opcional:

```kotlin
class LocalCommandEngine(
    clock: Clock = Clock.systemDefaultZone(),
    configuredName: String? = null,
    actionPort: LocalActionPort = UnavailableLocalActionPort,
)
```

`LocalActionPort.execute(LocalAction)` aceita somente os dois valores allowlisted. `LocalActionResult` distingue:

- `Changed(action, before, after)`: alteração confirmada por leitura anterior e posterior;
- `AtLimit(action, level)`: limite superior ou inferior já alcançado;
- `Fixed(action)`: política de volume fixo do dispositivo;
- `Unavailable(action)`: integração ausente;
- `Denied(action)`: `SecurityException` da plataforma;
- `Failure(action)`: erro de plataforma ou ajuste não confirmado pela leitura imediata.

`LocalCommandResult.Recognized` preserva `intent` e `response` e adiciona `actionResult`, nulo para reflexos e hora. Apenas `Changed` produz “Volume aumentado.” ou “Volume reduzido.”. Uma leitura posterior sem mudança responde “Não consegui confirmar o ajuste de volume.”, sem repetir automaticamente a ação.

## Integração Android

`AndroidMediaVolumeActionPort` usa exclusivamente `AudioManager.STREAM_MUSIC`. O fluxo é:

1. verificar `isVolumeFixed`;
2. ler o volume atual e o limite aplicável;
3. aplicar um passo com `adjustStreamVolume`, `ADJUST_RAISE` ou `ADJUST_LOWER`, e flags `0`;
4. ler novamente e confirmar somente uma mudança na direção solicitada.

Foi declarada apenas `android.permission.MODIFY_AUDIO_SETTINGS`. Segundo a documentação oficial do Android, `adjustStreamVolume` muda o stream indicado em um passo, requer essa permissão e não produz efeito sob política de volume fixo. A permissão tem nível de proteção `normal`, concedido na instalação, portanto não há prompt em tempo de execução.

Referências consultadas:

- https://developer.android.com/reference/android/media/AudioManager
- https://developer.android.com/reference/android/Manifest.permission#MODIFY_AUDIO_SETTINGS
- https://developer.android.com/guide/topics/permissions/overview

## Arquivos alterados

- `app/src/main/java/com/gregorypina/delamain/domain/LocalIntent.kt`
- `app/src/main/java/com/gregorypina/delamain/domain/LocalCommandResult.kt`
- `app/src/main/java/com/gregorypina/delamain/domain/LocalCommandEngine.kt`
- `app/src/main/java/com/gregorypina/delamain/domain/LocalActionPort.kt`
- `app/src/main/java/com/gregorypina/delamain/integration/audio/AndroidMediaVolumeActionPort.kt`
- `app/src/debug/java/com/gregorypina/delamain/ui/DebugCommandPanel.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/test/java/com/gregorypina/delamain/domain/LocalCommandEngineTest.kt`
- `docs/handoffs/LOCAL-02.md`

Nenhum documento central, arquivo Gradle, asset ou componente do rosto foi alterado nesta tarefa.

## Validação executada

Com `JAVA_HOME=C:\Users\Gregory\.jdks\jbr-21.0.11`:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

Resultado: `BUILD SUCCESSFUL in 12s`; 88 tarefas, 30 executadas e 58 atualizadas. Depois da inclusão final dos sinônimos naturais, `testDebugUnitTest` passou novamente em 4s. A execução final do conjunto completo e a contagem do relatório XML serão registradas antes da liberação ao coordenador.

`git diff --check` passou nos arquivos do recorte; surgiram apenas avisos de futura conversão LF/CRLF em arquivos já existentes.

Os testes usam uma porta falsa que registra ações e cobrem aliases, mudança confirmada com valores anterior/posterior, limites superior/inferior, volume fixo, indisponibilidade, negação, falha e o padrão indisponível. Também comprovam zero ações para frases negativas, compostas, parciais ou desconhecidas.

## Limitações e próximo passo

- A leitura logo após o ajuste pode ainda refletir o valor anterior em alguma rota ou implementação do fabricante. Nesse caso o resultado é conservador (`Failure`) e não há nova tentativa automática.
- O adaptador não altera mute, toque, DND, GPS, rede ou qualquer stream além de mídia.
- Não há TTS, STT, controle de reprodução ou prompt de permissão.
- O proprietário deve validar no Redmi 13 os dois sentidos, os dois limites e o comportamento da rota de áudio usada no carro.
- O coordenador revisa a entrega e atualiza os documentos centrais; uma QA independente pode inspecionar a correção do adaptador e dos resultados antes da validação no aparelho.

## Fechamento pelo Tech Lead

Após interrupção dos agentes por limite, Astra executou o conjunto final `assembleDebug testDebugUnitTest assembleRelease` com JBR 21.0.11: BUILD SUCCESSFUL (17s; 88 tarefas). XML registra 18 testes, zero falhas/erros/ignorados. Aliases extras, mensagem conservadora de confirmação e regressões de prefixo/negação estão incluídos. Teste físico de volume ainda pendente; implementação não será retomada por agente interno.
