package demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.tcp.TcpOperations;
import org.springframework.messaging.tcp.reactor.Reactor2TcpClient;
import org.springframework.util.SocketUtils;
import reactor.fn.Consumer;
import reactor.fn.Function;
import reactor.io.buffer.Buffer;
import reactor.io.codec.Codec;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DemoTcpApplication {

    @Slf4j
    @Configuration
    public static class AppConfig {
        int port = SocketUtils.findAvailableTcpPort();

        @Bean(destroyMethod = "shutdown")
        TcpOperations<String> tcpOperations() {
            return new Reactor2TcpClient<>("localhost", port,
                    new Codec<Buffer, Message<String>, Message<String>>() {
                        @Override
                        public Function<Buffer, Message<String>> decoder(Consumer<Message<String>> next) {
                            return bytes -> MessageBuilder.withPayload(bytes.asString()).build();
                        }

                        @Override
                        public Buffer apply(Message<String> message) {
                            return Buffer.wrap(message.getPayload());
                        }
                    });
        }

        @Bean(initMethod = "start", destroyMethod = "stop")
        HelloServer testTcpServer() throws IOException {
            return new HelloServer(port);
        }

        @Bean
        InitializingBean sampleRunner(TcpOperations<String> tcpOperations) {
            return () -> {
                log.info("start");
                IntStream.range(0, 10).forEach(i -> {
                    tcpOperations.connect(new TcpConnectionHandlerBuilder<String>()
                            .onConnect(c -> c.send(MessageBuilder.withPayload("Hello!" + i + "\n").build()))
                            .onMessage(m -> log.info(m.getPayload()))
                            .build());
                });
                log.info("end");
            };
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // System.setProperty("reactor.tcp.ioThreadCount", "10");
        new AnnotationConfigApplicationContext(AppConfig.class)
                .registerShutdownHook();
        TimeUnit.SECONDS.sleep(1);
        System.exit(0);
    }
}
