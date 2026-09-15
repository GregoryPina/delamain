# EXTERNAL AGENT DISPATCH — WAVE 2

### AGENT 1

Recommended model: GPT / Claude / Gemini, à escolha do proprietário.
Repository: https://github.com/GregoryPina/delamain
Branch de origem: main
Base commit: d3e341a9d684f1fd2fb6d9490c8c6d554fbf502c
Tasks: tasks/TASK-004.md + tasks/TASK-005.md
Can run now: YES, após confirmar que o commit-base está publicado.
Depends on: TASK-003 concluída (revisão estática).

## Instruction

Implemente as duas TASKs; uma resposta somente de análise não conclui esta entrega. Leia AGENTS.md, as TASKs, docs/DECISIONS.md e docs/handoffs/TASK-003.md no commit exato acima. Leia os demais documentos obrigatórios relevantes e apenas os arquivos necessários.

TASK-004 corrige reconhecimento parcial no roteador remoto, que deve continuar inativo. TASK-005 muda nome exibido e chamada digitada para VEXA, preservando repositório, applicationId, pacotes/classes, assets e layout. Não ampliar escopo nem ativar mídia/apps/volume percentual.

Se tiver acesso autorizado de escrita, crie branch codex/vexa-router-fixes a partir do BASE COMMIT e abra um PR para main; prefira commits separados por TASK. Não faça push diretamente em main, não faça merge e não descarte alterações alheias.

Sem acesso de escrita, devolva unified diff completo e aplicável à baseline. Se não conseguir ler o GitHub, peça os arquivos mínimos e declare BLOQUEADA; não invente contexto. Não presuma acesso ao filesystem do proprietário e não crie agentes internos.

Implemente os testes necessários, mas a execução dos builds/testes locais será feita pelo proprietário. Não afirmar testes não executados. Inclua roteiro curto, comandos e resultados esperados. Astra revisará código/PR e integrará após validação do proprietário.

## Retorno obrigatório

TASKS: TASK-004 + TASK-005
BASE COMMIT: d3e341a9d684f1fd2fb6d9490c8c6d554fbf502c
STATUS: IMPLEMENTADA_AGUARDANDO_TESTE / PARCIAL / BLOQUEADA
ALTEROU ARQUIVOS: SIM / NÃO
ENTREGA: link do PR + branch/commit, ou unified diff anexado
RESUMO: até cinco itens
TESTES CRIADOS: lista
TESTES EXECUTADOS: somente os reais, ou Nenhum
ROTEIRO PARA O PROPRIETÁRIO: comandos e passos curtos
PENDÊNCIAS/RISCOS: até três itens
PRÓXIMO PASSO PARA ASTRA: uma frase

Não declarar concluída sem PR/patch de implementação. Se parcial/bloqueada, explicar objetivamente o que falta.
