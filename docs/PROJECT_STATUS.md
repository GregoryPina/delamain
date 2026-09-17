# Estado do projeto

Atualizado em 2026-09-17. Baseline funcional integrada: TASK-004–013, aceite anterior do proprietário para microfone/fala/cancelar/trocar voz. Main conhecida: `140f14e`; nenhuma integração nova nesta rodada.

## Conjunto em validação

014: voz persistente. 015: rosto por eventos e watchdog. 016: OUVIR/PARAR/TEXTO na tela principal. 017: mute e controles prioritários. 018: nome/tom. 019: foco e monitor de rotas.

Base acumulada: `1b0bf1d71092dee40600a6c88c346ea2250674dc`, branch `codex/task-019-audio-focus`.
Correções e ajuda local: `codex/stabilize-014-019`. Status IMPLEMENTADA_AGUARDANDO_TESTE, sem merge.

[Revisão, limitações e roteiro único](handoffs/STABILIZATION-014-019.md). Relatórios anteriores: 116 testes sem falhas; não validam as mudanças novas. Nesta rodada nenhum build/teste executado.

## Próximo passo

Proprietário testa APK acumulado. Coordenador registra resultado, corrige falhas e revisa bases intermediárias antes de integração. Pendências estáticas estão no handoff, sem afirmar aprovação integral. IA (020/021) não iniciada. [Evolução scriptada](../tasks/SCRIPTED-BEFORE-AI.md) define recortes sem rede; somente ajuda implementada nesta rodada.

## Contratos

Motor ativo LocalCommandEngine, ações permitidas; legado inativo. Sem IA, wake word, GPS, play/pause ou volume percentual ativo. STT on-device depende do aparelho; sem fallback remoto. Mute suprime TTS automático, prévia explícita pode falar. Release possui controles principais, sem DEV. Áudio externo ainda exige matriz no aparelho.

[Retomada](../TECH_LEAD_HANDOFF.md), [decisões](DECISIONS.md), [processo](DEVELOPMENT.md).
