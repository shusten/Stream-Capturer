package com.stream.capturer.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OpenCVLoader {

    @PostConstruct
    public void init() {
        try {
            OpenCV.loadLocally(); // <- usa a extração automática
            log.info("OpenCV carregado com sucesso!");
        } catch (UnsatisfiedLinkError e) {
            log.error("Falha ao carregar a biblioteca nativa do OpenCV", e);
            throw e;
        }
    }
}
