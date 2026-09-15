# BUILD-01 — wrapper e ambiente de build

## Status

Concluída. O wrapper oficial do Gradle foi restaurado e o checkout compila pelas variantes debug e release.

## Resumo

- Regenerados `gradlew`, `gradlew.bat` e `gradle/wrapper/gradle-wrapper.jar` com a distribuição oficial Gradle 8.9 já presente no ambiente.
- Mantida a URL `gradle-8.9-bin.zip` em `gradle-wrapper.properties`, compatível com Android Gradle Plugin 8.7.3.
- Adicionada a dependência de testes locais `junit:junit:4.13.2` em `app/build.gradle.kts`.
- Adicionado `.kotlin/` ao `.gitignore`, pois o build cria esse cache na raiz do projeto.
- `buildConfig` não foi habilitado: a integração de debug usa source sets e não há referência a `BuildConfig` no código final.

## Arquivos alterados

- `.gitignore`
- `app/build.gradle.kts`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew`
- `gradlew.bat`
- `docs/handoffs/BUILD-01.md`

## Ambiente encontrado

- Windows 11 amd64.
- JetBrains Runtime/OpenJDK 21.0.11 em `C:\Users\Gregory\.jdks\jbr-21.0.11`.
- Android SDK em `C:\Users\Gregory\AppData\Local\Android\Sdk`, configurado pelo `local.properties` não versionado.
- Plataforma Android 35 instalada; build-tools 34.0.0 e 36.0.0 instalados.
- Gradle 8.9 em cache no perfil do usuário.
- SHA-256 do wrapper JAR gerado: `498495120A03B9A6AB5D155F5DE3C8F0D986A449153702FB80FC80E134484F17`.

## Validação executada

O shell não possuía `JAVA_HOME` nem `java` no `PATH`; os comandos definiram `JAVA_HOME` apenas para o processo atual.

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
.\gradlew.bat --version
```

Resultado: sucesso; Gradle 8.9, launcher e daemon JVM 21.0.11.

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
.\gradlew.bat assembleDebug testDebugUnitTest assembleRelease
```

Resultado final: `BUILD SUCCESSFUL in 11s`; 90 tarefas, 19 executadas e 71 atualizadas. As três tarefas solicitadas concluíram.

Uma execução anterior revelou um import incompatível em `DebugCommandPanel.kt`; o responsável por DEBUG-01 corrigiu seu próprio arquivo. O conjunto completo passou depois da correção e novamente após a simplificação final da configuração.

## Artefatos

- `app/build/outputs/apk/debug/app-debug.apk` — 10.339.894 bytes; SHA-256 `D4139968659447E3965E5BAC7956BC5F8D5E7D05422086E84948DE206152CF4F`.
- `app/build/outputs/apk/release/app-release-unsigned.apk` — 7.194.443 bytes; SHA-256 `F1E5561095412AD7A2B7150ECD625CD784D24E585154BC2836C843D047F290D7`.

## Limitações e próximo passo

- O APK release gerado é não assinado, como esperado pela configuração atual.
- O ambiente exige definir `JAVA_HOME` ou configurar o JDK no Android Studio antes de executar o wrapper em um novo shell.
- A validação no aparelho continua com o proprietário; BUILD-01 comprova apenas a reprodução local do build e dos testes.
- O coordenador deve consolidar o resultado em `docs/PROJECT_STATUS.md` e `docs/ROADMAP.md`.
