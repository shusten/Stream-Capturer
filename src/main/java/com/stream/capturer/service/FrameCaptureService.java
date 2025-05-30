package com.stream.capturer.service;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
public class FrameCaptureService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8000") // URL do CompreFace
            .build();

    public Mono<String> capturarEEnviar(String rtspUrl, String apiKey) {
        log.info("Capturando imagem da câmera: {}", rtspUrl);

        VideoCapture camera = new VideoCapture(rtspUrl);
        if (!camera.isOpened()) {
            log.error("Não foi possível abrir o stream RTSP.");
            return Mono.error(new RuntimeException("Erro ao abrir a câmera."));
        }

        Mat frame = new Mat();
        if (!camera.read(frame)) {
            log.error("Falha ao capturar frame.");
            camera.release();
            return Mono.error(new RuntimeException("Falha ao capturar imagem da câmera."));
        }

        camera.release();

        try {
            File tempFile = Files.createTempFile("frame_", ".jpg").toFile();
            Imgcodecs.imwrite(tempFile.getAbsolutePath(), frame);
            log.info("Frame salvo em {}", tempFile.getAbsolutePath());

            return enviarParaCompreFace(tempFile, apiKey)
                    .doFinally(signal -> {
                        if (!tempFile.delete()) {
                            log.warn("Não foi possível deletar o arquivo temporário: {}", tempFile.getAbsolutePath());
                        }
                    });

        } catch (IOException e) {
            log.error("Erro ao salvar frame como imagem", e);
            return Mono.error(e);
        }
    }

    private Mono<String> enviarParaCompreFace(File file, String apiKey) {
        log.info("Enviando imagem para CompreFace...");

        return webClient.post()
                .uri("/api/v1/recognition/recognize")
                .header("x-api-key", apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", new FileSystemResource(file)))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("Resposta do CompreFace: {}", response));
    }
}
