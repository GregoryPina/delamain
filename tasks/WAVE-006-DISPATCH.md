# EXTERNAL AGENT DISPATCH — WAVE 6 (TTS)

### AGENT 1

Repository: https://github.com/GregoryPina/delamain
Branch de origem: main
Base commit: (confirmar HEAD após merge TASK-008 em `main`)
Tasks: tasks/TASK-009.md
Can run now: YES após confirmar baseline.
Dependencies: ADR-005, TASK-008 concluída.

## Instruction

Implemente TASK-009 conforme `docs/DECISIONS.md` (ADR-005). Leia a TASK, ADR-005 e `docs/ARCHITECTURE.md`. Use `LocalCommandEngine` como roteador canônico; não ative `CommandRouter` remoto. TTS apenas no painel DEV; STT e wake word ficam fora desta entrega.

Retorno obrigatório: PR ou unified diff, roteiro de teste no aparelho, STATUS IMPLEMENTADA_AGUARDANDO_TESTE.
