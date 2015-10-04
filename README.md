# How to use `org.springframework.messaging.tcp.TcpOperations`

using `org.springframework.messaging.tcp.reactor.Reactor2TcpClient`

    $ ./mvnw package
    $ java -jar target/demo-tcp-0.0.1-SNAPSHOT.jar
    [main] INFO org.springframework.context.annotation.AnnotationConfigApplicationContext - Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@69f4a965: startup date [Sun Oct 04 10:46:40 JST 2015]; root of context hierarchy
    [main] INFO demo.HelloServer - listen 58360
    [main] INFO demo.DemoTcpApplication$AppConfig - start
    [main] INFO demo.DemoTcpApplication$AppConfig - end
    [reactor-tcp-io-4] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!3
    [reactor-tcp-io-3] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!2
    [reactor-tcp-io-1] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!0
    [reactor-tcp-io-2] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!1
    [reactor-tcp-io-4] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!7
    [reactor-tcp-io-3] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!6
    [reactor-tcp-io-1] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!4
    [reactor-tcp-io-2] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!5
    [reactor-tcp-io-1] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!8
    [reactor-tcp-io-2] INFO demo.DemoTcpApplication$AppConfig - Hi Hello!9
    [Thread-6] INFO org.springframework.context.annotation.AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@69f4a965: startup date [Sun Oct 04 10:46:40 JST 2015]; root of context hierarchy
    [Thread-6] INFO demo.HelloServer - stop...