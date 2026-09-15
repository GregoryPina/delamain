# DEBUG-01 — entrada manual de desenvolvimento

Status: implementação e compilação concluídas; validação visual em dispositivo pendente.

## Resumo

Foi integrada uma entrada manual pequena e recolhível para exercitar o motor de comandos locais sem microfone. O controle começa recolhido como `DEV`, preservando a tela de boot e a interface visual já aceita. Ao abrir, aceita texto pelo botão `ENVIAR` ou pela ação de envio do teclado e mostra a intenção/resposta reconhecida ou `Comando local não reconhecido.`.

A implementação é separada por variante: `debug` contém o painel funcional e `release` fornece um composable vazio. O motor permanece na camada de domínio; a UI apenas envia o texto e apresenta o resultado. O painel não altera os estados visuais, não simula TTS/STT e não usa rede.

## Arquivos alterados

- `app/src/main/java/com/gregorypina/delamain/ui/DelamainApp.kt`
- `app/src/debug/java/com/gregorypina/delamain/ui/DebugCommandPanel.kt`
- `app/src/release/java/com/gregorypina/delamain/ui/DebugCommandPanel.kt`
- `docs/handoffs/DEBUG-01.md`

## Integração

`DelamainApp` sobrepõe `DebugCommandPanel()` à tela existente. Cada source set fornece a mesma assinatura:

```kotlin
@Composable
internal fun DebugCommandPanel()
```

Na variante `debug`, o painel mantém uma instância de `LocalCommandEngine` enquanto a tela existir e trata exaustivamente `LocalCommandResult.Recognized` e `LocalCommandResult.Unknown`. Na variante `release`, a função não renderiza conteúdo.

O contêiner usa áreas seguras, ajuste para o teclado, altura limitada e rolagem vertical. O cabeçalho reserva espaço para `FECHAR` em telas horizontais estreitas. As cores do campo foram declaradas para leitura sobre o fundo escuro.

## Validação

- `git diff --check` nos arquivos da tarefa: passou; o Git informou apenas a conversão futura de LF para CRLF no arquivo principal já existente.
- Primeira compilação debug coordenada por BUILD-01: falhou por um import isolado de `weight` incompatível com a versão do Compose; o import foi removido, mantendo os usos válidos no escopo de `Row`.
- Reexecução coordenada por BUILD-01: `assembleDebug testDebugUnitTest assembleRelease` concluiu com `BUILD SUCCESSFUL`; as variantes debug e release compilaram, os 10 testes do motor local passaram sem falhas e foram gerados o APK debug e o APK release não assinado.

## Roteiro manual

1. Executar a variante `debug`; confirmar que apenas o pequeno botão `DEV` aparece e que o boot/rosto continuam visíveis.
2. Abrir `DEV`, digitar `Delamain, está aí?` e enviar; confirmar intenção `PRESENCE` e uma resposta local.
3. Enviar `que horas são?`; confirmar intenção `TIME` com a hora do aparelho.
4. Enviar `não, obrigado`; confirmar `Comando local não reconhecido.`.
5. Abrir o teclado em landscape e verificar que campo, envio, resposta e `FECHAR` continuam acessíveis, usando rolagem quando necessário.
6. Fechar e reabrir o painel; confirmar que o rosto permanece operável e que as variantes do motor continuam alternando.
7. Executar a variante `release`; confirmar ausência de `DEV` e de qualquer painel de entrada.

## Limitações

- A validação visual e do teclado em aparelho/emulador ainda depende do proprietário ou de QA-01.
- O painel é uma ferramenta manual de desenvolvimento; não há voz, rede, persistência ou mudança automática dos estados do rosto.

## Próximo responsável

BUILD-01 registra os resultados finais do Gradle. QA-01 executa o roteiro manual, incluindo landscape com teclado e a ausência dos controles na variante release. O coordenador atualiza `PROJECT_STATUS.md` e `ROADMAP.md` após revisar o conjunto.
