# 🔍 Desafio de Engenharia Reversa: Lura Toons WASM

## Contexto do Problema

O site **luratoons.net** implementa um sistema avançado de proteção de conteúdo usando WebAssembly (WASM) compilado de C++ com a biblioteca Crypto++. O objetivo é realizar engenharia reversa para entender como remover o **fingerprint invisível** aplicado nas imagens dos mangás.

---

## 🎯 Objetivo Principal

**Descriptografar as imagens dos capítulos SEM o fingerprint de usuário** que é aplicado pelo módulo WASM antes de exibir no browser.

### Por que isso é difícil?

1. O código está em WebAssembly (binário compilado)
2. A lógica de criptografia e fingerprint está toda dentro do WASM
3. O fingerprint é aplicado DENTRO do processo de descriptografia
4. Não há separação clara entre "descriptografar" e "aplicar fingerprint"

---

## 📁 Arquivos do Sistema

### Arquivos WASM/JS Principais

| Arquivo | Propósito |
|---------|-----------|
| `d4pJlc1V.wasm` | Binário WebAssembly principal (compilado de C++) |
| `BNPDKTcT.js` | Glue code Emscripten (interface JS ↔ WASM) |
| `B1HnHCNd.js` | Componente Vue que usa o módulo WASM |
| `wasm-C5IAUDsv.js` | Worker para multithreading (pthread) |

### Localização dos Arquivos (para análise)

```
src/pt/randomscan/src/eu/kanade/tachiyomi/extension/pt/randomscan/temp/
├── BNPDKTcT.js       # 4804 linhas
├── B1HnHCNd.js       # 3519 linhas
├── wasm-C5IAUDsv.js  # 4804 linhas (worker)
└── d4pJlc1V.wasm     # Binário WASM
```

---

## 🔐 Sistema de Criptografia Identificado

### Algoritmo
- **Cifra**: AES-CTR (Advanced Encryption Standard - Counter Mode)
- **Biblioteca**: Crypto++ (compilada para WASM via Emscripten)
- **Implementação Interna**: Rijndael (nome original do AES)

### Geração da Chave

```
Template: 6514b4a8{}{}{}
```

**Parâmetros conhecidos:**
- **1º `{}`**: `user_id` (campo `i` do JWT token recebido no header)
- **2º `{}`**: ❓ Desconhecido (possivelmente vazio ou chapter_id)
- **3º `{}`**: ❓ Desconhecido (possivelmente vazio ou obra_id)

**Processo:**
1. String é montada com o template + parâmetros
2. SHA-256 é aplicado na string
3. Resultado de 32 bytes é usado como chave AES

### IV (Initialization Vector)
- **Status**: ❓ NÃO IDENTIFICADO
- **Necessário para**: Descriptografia AES-CTR
- **Onde procurar**: Código WASM próximo às funções AES

---

## 🖼️ Sistema de Fingerprint

### Parâmetros Descobertos

| Parâmetro | Valor |
|-----------|-------|
| Canvas | `OffscreenCanvas` |
| Contexto | `"2d"` |
| Fonte | `"70px arial"` |
| Cor | `"rgba(30,30,30, 0.015)"` |
| Opacidade | **1.5%** (quase invisível) |

### Fluxo do Fingerprint

```
1. Imagem descriptografada (AES-CTR)
2. Descomprimida (ZIP/miniz)
3. Criado OffscreenCanvas com dimensões da imagem
4. Imagem original desenhada no canvas (drawImage)
5. fillStyle definido como rgba(30,30,30, 0.015)
6. font definido como "70px arial"
7. fillText() com user_id - FINGERPRINT APLICADO AQUI
8. canvas.convertToBlob() - imagem final com fingerprint
9. URL.createObjectURL() - blob URL para exibição
```

---

## 🔄 Fluxo Completo de Carregamento

### JavaScript (Observável)

```javascript
// 1. Inicializar módulo WASM
const module = await Module();  // BNPDKTcT.js

// 2. Configurar módulo
module.unzipData = unzipFunction;
module.user = jwt.i.toString();  // user_id do JWT

// 3. Descriptografar metadados do capítulo
chapterData = await module.b(encryptedChapterData);

// 4. Para cada imagem do capítulo
for (let i = 0; i < chapterData.files; i++) {
    // 4a. Baixar imagem criptografada
    const encrypted = await fetch(`/download/${obra_id}/${chapter_id}/${i}/?api=1`);
    
    // 4b. Descriptografar (FINGERPRINT APLICADO INTERNAMENTE)
    const decrypted = await module.a(encrypted);  // <- CRÍTICO!
    
    // 4c. Exibir
    displayImage(decrypted);
}

// 5. Limpar
module.c();
```

### Dentro do WASM (Inferido)

