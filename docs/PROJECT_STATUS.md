# Estado do projeto

Atualizado em 2026-09-15. Documento canônico de estado; não duplicar como PROJECT_STATE.md.

## Objetivo atual

Baseline publicada; receber revisão externa para consolidar os dois motores preservados.

## Concluído

- Interface V0.1 e fluxo digitado V0.2-A aceitos pelo proprietário.
- Wrapper Gradle 8.9, motor local, painel DEV exclusivo de debug, reflexos e hora.
- V0.2-B: aliases de presença informal e ações de aumentar/reduzir volume de mídia, com resultados tipados.
- Astra executou assembleDebug, testDebugUnitTest e assembleRelease: sucesso; 18 testes, zero falhas/erros/ignorados.
- Build não equivale a teste de volume no aparelho.

## Operação

Astra é o único agente no Work: planejamento, revisão, aplicação local, testes e Git. Não criar/reativar agentes internos. Modelos externos são abertos pelo proprietário e devolvem patches contra BASE COMMIT exato. Os handoffs de SOL são históricos; os executores pararam por limite de uso e o fechamento foi assumido por Astra.

## Wave atual

Checkpoint e sincronização concluídos. A contribuição remota 2894e76 trouxe domain/command/ e platform/; código preservado sem conexão ao painel antes da revisão. O fluxo ativo continua LocalCommandEngine + LocalActionPort + AndroidMediaVolumeActionPort.

## Próxima wave

Wave 1, um executor externo: tasks/TASK-003.md, revisão de consolidação. Dependência: commit do checkpoint publicado e informado no EXTERNAL AGENT DISPATCH. Nenhum executor externo foi iniciado automaticamente.

## Problemas / limites

- Dois motores no código até a consolidação; mídia/apps/volume percentual remotos não estão integrados ao fluxo ativo.
- Parser remoto usa substring para alguns comandos e precisa de revisão de negações; executor remoto confirma ações sem toda a evidência exigida no fluxo atual.
- Volume real, limites e rotas de áudio ainda precisam de validação no aparelho.
- Catálogo local continua fechado; aliases não são compreensão livre de linguagem.
- Release não assinado; APK debug para teste pessoal. Sem voz, IA ou rede no app.

## Memória

- [Retomada](../TECH_LEAD_HANDOFF.md)
- [Decisões](DECISIONS.md)
- [Feedback de digitação](handoffs/FEEDBACK-01.md)
- [Núcleo inicial](handoffs/LOCAL-01.md)
- [Volume e aliases](handoffs/LOCAL-02.md)
- [Revisão de volume](handoffs/QA-02.md)
- [Build](handoffs/BUILD-01.md)
- [Painel DEV](handoffs/DEBUG-01.md)

## Próximo passo

Conferir a baseline em TECH_LEAD_HANDOFF.md e encaminhar TASK-003 a um modelo externo. Não ativar mais recursos até receber e revisar a proposta de consolidação. Em paralelo, proprietário pode testar o APK V0.2-B.

## Checkpoint publicado

Baseline fixa da Wave 1: `06bf60d73d38ac4acf76c9684082572590a606f4`, publicada em `origin/main`. Inclui checkpoint local `6972820` e contribuição remota `2894e76`, sem descarte de trabalho. Validação após merge: BUILD SUCCESSFUL em 22s; 18 testes sem falhas/erros/ignorados. [Despacho externo](../tasks/WAVE-001-DISPATCH.md) pronto para o proprietário encaminhar.

## Wave 2 — implementação após revisão externa

TASK-003 foi concluída sem patch ou alterações: revisão estática, CHANGES REQUIRED para ativar contribuição remota. Achados registrados em handoffs/TASK-003.md. O fluxo ativo permanece canônico.

Próxima entrega obrigatoriamente contém código: TASK-004 corrige falsos positivos no roteador remoto ainda inativo; TASK-005 aplica VEXA no nome exibido e na chamada digitada, sem renomear identificadores técnicos. Um executor externo pode agrupar as duas em uma branch/PR com commits separados. Não foi iniciado automaticamente.

Preferência atual: proprietário executa builds/testes locais; Astra não os executa automaticamente. Resultados antigos continuam históricos. Agente com acesso autorizado pode abrir PR em branch própria, nunca push em main ou merge. Sem escrita, deve retornar unified diff. Roteiro de validação acompanha a entrega.

## Revisão do PR #1

TASK-004/005 entregues no PR draft #1, HEAD `8351703787b01fe598e31307d3dab43ffdefa902`, base confirmada. Revisão estática Astra: CHANGES REQUIRED por normalização excessiva de pontuação em percentual. Identidade VEXA sem impedimentos encontrados na leitura. Nenhum build/teste executado; nenhum merge realizado. Próximo passo: correção localizada no mesmo PR, conforme [revisão](handoffs/PR-001-REVIEW.md); depois validação pelo proprietário.

## PR #1 — correção aprovada estaticamente

HEAD `39d9fce03943760eb722bd31b557596653a15798` revisado somente contra o HEAD anterior. APPROVED na revisão estática; agora aguardando execução dos builds/testes e verificação de VEXA pelo proprietário. Não houve merge nem teste automático no Work. A correção ficou restrita aos dois arquivos de roteador/testes pedidos.

## Fechamento da Wave 2 — estado atual

Proprietário respondeu “teste ok” ao roteiro do HEAD `39d9fce03943760eb722bd31b557596653a15798`. Validação aceita como relato, sem logs/contagem/aparelho inferidos. Astra não executou testes. PR #1 integrado em `85d249df33d5722cec74532bcee8dd6a58d2b3af`; checkout local retornou a main.

TASK-004 e TASK-005 concluídas. Nome/gatilho oficiais: VEXA. Parser remoto corrigido, mas permanece inativo. Não há executor ativo. Próximo recorte recomendado: abertura controlada de aplicativo via porta tipada; preparar TASK antes de implementar. Este fechamento substitui os estados pendentes da Wave 2 registrados acima.

## Wave 3 — TASK-006

Implementação em `codex/open-app-local` (`0260d6b`): abertura de apps allowlisted no fluxo local, `<queries>` no manifesto, validação manual do YouTube pelo proprietário (“funcionou”). Branch publicada; PR pendente em https://github.com/GregoryPina/delamain/compare/main...codex/open-app-local Testes automáticos não executados pelo Acting Tech Lead. Próximo recorte preparado: TASK-007 (faixa anterior/próxima).
