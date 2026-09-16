# TASK-014 — lembrar a voz escolhida

STATUS: IMPLEMENTADA_AGUARDANDO_TESTE. Base exata: `f7e851bc799b018cd58c6f1bf2b15a54511f2c3f`. Nenhum build ou teste foi executado pelo executor, conforme despacho.

## Implementação

A escolha explícita de voz no DEV agora é persistida com Preferences DataStore após `TextToSpeech.setVoice` ser confirmado pelo adapter. O registro usa identidade estável do motor TTS + `Voice.name`; nunca índice/posição da lista. Na abertura do painel, catálogo e preferência são carregados sem bloquear a UI e sem falar automaticamente.

Esquema no arquivo DataStore `vexa_preferences`:

- `voice_preference_version` (Int), versão atual 1;
- `voice_engine_id` (String), `TextToSpeech.defaultEngine`;
- `voice_id` (String), `Voice.name`.

Não há áudio ou transcrição persistidos. “USAR PADRÃO” remove somente essas três chaves; futuras preferências no mesmo DataStore são preservadas.

## Restauração, fallback e concorrência

A preferência só é aplicada quando versão, motor e ID coincidem e o ID ainda pertence ao catálogo filtrado pela regra da TASK-012: `pt-BR`, instalado e declarado sem rede. O adapter revalida idioma/país/rede/dados também no `setVoice`. Preferência ausente mantém a alternativa local determinística já escolhida pelo adapter. Preferência incompatível/indisponível é informada e não é sobrescrita pelo fallback. Sem voz local elegível, o comportamento anterior de resposta em texto permanece.

Uma geração é incrementada sincronamente antes de cada escolha explícita. Assim, leitura de startup que termine depois é descartada. Escritas/clear são serializados por `Mutex`; apenas o evento correspondente à escolha explícita mais recente altera o estado exibido. Falha de escrita mantém a voz realmente aplicada e informa “Usando nesta sessão; não foi possível salvar”. Falha de leitura é distinta de falha TTS.

Migração: versão desconhecida é tratada como preferência indisponível, sem apagar ou regravar automaticamente. Não há migração de seleção por índice porque ela nunca é persistida.

## Arquivos e contratos

- `domain/SpeechVoicePreference.kt`: modelo versionado, store fakeável, resultados/eventos e coordenador de concorrência.
- `domain/SpeechVoiceSelection.kt`: passa a expor `engineId` junto ao catálogo/seleção.
- `integration/voice/DataStoreSpeechVoicePreferenceStore.kt`: implementação Preferences DataStore.
- `integration/voice/AndroidTextToSpeechPort.kt`: restauração por motor+ID, revalidação de elegibilidade e seleção do fallback local.
- `src/debug/.../DebugCommandPanel.kt`: leitura única por abertura/catálogo, salvar após seleção confirmada e “USAR PADRÃO”.
- `app/build.gradle.kts`: `androidx.datastore:datastore-preferences:1.1.2`.

## Testes criados

`SpeechVoicePreferenceCoordinatorTest`: restauração válida; ID igual em motor diferente; ID removido/inelegível; falha de leitura/escrita; leitura atrasada após escolha; escritas concorrentes serializadas com última escolha prevalecendo; clear; versão incompatível. `SpeechVoiceSelectionTest` existente continua cobrindo exclusão de voz de rede, dados ausentes e outros locales.

TESTES EXECUTADOS: nenhum. Builds e testes foram explicitamente reservados ao proprietário.

## Comandos para o proprietário

No Windows, na raiz do repositório e com JDK/SDK configurados:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

## Roteiro manual

1. Abrir APK debug → DEV; anotar voz inicial, trocar para outra voz, aguardar indicação `(salva)`, fechar DEV e abrir novamente. Confirmar restauração sem fala automática.
2. Encerrar o app completamente, abrir novamente → DEV e confirmar a mesma voz.
3. Com voz salva, tocar “USAR PADRÃO”, fechar/reabrir e reiniciar app. Confirmar que a preferência anterior não volta.
4. Repetir troca e restauração sem rede. A voz restaurada deve continuar sendo local pt-BR; não aceitar/download voz remota.
5. Regressão: testar ENVIAR/TTS, TESTAR VOZ, PARAR VOZ, OUVIR/CANCELAR ESCUTA e FECHAR.
6. Preferência de motor/voz ausente e falhas de store são cobertas por fake; não é necessário remover motor TTS do aparelho.

## Limitações e pendências

Preferences DataStore participa do backup normal do app conforme configuração/plataforma Android; este recorte não implementa política própria de backup/restauração entre aparelhos. Como IDs de motor/voz podem não existir em outro aparelho, uma preferência restaurada por backup só será aplicada se motor+ID ainda forem elegíveis; caso contrário permanece indisponível e o fallback local é usado sem sobrescrevê-la.

A validação funcional, unitária, release e offline ainda depende do proprietário. TASK-015 não foi iniciada.
