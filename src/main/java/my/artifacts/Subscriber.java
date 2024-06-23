package my.artifacts;

import my.artifacts.abstractions.ISubscriber;
import my.artifacts.utils.GsonUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public abstract class Subscriber implements ISubscriber {

    private final MultiValuedMap<String, Consumer<Object>> handlers = new HashSetValuedHashMap<>();
    public Subscriber() {
        System.out.println("Registering handlers...");
        Arrays.stream(this.getClass().getMethods()).forEach( method -> {
            if (method.isAnnotationPresent(EventHandler.class)) {
                EventHandler annotation = method.getAnnotation(EventHandler.class);
                for (String event : annotation.events()) {
                    handlers.put(event, (message) -> {
                        try {
                            method.invoke(this, message);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    @RabbitHandler
    @Override
    final public void receive(String message) {
        try {
            Event event = GsonUtils.fromJson(message, Event.class);
            System.out.println(event);
            String eventName = event.name.toLowerCase();
            Collection<Consumer<Object>> consumers = handlers.get(eventName);
            consumers.forEach(consumer -> consumer.accept(message));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
