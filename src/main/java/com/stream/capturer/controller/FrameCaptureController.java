package com.stream.capturer.controller;

import com.stream.capturer.service.FrameCaptureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/captura")
@RequiredArgsConstructor
@Slf4j
public class FrameCaptureController {

    private final FrameCaptureService frameCaptureService;

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarCaptura() {
        try {
            boolean iniciou = frameCaptureService.iniciarCaptura();
            if (iniciou) {
                return ResponseEntity.ok("Captura iniciada com sucesso.");
            } else {
                return ResponseEntity.status(409).body("A captura já está em andamento.");
            }
        } catch (Exception e) {
            log.error("Erro ao iniciar captura", e);
            return ResponseEntity.internalServerError().body("Erro ao iniciar captura: " + e.getMessage());
        }
    }

    @PostMapping("/parar")
    public ResponseEntity<String> pararCaptura() {
        boolean parou = frameCaptureService.pararCaptura();
        if (parou) {
            return ResponseEntity.ok("Captura parada com sucesso.");
        } else {
            return ResponseEntity.status(409).body("A captura já estava parada.");
        }
    }
}
