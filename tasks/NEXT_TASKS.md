# Próximas tarefas — guia de despacho

Planejamento de 2026-09-16, solicitado pelo proprietário. TASK-013 consolidada após aceite funcional. TASK-014–017 implementadas em branches (`codex/task-014-voice-preference` … `codex/task-017-mute-controls`), aguardando validação em massa; TASK-018+ não iniciadas. Referência histórica de leitura: `985af5faf0119a20ff19d32f49ed77f02c1fa1bb` (TASK-012). Baselines de execução futura serão informadas no despacho de cada TASK.

## Ordem e dependências

| TASK | Entrega | Depende de | Liberação |
| --- | --- | --- | --- |
| [013](TASK-013.md) | Consolidar validação e corrigir regressões 010–012 | Relato do proprietário sobre PRs #2/#3/#4 | Concluída; ver handoff TASK-013 |
| [014](TASK-014.md) | Lembrar a voz escolhida | 013 integrada | Implementação |
| [015](TASK-015.md) | Coordenar sessão e estados reais do rosto | 013/014 | Implementação |
| [016](TASK-016.md) | Controles de uso fora do DEV | 015 | Implementação com revisão visual |
| [017](TASK-017.md) | Parar/cancelar e modo mute (sem TTS automático) | 016 | Implementada; aguardando teste |
| [018](TASK-018.md) | Nome e tom de personalidade configuráveis | 014/017 | Implementada; aguardando teste |
| [019](TASK-019.md) | Foco de áudio e matriz de rotas | 017; aparelho disponível | Implementada; aguardando teste no aparelho |
| [020](TASK-020.md) | Conversa de IA com provider falso | 015/017/018/019 | Implementação sem rede |
| [021](TASK-021.md) | Primeiro provider real de IA | 020 + provider/custos/credenciais decididos | Condicionada à decisão do proprietário |
| [022](TASK-022.md) | Estudo de wake word local | 013/019 + medições no aparelho | Pesquisa; não habilita escuta contínua |

A numeração é a ordem recomendada, não autorização para abrir todas as branches. TASK-022 pode ser estudada antes da 021 se a escolha do provider estiver pendente. Correções encontradas na 013 têm prioridade sobre qualquer funcionalidade. Nenhuma data de conclusão prometida.

## Protocolo obrigatório para todos os executores

1. Ler AGENTS, README, arquitetura, roadmap, estado, handoff e documentação específica. Este guia complementa, não substitui essas fontes.
2. Coordenador fornece BASE COMMIT completo publicado, branch base, uma TASK, caminhos permitidos, evidências relevantes e dependências fechadas. Se só houver este planejamento, a tarefa ainda não foi despachada; não escolher hash antigo por conveniência.
3. Conferir árvore/branch e preservar trabalho alheio. A branch `codex/<tarefa>` parte da base informada. Não usar reset/clean, não ativar motor remoto legado, não tocar no STL local. Não criar agentes internos.
4. Implementar apenas o recorte; contratos propostos nas TASKs podem ser adaptados com justificativa. Não duplicar serviços, estado, normalizadores ou armazenamento para contornar um contrato existente.
5. Criar testes de regressão para comportamento relevante; não executar builds/testes automaticamente. Proprietário executa `./gradlew.bat testDebugUnitTest assembleDebug assembleRelease` e roteiro manual específico. Revisão estática não é teste aprovado.
6. Entregar PR draft ou unified diff. Nunca main/merge pelo executor. Não publicar segredos, áudio ou transcrições em logs/PR. Sem introdução de dependência ou serviço externo fora do recorte.
7. Criar `docs/handoffs/TASK-NNN.md`: contexto mínimo, decisões, arquivos, contrato novo/alterado, estados de erro/cancelamento, testes criados versus executados, roteiro, pendências e próximo passo. Coordenador atualiza documentos de estado após revisar, sem apagar história.
8. Aceite exige evidência vinculada ao HEAD. Se código mudar após teste, analisar delta; não reaproveitar aprovação automaticamente. Dependência aprovada conceitualmente não significa integrada.

## Modelo do retorno

```text
TASK:
BASE COMMIT:
HEAD / BRANCH:
STATUS: IMPLEMENTADA_AGUARDANDO_TESTE | BLOQUEADA | CONCLUÍDA
RESULTADO DA REVISÃO: não realizada | alterações necessárias | aprovada estaticamente
ALTEROU ARQUIVOS: SIM/NÃO
ENTREGA: URL do PR ou patch
RESUMO: comportamento antes/depois
CONTRATOS ALTERADOS:
TESTES CRIADOS:
TESTES EXECUTADOS: comando, quem, HEAD, resultado; ou “nenhum”
VALIDAÇÃO NO APARELHO: relato real ou pendente
PENDÊNCIAS / LIMITAÇÕES:
PRÓXIMO PASSO PARA COORDENADOR:
```

Se for tarefa de pesquisa, usar STATUS: PESQUISA_ENTREGUE e indicar decisão ainda pendente. Uma revisão que não encontra defeito pode terminar sem código; tarefa de implementação não se encerra com relatório sem patch.

## Regras de planejamento

Nomes de novas classes são sugestões, não arquivos existentes. Versões de dependência e comportamento de APIs devem ser conferidos em documentação oficial na execução. Não copiar a versão “mais recente” sem verificar compatibilidade com o projeto. Os testes abaixo são cenários, não contagens prometidas. Defaults indicados são propostas de produto, e devem ser identificados assim no despacho quando ainda não aprovados pelo proprietário.
