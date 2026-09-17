# Revisão e estabilização TASK-014–019

Data: 2026-09-17. BASE: `1b0bf1d71092dee40600a6c88c346ea2250674dc`.
Branch de entrega: `codex/stabilize-014-019`. Status: IMPLEMENTADA_AGUARDANDO_TESTE; sem merge. HEAD de entrega: commit que contém este documento.

## Revisão estática e mudanças

- 014: persistência engine/voice e remoção de chaves continuam separadas das demais preferências. A leitura de voz versus gravação pendente ao reabrir sessão ainda merece regressão específica; não declarar revisão integral aprovada.
- 015: watchdog de fila/fala presente. Faltava agendar expiração de ERROR: agora efeito cancelável aguarda 3 segundos, vinculado ao ID. Novo estado cancela espera. `begin` após shutdown não publica estado.
- 016: sessão principal compartilhada preservada. Sem alteração de assets, efeitos ou layout.
- 017/018: leitura tardia de mute/personalidade podia sobrescrever escolha explícita. Revisões independentes invalidam restore; token/revisão são capturados antes de agendar escrita. Escritas obsoletas aguardando mutex não substituem a escolha mais recente. Escolha explícita nesta instância impede nova restauração sobre seu estado. Falhas de persistência de mute ainda não têm mensagem dedicada: verificar como melhoria posterior.
- 018: abrir TOM atualiza rascunho com o nome atual, evitando campo inicial vazio após restauração.
- 019: listener antigo de foco podia chamar callback do pedido novo. Geração no adapter e domínio descarta esse listener. Retorno síncrono de TTS indisponível/falho agora libera foco também na prévia.
- Scriptado: HELP aceita frases completas (ajuda, comandos, o que você sabe fazer etc.), responde com exemplos locais por tom e nunca executa o texto dos exemplos. Sem nova permissão, rede ou dependência.

## Evidências e limites

Relatórios preexistentes em 16/09: 116 testes, zero falhas/erros. Handoffs anteriores relatam builds verdes; não são evidência para este patch. Nesta rodada: revisão estática e `git diff --check`; NENHUM teste/build executado. Foram criadas regressões de leitura atrasada, listener antigo de foco e ajuda sem ações. A matriz de rotas ainda depende do aparelho.

Pendências de revisão antes de integrar a cadeia: exercício de restore de voz enquanto save está pendente; comportamento de remoção de dispositivo que não era a rota ativa (monitor atual é conservador e cancela); verificar cada base intermediária, pois correções posteriores não corrigem retroativamente PRs anteriores. O watchdog atual cobre pedidos aceitos/fala; não comprova timeout da inicialização do serviço antes de existir pedido.

## Roteiro único do proprietário

Na branch de entrega, registrar SHA de `git rev-parse HEAD` e executar:

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

APK: `app/build/outputs/apk/debug/app-debug.apk` (gerado por esse build; APK anterior não contém o patch).

1. Voz: escolher, reiniciar, USAR PADRÃO; repetir offline; abrir/fechar DEV.
2. OUVIR, cancelar, TEXTO, PARAR e sair do app durante fala; não retomar áudio automaticamente.
3. Ativar mute e trocar nome/tom logo ao abrir app; reiniciar e verificar persistência. Abrir TOM deve mostrar nome restaurado. Testar prévia explícita em mute.
4. Provocar erro: rosto retorna após ~3s; nova escuta não é interrompida por timer antigo.
5. Música + fala + escuta; desconectar fone/Bluetooth. Preencher TASK-019-AUDIO-MATRIX.md. Falha de voz não deve manter música sob foco da VEXA.
6. Ajuda por texto e voz: “Vexa, o que você sabe fazer?”, “ajuda”, “comandos”. Exemplos não abrem apps nem alteram volume. “não quero ajuda” e “ajuda e abra Spotify” não executam ação.
7. Release sem DEV, mas COM controles principais e permissão de microfone da TASK-016; roteiro antigo da 015 sem microfone vale apenas para aquela base histórica.

Retorno: HEAD, builds OK/FALHA, aparelho/Android, cenários OK/FALHA/NÃO TESTADO e passos para reproduzir falha. Sem merge automático.
