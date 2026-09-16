# TASK-013 — fechar validação e estabilizar TASK-010–012

Status: CONCLUÍDA como consolidação documental e integração por aceite funcional do proprietário. Ver [evidências e limitações](../docs/handoffs/TASK-013.md).
Referência conferida: `b0ced16610ba65eca731b9695c735818e615b482`; confirmação não incluiu hash do APK instalado. Integração final: `a4789b1c9e3b6a74e325215487c849f133c6fcd3`. As seções abaixo preservam o roteiro da tarefa; conclusão efetiva está no handoff. Aplicar [protocolo de despacho](NEXT_TASKS.md).

## Problema e resultado

PRs #2/#3/#4 têm código e 29 testes novos, mas nenhum build/teste desta rodada foi executado. Antes de ampliar o app, transformar o relato de amanhã em evidência precisa e corrigir somente falhas verificadas. Não converter “funcionou” em aprovação de todos os cenários.

## Entrada necessária

- HEAD realmente instalado, comando de build e primeiro erro completo se houver.
- Resultado separado para texto, TTS, interrupção, STT, vozes e ausência de rede.
- Versão Android, modelo do aparelho e nome do motor de voz, se informados; campos desconhecidos permanecem desconhecidos.
- Identificar “serviço local ausente”, “modelo pt-BR ausente”, “permissão negada” e bug do app separadamente. Indisponibilidade esperada não autoriza habilitar rede.

Se só houver confirmação genérica, registrar exatamente o alcance relatado e manter os outros cenários pendentes. Pedir apenas os dados necessários à falha, sem solicitar gravação pessoal.

## Procedimento

1. Comparar HEAD testado com baselines nos handoffs. A base da TASK-012 contém 010/011; não aplicar patches duas vezes.
2. Verificar build debug/release, testes e manifesto debug/release a partir da evidência fornecida. Se faltarem logs, não inventar totais.
3. Classificar defeitos: bloqueio de build, ação duplicada/tardia, mic não liberado, erro de voz, layout ou indisponibilidade esperada. Corrigir os primeiros antes de aparência.
4. Para cada defeito reproduzível, registrar entrada/eventos, esperado/observado e teste de regressão possível sem hardware. Não alterar catálogo por suposição de erro do reconhecedor.
5. Quando necessário, manter correção no PR que introduziu o problema. Se houver dependentes, coordenador propaga e revisa o conjunto sem reescrever histórico publicado por surpresa.
6. Após aprovação, coordenador integra 010 → 011 → 012, retarget das filhas para main e revisão do diff. Executor não faz merge. Preferir preservar ancestralidade; se houver squash, revisar explicitamente commits aparentando duplicação.

## Arquivos e limites

Permitidos: arquivos tocados pelas 010–012, respectivos testes, build apenas se falha comprovada e docs de estado/handoffs. Não incluir voz persistente, IA, novo layout ou reconhecimento remoto. Se não houver defeito, entrega pode ser só documentação de validação, sem criar testes artificiais.

## Pontos da revisão estática a conferir no aparelho

- Permissão concedida exige novo toque; retorno do diálogo não reativa painel fechado.
- Stop aceito pelo motor não garante silêncio físico; observar autoescuta/eco antes de STT.
- Motor TTS sem callback pode permanecer preparando; fechar libera. Se observado, tratar timeout sem confundir timeout com sucesso.
- Seletor pode oferecer só uma voz; não prometer melhoria por quantidade.
- O painel acumulou controles e scroll; confirmar acesso a entrada, enviar e cancelar com teclado aberto.

## Aceite e passagem

Nenhuma ação duplicada após cancelamento; recursos liberados; texto utilizável nas falhas esperadas; evidências identificadas por HEAD. Criar tabela de cenários PASSOU/FALHOU/NÃO TESTADO em `docs/handoffs/TASK-013.md`. Só então liberar TASK-014. Se regressão persistir, manter status aguardando teste e fornecer correção concreta.
