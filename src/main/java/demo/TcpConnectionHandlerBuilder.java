package demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.tcp.TcpConnection;
import org.springframework.messaging.tcp.TcpConnectionHandler;

import java.util.function.Consumer;

@Slf4j
public class TcpConnectionHandlerBuilder<T> {
    private Consumer<TcpConnection<T>> onConnect;
    private Consumer<Message<T>> onMessage;
    private Consumer<Throwable> onConnectFailure;
    private Consumer<Throwable> onFailure;
    private Runnable onClosed;

    public TcpConnectionHandlerBuilder<T> onConnect(
            Consumer<TcpConnection<T>> onConnected) {
        this.onConnect = onConnected;
        return this;
    }

    public TcpConnectionHandlerBuilder<T> onMessage(Consumer<Message<T>> onMessage) {
        this.onMessage = onMessage;
        return this;
    }

    public TcpConnectionHandlerBuilder<T> onConnectFailure(Consumer<Throwable> onConnectFailure) {
        this.onConnectFailure = onConnectFailure;
        return this;
    }

    public TcpConnectionHandlerBuilder<T> onFailure(Consumer<Throwable> onFailure) {
        this.onFailure = onFailure;
        return this;
    }

    public TcpConnectionHandlerBuilder<T> onClosed(Runnable onClosed) {
        this.onClosed = onClosed;
        return this;
    }

    public TcpConnectionHandler<T> build() {
        return new TcpConnectionHandler<T>() {
            @Override
            public void afterConnected(TcpConnection<T> connection) {
                log.trace("afterConnected {}", connection);
                if (TcpConnectionHandlerBuilder.this.onConnect != null) {
                    TcpConnectionHandlerBuilder.this.onConnect.accept(connection);
                }
            }

            @Override
            public void afterConnectFailure(Throwable ex) {
                log.trace("afterConnectFailure", ex);
                if (TcpConnectionHandlerBuilder.this.onConnectFailure != null) {
                    TcpConnectionHandlerBuilder.this.onConnectFailure.accept(ex);
                }
            }

            @Override
            public void handleMessage(Message<T> message) {
                log.trace("handleMessage {}", message);
                if (TcpConnectionHandlerBuilder.this.onMessage != null) {
                    TcpConnectionHandlerBuilder.this.onMessage.accept(message);
                }
            }

            @Override
            public void handleFailure(Throwable ex) {
                log.trace("handleFailure", ex);
                if (TcpConnectionHandlerBuilder.this.onFailure != null) {
                    TcpConnectionHandlerBuilder.this.onFailure.accept(ex);
                }
            }

            @Override
            public void afterConnectionClosed() {
                log.trace("afterConnectionClosed");
                if (TcpConnectionHandlerBuilder.this.onClosed != null) {
                    TcpConnectionHandlerBuilder.this.onClosed.run();
                }
            }
        };
    }
}