```c
// Pseudo-código do que provavelmente acontece em module.a()

Blob* processImage(uint8_t* encryptedData, size_t length) {
    // 1. Gerar chave
    char key_template[] = "6514b4a8{}{}{}";
    char key_str[100];
    sprintf(key_str, key_template, user_id, param2, param3);
    
    uint8_t key[32];
    SHA256(key_str, strlen(key_str), key);
    
    // 2. Descriptografar com AES-CTR
    uint8_t* decrypted = AES_CTR_Decrypt(encryptedData, key, iv);
    
    // 3. Descompactar ZIP
    uint8_t* imageData = unzip(decrypted);
    
    // 4. FINGERPRINT - Isso precisa ser bypassado!
    val canvas = val::global("OffscreenCanvas").new_(width, height);
    val ctx = canvas["getContext"]("2d");
    
    // Desenha imagem original
    val img = val::global("createImageBitmap")(imageData);
    ctx["drawImage"](img, 0, 0);
    
    // Aplica fingerprint invisível
    ctx.set("fillStyle", "rgba(30,30,30, 0.015)");
    ctx.set("font", "70px arial");
    ctx["fillText"](user_id_str, x, y);  // <- FINGERPRINT!
    
    // 5. Retornar blob
    return canvas["convertToBlob"]();
}
```

---

## 🔬 Análise no Ghidra

### Strings Importantes para Buscar

| String | Endereço Encontrado | Propósito |
|--------|---------------------|-----------|
| `6514b4a8{}{}{}` | `ram:800a7fe9` | Template da chave |
| `SHA-256` | - | Função de hash |
| `OffscreenCanvas` | - | Criação do canvas |
| `70px arial` | - | Fonte do fingerprint |
| `rgba(30,30,30` | - | Cor do fingerprint |
| `fillText` | - | Desenho do fingerprint |
| `createImageBitmap` | - | Criação da imagem |
| `Criando chave` | - | Debug - geração de chave |
| `Descriptografando` | - | Debug - processo de decrypt |

### Como Encontrar XREFs

1. No Ghidra, vá para o endereço `ram:800a7fe9`
2. Clique com botão direito → **References → Show References to Address**
3. Ou pressione **Ctrl+Shift+F**
4. Isso mostra quais funções usam a string `6514b4a8{}{}{}`

### Funções WASM Exportadas

| Nome JS | Nome WASM | Propósito |
|---------|-----------|-----------|
| `_main` | `La` | Ponto de entrada |
| `_malloc` | - | Alocação de memória |
| `_free` | - | Liberação de memória |
| Funções `Ja-Zb` | - | Diversas funções internas |

---

## ❓ Informações Necessárias para Bypass

### Para Descriptografar Nativamente

| Item | Status | Prioridade |
|------|--------|------------|
| Formato completo da chave | ⚠️ Template conhecido, parâmetros parciais | ⭐⭐⭐ |
| IV (Initialization Vector) | ❌ Desconhecido | ⭐⭐⭐ |
| Parâmetros 2 e 3 da chave | ❌ Desconhecidos | ⭐⭐ |
| Tamanho do bloco AES | ✅ 16 bytes (AES-128/256) | ⭐ |
| Formato do ZIP | ✅ miniz standard | ⭐ |

### Para Remover Fingerprint

| Item | Status | Prioridade |
|------|--------|------------|
| Localização exata do código | ⚠️ Inferido, não confirmado | ⭐⭐⭐ |
| Se é removível ou integrado | ❌ Desconhecido | ⭐⭐⭐ |
| Alternativas (patch WASM) | ❓ A avaliar | ⭐⭐ |

---

## 🛠️ Próximos Passos da Análise

### 1. Mapear a Função de Chave (Prioridade Alta)
- Encontrar XREFs para `6514b4a8{}{}{}`
- Identificar a função que monta a chave
- Extrair os parâmetros 2 e 3
- Encontrar o IV

### 2. Mapear a Função de Fingerprint (Prioridade Alta)
- Buscar por `OffscreenCanvas` e `fillText`
- Identificar onde exatamente o fingerprint é aplicado
- Avaliar se pode ser bypassado

### 3. Analisar Possibilidades de Bypass
- **Opção A**: Interceptar dados APÓS descriptografia, ANTES do fingerprint
- **Opção B**: Patch no WASM para pular o código de fingerprint
- **Opção C**: Reimplementar descriptografia em Kotlin (sem fingerprint)

---

## 📋 Ferramentas Recomendadas

| Ferramenta | Propósito |
|------------|-----------|
| **Ghidra + WASM Plugin** | Descompilação do WASM |
| **GhidraMCP** | Integração com LLMs para análise |
| **wasm2wat** | Conversão WASM → WAT (texto) |
| **wasm-decompile** | Decompilação alternativa |
| **Chrome DevTools** | Debug em tempo real |

---

## 📚 Referências

- [WebAssembly Specification](https://webassembly.org/specs/)
- [Crypto++ Library](https://www.cryptopp.com/)
- [Emscripten Documentation](https://emscripten.org/docs/)
- [AES-CTR Mode](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Counter_(CTR))
- [GhidraMCP Repository](https://github.com/LaurieWired/GhidraMCP)

---

## 🤝 Como Contribuir

Se você conseguir:
1. Encontrar os XREFs para `6514b4a8{}{}{}` - **documente a função**
2. Identificar o IV - **compartilhe o valor ou método de derivação**
3. Mapear a função de fingerprint - **descreva o fluxo exato**
4. Propor um bypass - **descreva a abordagem**

---

## ⚠️ Aviso Legal

Esta análise é para fins educacionais e de pesquisa em segurança. O uso de técnicas de bypass para violar termos de serviço ou direitos autorais não é recomendado.

---

*Última atualização: Janeiro 2025*
*Status: Em andamento - Fase de análise do WASM no Ghidra*
