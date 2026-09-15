# Decisões

## ADR-001 — domínio local antes de IA

Kotlin/Compose existentes são preservados. O fluxo ativo usa `LocalCommandEngine`, catálogo explícito e portas tipadas; a IA não é necessária para reflexos ou volume. Reconsiderar roteamento somente com testes que preservem negações e ausência de efeitos inesperados.

## ADR-002 — transição para executores externos

Instrução do proprietário em 2026-09-15 substitui a política de SOL interno. Astra concentra Work, integração, testes e Git. Não criar/reativar agentes internos. O proprietário encaminha TASKs a modelos externos; cada wave usa commit publicado exato e retorna patches.

## ADR-003 — preservar as duas contribuições durante sincronização

O remoto avançou de `5d0be25` a `2894e76` com `domain/command/CommandRouter` e `platform/AndroidCommandExecutor`, enquanto o trabalho local introduziu `LocalCommandEngine` e `LocalActionPort`. Preservar ambas as contribuições no checkpoint. O painel continua usando apenas o motor local validado. O código remoto não deve ser conectado antes de revisar reconhecimento por substring, negações e confirmação de resultados.

Uma TASK externa deverá propor consolidação incremental, aproveitando comportamentos úteis sem duplicar caminhos ativos. Mídia, abertura de apps e volume percentual presentes no remoto não são considerados integrados/validados pelo simples merge.
