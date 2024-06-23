package my.artifacts;

import com.google.gson.Gson;
import my.artifacts.abstractions.IPublisher;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class Publisher implements IPublisher {

    @Autowired
    private RabbitTemplate template;

    protected final Queue queue;

    public Publisher(Queue queue) {
        this.queue = queue;
    }

    @Override
    public <TEvent extends Event> void send(TEvent event) {
        String message = new Gson().toJson(event);
        this.template.convertAndSend(queue.getName(), message);

        System.out.println(" [x] Sent '" + message + "'");
    }

    @Override
    public <TEvent extends Event> void send(TEvent event, Map<String, Object> headers) {
        String message = new Gson().toJson(event);
        this.template.convertAndSend(queue.getName(), message, msg -> {
            headers.forEach((k, v) -> msg.getMessageProperties().setHeader(k, v));
            return msg;
        });

        System.out.println(" [x] Sent '" + message + "'");
        System.out.println(" With headers");
        headers.forEach((k, v) -> System.out.println(k + ": " + v));
    }
}
