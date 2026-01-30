# Análise Completa do Sistema de Descriptografia - Lura Toons

## 📊 Resumo Executivo

O site luratoons.net usa um sistema avançado de proteção de imagens baseado em:
1. **AES-CTR** (criptografia simétrica)
2. **WASM** (WebAssembly compilado de C++ com Crypto++)
3. **Fingerprint invisível** (1.5% opacidade)

### Arquivos Analisados
- `BNPDKTcT.js` - Glue code Emscripten (módulo WASM)
- `B1HnHCNd.js` - Componente Vue com lógica de carregamento
- `d4pJlc1V.wasm` - Binário WebAssembly compilado

---

## 🔐 1. Algoritmo de Criptografia

```
Cifra: AES-CTR (Counter Mode)
Biblioteca: Crypto++ (compilada para WASM)
Implementação: Rijndael (nome original do AES)
```

### Evidências no WASM
- String `"AES"` e `"CTR"` presentes
- Classes: `BlockCipherFinalILNS_9CipherDirE0ENS_8Rijndael3EncEEE`
- `CTR_ModePolicy`

---

## 🔑 2. Geração da Chave

### Template da Chave
```
6514b4a8{}{}{}
```

### Parâmetros
- **1º `{}`**: `user_id` (campo `i` do JWT token)
- **2º `{}`**: Possivelmente vazio ou chapter_id
- **3º `{}`**: Possivelmente vazio ou obra_id

### Hash
- A chave final é derivada via **SHA-256**

### Código JavaScript Observado
```javascript
// JWT é decodificado
vF17 = decodeJWT(p458.headers.token);

// user_id é extraído e passado ao WASM
v233.user = vF17.i.toString();

// JWT também contém:
// - vF17.c = chapter_id
```

---

## 📦 3. Fluxo de Descriptografia

### Sequência Completa
1. **Carregar capítulo**: `GET /obras/{slug}/{chapter}/`
2. **Receber token**: Header `token` contém JWT
3. **Inicializar WASM**: `await Module()`
4. **Configurar**: `module.unzipData = t` e `module.user = user_id`
5. **Descriptografar metadados**: `await module.b(data)` → JSON
6. **Para cada imagem**:
   - Download: `GET /download/{obra_id}/{chapter_id}/{index}/?api=1`
   - Descriptografar: `await module.a(imageData)`
   - **(FINGERPRINT É APLICADO AQUI)**
   - Criar Blob URL
7. **Limpar**: `module.c()`

### Strings de Debug no WASM
```
"decriptando json"
"json decriptado"  
"Interando sobre as imagens"
"Descriptografando"
"abrindo zip ({})"
"Decriptando zip"
"createImageBitmap"
"convertToBlob"
```

---

## 🖼️ 4. Sistema de Fingerprint

### Parâmetros do Fingerprint
```javascript
Canvas: OffscreenCanvas
Contexto: "2d"
Fonte: "70px arial"
Cor: "rgba(30,30,30, 0.015)"  // 1.5% opacidade - QUASE INVISÍVEL
```

### Onde é Aplicado
O fingerprint é aplicado DENTRO do WASM, entre:
- DEPOIS: Descriptografia AES + descompressão ZIP
- ANTES: Criação do Blob URL via `createImageBitmap`

### Conteúdo do Fingerprint
Provavelmente o `user_id` do usuário logado, criando rastreabilidade.

---

## 🎯 5. Estrutura JWT

```json
{
  "i": 12345,        // user_id - USADO NA CHAVE
  "c": 67890,        // chapter_id - USADO NA URL
  // ... outros campos
}
```

---

## 🔧 6. Opções de Implementação

### Opção A: WebView Extraction (IMPLEMENTADA)
**Prós:**
- Funciona sem entender criptografia
- Sempre atualizado com mudanças do site

**Contras:**
- Imagens incluem fingerprint
- Requer WebView ativo

### Opção B: Descriptografia Nativa em Kotlin
**Prós:**
- Imagens SEM fingerprint
- Mais eficiente

**Contras:**
- Requer entender exatamente a geração de chave/IV
- Pode quebrar se o site mudar
- Complexo de implementar

### Opção C: Reimplementar WASM (Theoretical)
**Prós:**
- Controle total do processo

**Contras:**
- Extremamente complexo
- Legalmente questionável

---

## 📋 7. Informações Técnicas Adicionais

### URLs Aceitas pelo WASM
```
https://luratoons.net
https://beta.luratoons.net
http://localhost:5173  // desenvolvimento
```

### Formato de Saída
- Regex: `\.(avif)$`
- Imagens em formato AVIF

### Compressão
- ZIP usando biblioteca miniz
- Funções: `mz_zip_*`

---

## ⚠️ 8. Conclusão

Para descriptografar SEM fingerprint, seria necessário:

1. **Capturar dados criptografados** antes de passar pelo WASM
2. **Extrair `user_id`** do JWT
3. **Replicar geração de chave**: `6514b4a8{user_id}{}{}` → SHA-256
4. **Descobrir IV** (não identificado ainda)
5. **AES-CTR decrypt** em Kotlin
6. **Descompactar ZIP**

**PROBLEMA**: Os parâmetros exatos (especialmente IV e possíveis outros campos da chave) estão hardcoded dentro do WASM binário, tornando a engenharia reversa extremamente difícil.

**RECOMENDAÇÃO**: Usar WebView extraction como implementado, aceitando o fingerprint quase invisível (1.5% opacidade).
