package com.jangchwisa.userservice.grpc;

import com.jangchwisa.userservice.config.GrpcServerProperties;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcServerLifecycle implements SmartLifecycle {

    private final GrpcServerProperties grpcServerProperties;
    private final UserInternalGrpcService userInternalGrpcService;

    private volatile boolean running;
    private Server server;

    @Override
    public void start() {
        if (running) {
            return;
        }

        try {
            server = NettyServerBuilder.forPort(grpcServerProperties.port())
                    .addService(userInternalGrpcService)
                    .build()
                    .start();
            running = true;
            log.info("gRPC server started on port {}", grpcServerProperties.port());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start gRPC server", exception);
        }
    }

    @Override
    public void stop() {
        if (server == null) {
            running = false;
            return;
        }

        server.shutdown();
        try {
            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}
