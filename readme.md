# Fingerprint Integration Service

## Descrição

Este projeto é um serviço de integração biométrica responsável por intermediar a comunicação entre a API principal e leitores de impressão digital (ex: ControlID e Hamster DX).

O serviço:

- executa um servidor HTTP local
- encapsula SDKs nativos dos fabricantes
- expõe uma API unificada para inicialização, captura e comparação de digitais
- opera de forma independente do Java instalado na máquina

A aplicação é distribuída como um executável (`FingerprintService.exe`) contendo um runtime Java embutido (JRE 8 x86), garantindo compatibilidade com SDKs e DLLs biométricas 32-bit.

---

## Fluxo de comunicação

```text
Flutter
   ↓
Go API
   ↓ HTTP localhost
FingerprintService.exe
   ↓
Java Runtime (x86 embutido)
   ↓
SDKs biométricos (DLLs)
```

---

## Pré-requisitos
- JDK instalado (apenas para build do projeto)
- Gradle
- Windows (para geração do executável)
- Launch4j

---

## Gerando o JAR
Na raiz do projeto, execute:
```bash
gradle clean build
```

O arquivo gerado estará em:
```
build/libs/
```

Copie o JAR para e renomeie:
```
lib/fingerprint-process.jar
```

---

## Estrutura final do projeto
```
fingerprint/
   FingerprintService.exe
   libs/
      fingerprint-process.jar
      runtime/
         bin/
         lib/
```

> **Importante:** o diretório `runtime` deve conter o JRE 8 x86 completo.

---

## Configurando o Java embutido

Baixe e instale:

```text id="jlwmws"
jre-8uXXX-windows-i586
```

Instale diretamente em:
```
libs/runtime
```

Verifique se existe:
```
libs/runtime/bin/java.exe
```

---

## Gerando o Executável (Launch4j)

Abra o Launch4j e configure:
### Basic
- Output file: `FingerprintService.exe`
- Jar: `libs/fingerprint-process.jar`

### JRE
- JRE Path: `libs/runtime`
- Desmarcar: `64-bit required`

### Single instance
- Marcar: `Allow only a single instance of the application`

![Launch4j Configuration](docs/launch4j.png)

## Build do executável
Clique em "Build Wrapper" para gerar o `FingerprintService.exe` na raiz do projeto.

Será gerado um executável que inclui o JRE embutido, garantindo compatibilidade com os SDKs biométricos 32-bit.

---

## Testando o serviço
Execute: `FingerprintService.exe`

Valide: `http://localhost:8080/health` retorna `OK`.

## Integração com VRPdvProAPI
O processo é iniciado diretamente via:
```
[]string{
   "./libs/FingerprintService.exe",
}
```
## Observações importantes
- o runtime Java embutido deve ser x86 (32-bit) para compatibilidade com SDKs biométricos 32-bit
- o serviço é independente do Java instalado na máquina, garantindo portabilidade
- o executável é apenas um launcher — o JAR pode ser atualizado sem recriar o EXE
- a comunicação é feita via HTTP local, permitindo fácil integração com a API principal e outros serviços