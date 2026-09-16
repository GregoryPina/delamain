# TASK-012 — comparação de vozes locais

STATUS: IMPLEMENTADA_AGUARDANDO_TESTE.
BASE COMMIT: `7517161e13a30ef7702b3f669ee7aba9ca5164a8` (TASK-011/PR #3).
Branch: `codex/task-012-voice-preview`; inclui TASK-010/PR #2 e TASK-011/PR #3, todas aguardando validação. Não integrar isoladamente nem antes das dependências.

## Entrega

DEV oferece TROCAR VOZ e TESTAR VOZ entre vozes instaladas pt-BR declaradas sem rede. Lista preserva voz atual elegível antes de ordenar por qualidade declarada/nome; não infere gênero, naturalidade ou qualidade percebida. Exemplo não passa pelo motor de comandos e não executa ações.

Escolha vale apenas para a sessão do painel: fechar/reabrir volta à seleção inicial. Sem persistência, instalação de motores, mudança da voz global do Android, pitch ou velocidade. Quantidade depende do aparelho; uma voz permite exemplo, nenhuma mantém texto e indisponibilidade.

Troca cancela escuta, exige parada TTS aceita e verifica seleção aplicada. Se falhar, fala fica indisponível até seleção explícita válida; não cai para voz remota. Antes de cada enqueue verifica voz atual contra seleção, exigência de rede e dados instalados. Callbacks antigos continuam invalidados pela sessão. Seleção só disponível depois do listener TTS instalado.

## Verificação

Sete testes criados: cinco de catálogo/ciclo (rejeição de rede/dados ausentes/outros idiomas, preferência elegível, ordem estável, lista vazia e alternância); dois da sessão (falha de seleção bloqueia fala e seleção não reativa objeto encerrado).

Total novo nas três entregas: 29 testes criados (12 + 10 + 7), nenhum executado. Builds não executados. Revisão estática e diff realizados; seleção/qualidade/funcionamento offline reais dependem do proprietário. Não afirmar compilação aprovada.

## Roteiro consolidado de amanhã

Esta branch inclui todas as entregas; não é necessário instalar três APKs para validar o conjunto.

```powershell
git switch codex/task-012-voice-preview
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

1. Instalar APK debug. Em DEV, testar digitação e fala: Vexa, hora, bateria, volume, apps e mídia. Conferir Unknown.
2. PARAR VOZ durante fala; fechar/reabrir DEV; sair/retornar durante preparação e fala. Nenhuma fala tardia. Texto preservado. Roteiro detalhado: [TASK-010](TASK-010.md).
3. OUVIR: negar/conceder permissão; após conceder, tocar novamente. Dizer uma frase, conferir resultado único. Cancelar e falar depois não executa. Silêncio encerra em até 15s; fechar/sair libera microfone. Sem suporte local, mensagem e digitação. Roteiro: [TASK-011](TASK-011.md).
4. TROCAR VOZ e TESTAR VOZ: comparar opções disponíveis, parar exemplo, trocar durante fala. Trocar/testar durante escuta cancela captura. Fechar/reabrir volta à seleção inicial — comportamento esperado deste recorte.
5. Repetir fala/escuta sem rede com modelos já disponíveis. Conferir ausência de autoescuta/eco do TTS e de reinício automático. Serviço local pt-BR pode não existir no aparelho; isso não autoriza fallback remoto.
6. Conferir painel com teclado e scroll; release sem DEV e sem RECORD_AUDIO. Caso falhe, mandar primeiro erro do build ou sequência exata no aparelho e HEAD testado.

## Integração

Revisar o HEAD final testado, resolver falhas antes de integrar. PRs dependentes: primeiro TASK-010 → main; depois retarget TASK-011 para main e revisar diff; finalmente retarget TASK-012 para main e revisar diff. Não mesclar a branch filha na mãe automaticamente. Evitar squash das dependências sem conferir ancestralidade/diffs posteriores. Todos os testes e aceites continuam pendentes.

Método habitual de trabalho permanece: Astra coordena, executores externos implementam e proprietário testa. A implementação direta destas três tarefas foi exceção desta sessão. Próximo passo é validar, não ampliar a cadeia sem feedback.

## Referência publicada

[PR #4](https://github.com/GregoryPina/delamain/pull/4), dependente de [PR #3](https://github.com/GregoryPina/delamain/pull/3) e [PR #2](https://github.com/GregoryPina/delamain/pull/2). Baseline conjunta de código: `7d63e605cc3ad36cc3ded50bd62dfcdff449c207`; atualização documental posterior não executa nem altera o código testável.
