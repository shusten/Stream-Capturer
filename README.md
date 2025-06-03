## 📸 Stream Capturer – API de Captura de Imagens RTSP

### 🔍 Visão Geral

A **Stream Capturer** é uma API desenvolvida em Java que:

* Conecta-se a câmeras IP via protocolo **RTSP**;
* Captura imagens do stream **a cada 1 segundo**;
* Envia essas imagens para o **CompreFace**, onde está mantida uma **lista negra** (pessoas de interesse);
* Recebe eventos de detecção sempre que um rosto da lista negra é identificado;
* Encaminha esses eventos para uma outra API, que é responsável por:

  * Geração de relatórios;
  * Registro de ocorrências;
  * Ações administrativas (fora do escopo desta API).

### ⚙️ Tecnologias Utilizadas

* **Java 21**
* **Spring Boot 3.4.6**
* **JavaCV** (`FFmpegFrameGrabber`) para captura de stream
* **FFmpeg** (via `javacv-platform`) para suporte a protocolos como RTSP
* **REST APIs** para comunicação com o CompreFace e APIs externas

### 📊 Fluxo de Operação

1. Conecta ao stream da câmera via RTSP;
2. Captura e envia uma imagem por segundo ao CompreFace;
3. Aguarda resposta de detecção facial;
4. Ao detectar um rosto da lista negra, o CompreFace envia um evento para esta API;
5. Esta API trata e repassa o evento para o sistema de relatórios e controle externo.

---

Essa é a funcionalidade principal da API. Funcionalidades auxiliares podem ser adicionadas conforme a evolução do sistema.
