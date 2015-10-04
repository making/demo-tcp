package demo;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class HelloServer {
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    ServerSocket server;

    public HelloServer(int port) throws IOException {
        this.server = new ServerSocket(port);
        log.info("listen {}", port);
    }

    public void start() throws IOException {
        executorService.execute(() -> {
            while (true) {
                try (Socket connection = server.accept();
                     Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
                    String input = scanner.next();
                    Writer out = new OutputStreamWriter(connection.getOutputStream());
                    out.write("Hi " + input);
                    out.flush();
                } catch (IOException ignored) {
                }
            }
        });
    }

    public void stop() throws IOException {
        log.info("stop...");
        this.executorService.shutdown();
        this.server.close();
    }

}
