# EXTERNAL AGENT DISPATCH — WAVE 3

### AGENT 1

Recommended model: GPT / Claude / Gemini, à escolha do proprietário.
Repository: https://github.com/GregoryPina/delamain
Branch de origem: main
Base commit: 85d249df33d5722cec74532bcee8dd6a58d2b3af
Tasks: tasks/TASK-006.md
Can run now: YES, após confirmar que o commit-base está publicado em `origin/main`.
Dependencies: Wave 2 concluída (TASK-004 + TASK-005 integradas).

## Instruction

Implemente TASK-006; uma resposta somente de análise não conclui esta entrega.

Leia `AGENTS.md`, `tasks/TASK-006.md`, `docs/DECISIONS.md`, `docs/ARCHITECTURE.md`, `docs/INTERACTIONS.md` e `docs/handoffs/TASK-003.md` no commit exato acima. Leia os demais documentos obrigatórios relevantes e apenas os arquivos necessários.

A entrega adiciona abertura de aplicativos permitidos ao **fluxo local ativo** (`LocalCommandEngine` + porta tipada + adapter Android). O roteador remoto (`CommandRouter`) e o executor remoto (`AndroidCommandExecutor`) devem permanecer **inativos e intocados**. Use o catálogo remoto apenas como referência de pacotes/nomes; não conecte esse caminho à UI.

Se tiver acesso autorizado de escrita, crie branch `codex/open-app-local` a partir do BASE COMMIT e abra um PR para `main`. Não faça push diretamente em `main`, não faça merge e não descarte alterações alheias.

Sem acesso de escrita, devolva unified diff completo e aplicável à baseline. Se não conseguir ler o GitHub, peça os arquivos mínimos e declare BLOQUEADA; não invente contexto. Não presuma acesso ao filesystem do proprietário e não crie agentes internos.

Implemente os testes necessários, mas a execução dos builds/testes locais será feita pelo proprietário. Não afirmar testes não executados. Inclua roteiro curto, comandos e resultados esperados. O Acting Tech Lead / Astra revisará código/PR após validação do proprietário.

## Retorno obrigatório

TASKS: TASK-006
BASE COMMIT: 85d249df33d5722cec74532bcee8dd6a58d2b3af
STATUS: IMPLEMENTADA_AGUARDANDO_TESTE / PARCIAL / BLOQUEADA
ALTEROU ARQUIVOS: SIM / NÃO
ENTREGA: link do PR + branch/commit, ou unified diff anexado
RESUMO: até cinco itens
TESTES CRIADOS: lista
TESTES EXECUTADOS: somente os reais, ou Nenhum
ROTEIRO PARA O PROPRIETÁRIO: comandos e passos curtos
PENDÊNCIAS/RISCOS: até três itens
PRÓXIMO PASSO PARA TECH LEAD: uma frase

Não declarar concluída sem PR/patch de implementação. Se parcial/bloqueada, explicar objetivamente o que falta.
