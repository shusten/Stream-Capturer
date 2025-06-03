package com.stream.capturer.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.bytedeco.ffmpeg.global.avutil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Service
@Slf4j
public class FrameCaptureService {

    @Value("${camera.rtsp.url}")
    private String rtspUrl;

    private volatile boolean capturando = false;
    private Thread threadCaptura;

    public synchronized boolean iniciarCaptura() {
        if (capturando) {
            log.warn("Captura já está em andamento.");
            return false;
        }

        capturando = true;
        log.info("Iniciando captura contínua com FFmpegFrameGrabber a cada 2 segundos...");

        threadCaptura = new Thread(() -> {
            // Silencia os avisos do FFmpeg (como "deprecated pixel format")
            avutil.av_log_set_level(avutil.AV_LOG_ERROR);

            FFmpegFrameGrabber grabber = null;

            try {
                grabber = new FFmpegFrameGrabber(rtspUrl);
                grabber.setFormat("rtsp");
                grabber.setOption("rtsp_transport", "tcp");
                grabber.start();

                Java2DFrameConverter converter = new Java2DFrameConverter();
                long ultimoTimestamp = System.currentTimeMillis();

                while (capturando) {
                    try {
                        Frame frame = grabber.grabImage();

                        if (frame != null) {
                            long agora = System.currentTimeMillis();
                            if (agora - ultimoTimestamp >= 2000) {
                                BufferedImage imagem = converter.convert(frame);
                                salvarImagem(imagem);
                                log.info("Frame capturado com sucesso em {} ms.", agora);
                                ultimoTimestamp = agora;
                            }
                        } else {
                            log.warn("Frame nulo recebido.");
                        }

                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Erro durante a captura do frame", e);
                    }
                }

            } catch (Exception e) {
                log.error("Erro ao iniciar o stream da câmera", e);
            } finally {
                try {
                    if (grabber != null) {
                        grabber.stop();
                        grabber.release();
                        log.info("Captura encerrada.");
                    }
                } catch (Exception e) {
                    log.error("Erro ao encerrar o stream da câmera", e);
                }
            }
        });

        threadCaptura.start();
        return true;
    }
    public synchronized boolean pararCaptura() {
        if (!capturando) {
            return false;
        }

        capturando = false;
        threadCaptura.interrupt();
        return true;
    }

    @PreDestroy
    public void encerrar() {
        pararCaptura();
    }

    private void salvarImagem(BufferedImage imagem) {
        try {
            File pastaCapturas = new File("capturas");
            if (!pastaCapturas.exists()) {
                boolean criada = pastaCapturas.mkdirs();
                if (!criada) {
                    log.warn("Não foi possível criar a pasta de capturas.");
                }
            }

            String nomeArquivo = "captura_" + System.currentTimeMillis() + ".jpg";
            File arquivo = new File(pastaCapturas, nomeArquivo);
            ImageIO.write(imagem, "jpg", arquivo);
            log.info("Imagem salva em: {}", arquivo.getAbsolutePath());
        } catch (Exception e) {
            log.error("Erro ao salvar imagem capturada", e);
        }
    }
}
