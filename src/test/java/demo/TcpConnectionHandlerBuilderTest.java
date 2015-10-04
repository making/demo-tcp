package demo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.tcp.TcpOperations;
import org.springframework.messaging.tcp.reactor.Reactor2TcpClient;
import org.springframework.util.SocketUtils;
import reactor.fn.Consumer;
import reactor.fn.Function;
import reactor.io.buffer.Buffer;
import reactor.io.codec.Codec;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TcpConnectionHandlerBuilderTest {
    int port = SocketUtils.findAvailableTcpPort();
    ServerSocket server;
    TcpOperations<String> tcpOperations;

    @Before
    public void setUp() throws Exception {
        server = new ServerSocket(port);
        tcpOperations = createTcpOperations(port);
    }

    TcpOperations<String> createTcpOperations(int port) {
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

    @After
    public void tearDown() throws Exception {
        tcpOperations.shutdown();
        server.close();
    }

    @Test
    public void testOnMessage() throws Exception {
        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicReference<String> received = new AtomicReference<>("");
        CountDownLatch latch = new CountDownLatch(1);
        tcpOperations.connect(new TcpConnectionHandlerBuilder<String>()
                .onConnect(c -> {
                    connected.set(true);
                    c.send(MessageBuilder.withPayload("test\n").build());
                })
                .onMessage(m -> {
                    received.set(m.getPayload());
                    latch.countDown();
                })
                .build());
        try (Socket socket = server.accept(); Scanner scanner = new Scanner(socket.getInputStream())) {
            Writer out = new OutputStreamWriter(socket.getOutputStream());
            out.write("Hi " + scanner.next());
            out.flush();
        }
        latch.await(1, TimeUnit.SECONDS);
        assertThat(connected.get(), is(true));
        assertThat(received.get(), is("Hi test"));
    }

    @Test
    public void testOnConnectFailure() throws Exception {
        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        TcpOperations<String> tcpOperations = createTcpOperations(SocketUtils.findAvailableTcpPort());
        tcpOperations.connect(new TcpConnectionHandlerBuilder<String>()
                .onConnect(c -> connected.set(true))
                .onConnectFailure(ex -> {
                    failed.set(true);
                    latch.countDown();
                })
                .build());
        latch.await(1, TimeUnit.SECONDS);
        assertThat(connected.get(), is(false));
        assertThat(failed.get(), is(true));
    }

    @Test
    public void testOnFailure() throws Exception {
        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        tcpOperations.connect(new TcpConnectionHandlerBuilder<String>()
                .onConnect(c -> {
                    connected.set(true);
                    c.send(MessageBuilder.withPayload("test\n").build());
                })
                .onFailure(e -> {
                    failed.set(true);
                    latch.countDown();
                })
                .build());

        try (Socket socket = server.accept()) {
            Writer out = new OutputStreamWriter(socket.getOutputStream());
            out.close(); // close forcibly
        }

        latch.await(1, TimeUnit.SECONDS);
        assertThat(connected.get(), is(true));
        assertThat(failed.get(), is(true));
    }

    @Test
    public void testOnClosed() throws Exception {
        AtomicBoolean closed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        tcpOperations.connect(new TcpConnectionHandlerBuilder<String>()
                .onClosed(() -> {
                    closed.set(true);
                    latch.countDown();
                })
                .build());
        tcpOperations.shutdown().get(); // wait
        latch.await(1, TimeUnit.SECONDS);
        assertThat(closed.get(), is(true));
    }
}