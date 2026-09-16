# Despacho — corrigir TASK-014 e implementar TASK-015

Autorização do proprietário: acumular testes e continuar desenvolvimento, mantendo processo habitual de executor externo. Não executar builds/testes automaticamente, nem merge em main.

## Referências fixas

Repositório: GregoryPina/delamain.
PR da TASK-014: https://github.com/GregoryPina/delamain/pull/5
Branch inicial: codex/task-014-voice-preference.
HEAD INICIAL EXATO: `8e4d8bc09e236ec1429f2a6935ba05691eccf21b`.
Base original da 014: `f7e851bc799b018cd58c6f1bf2b15a54511f2c3f`.

Ler este despacho, revisão PR-005-REVIEW e instruções atuais pela main publicada. Implementação parte do HEAD inicial acima, não da main: ela ainda não contém 014. Se HEAD remoto mudou, revisar delta antes e registrar nova referência, sem sobrescrever trabalho.

## Fase A — corrigir PR #5

Ler `docs/handoffs/PR-005-REVIEW.md` na main. Corrigir R1–R4 e feedback de USAR PADRÃO. Incluir testes de sessão encerrada/reaberta, restore versus gravação pendente, cancelamento de corrotinas, jobs invertidos save/clear e status TTS não oculto. Criar testes sem executá-los. Manter correção limitada à 014 e atualizar seu handoff.

Publicar commits na branch original sem force push; PR #5 permanece draft e IMPLEMENTADA_AGUARDANDO_TESTE. Registrar SHA completo do último commit desta fase; esse será BASE COMMIT da fase B. Não afirmar que correção passou em teste sem executá-lo. Sem escrita, entregar patch A contra HEAD inicial e patch B separado contra resultado de A, com instruções claras.

## Fase B — TASK-015

Criar `codex/task-015-session-state` a partir do HEAD corrigido da fase A. PR draft com BASE `codex/task-014-voice-preference`, não main; assim mostra somente delta da 015. Não misturar 015 no PR #5.

Implementar `tasks/TASK-015.md`: coordenador único de interação e rosto por eventos reais, lifetime explícito, IDs de interação e timers canceláveis. Preservar contratos de persistência corrigidos; não duplicar store ou portas. Manter assets/layout/efeitos, release sem microfone e sem promover controles da 016. Não iniciar 016+.

Exceção autorizada à dependência “014 integrada”: basta fase A implementada e revisada estaticamente pelo executor para iniciar a branch filha. Nenhuma das duas é funcionalmente validada. Coordenador fará revisão conjunta depois; testes acumulados não liberam merge.

A sessão de preferência e a interação de voz têm lifetimes relacionados, mas distintos: leitura antiga não restaura durante sessão nova, gravação confirmada não deve se perder por recomposição, e não se declara salva antes do retorno. Explicitar essa propriedade no handoff da 015.

## Entrega obrigatória

Dois PRs/patches separados; HEAD corrigido da 014 e BASE/HEAD da 015 completos. Listar R1–R4 resolvidos com arquivo/teste correspondente, testes criados e “TESTES EXECUTADOS: nenhum”. Atualizar handoffs 014/015 e roteiro conjunto de persistência + rosto/sessão + regressão microfone/fala/cancelar/vozes. Incluir comando de build para proprietário; usar JAVA_HOME já documentado em BUILD-01 se necessário.

Status de ambas: IMPLEMENTADA_AGUARDANDO_TESTE. Não marcar como concluídas, não fazer merge, não criar subagentes. Se houver impedimento técnico real, entregar correção possível e descrever bloqueio, sem alegar cumprimento parcial como conclusão.
