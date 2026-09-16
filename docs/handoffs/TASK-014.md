# TASK-014 — lembrar a voz escolhida

STATUS: IMPLEMENTADA_AGUARDANDO_TESTE. Base original: `f7e851bc799b018cd58c6f1bf2b15a54511f2c3f`. Revisão do PR #5 em `docs/handoffs/PR-005-REVIEW.md` aplicada sem executar build/testes.

## Implementação
A escolha explícita é persistida por `TextToSpeech.defaultEngine` + `Voice.name`, versão 1, em Preferences DataStore `vexa_preferences`. Só voz pt-BR instalada e declarada sem rede pode ser restaurada. Preferência incompatível não é sobrescrita pelo fallback. USAR PADRÃO só limpa as chaves de voz depois que o fallback local foi aplicado com sucesso.

## Correções da revisão PR #5
- **R1:** coordinator ganhou token de sessão. `openSession/closeSession` impedem restore/evento antigo de atingir porta de uma reabertura. Uma gravação aceita pode terminar no store após fechar, mas não publica UI na sessão nova.
- **R2:** estado TTS e mensagem de preferência são linhas separadas; Saved/Restore não ocultam Speaking/Completed/Failed.
- **R3:** `CancellationException` é propagada em read/write/clear do DataStore; cancelamento não vira ReadFailed/SaveFailed/ClearFailed.
- **R4:** geração é validada dentro do `Mutex` antes da mutação. Save/clear obsoleto não escreve no disco, inclusive ordem invertida.
- **USAR PADRÃO:** falha ao aplicar fallback não dispara clear nem anuncia remoção. Falha de seleção e falha de persistência permanecem distintas.

## Testes criados
`SpeechVoicePreferenceCoordinatorTest` cobre sessão encerrada/reaberta, restore versus save pendente, save/clear obsoletos em ordem invertida e falhas distintas. Testes do store preservam chave não relacionada; os testes existentes de seleção continuam cobrindo elegibilidade local pt-BR. Nenhum teste foi executado.

## Roteiro acumulado do proprietário
```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```
Manual: trocar voz e confirmar `(salva)`; fechar/reabrir DEV; reiniciar app; usar padrão e confirmar que a preferência não volta; repetir offline; durante/restaurar voz executar TESTAR VOZ e observar Speaking/Completed/Failed sem a mensagem de preferência ocultar o estado; regressão ENVIAR, PARAR VOZ, OUVIR/CANCELAR ESCUTA e FECHAR.

## Pendências
Validação local, release e aparelho continuam com o proprietário. TASK-015 deve partir do HEAD corrigido desta branch, conforme despacho, sem merge da 014.
