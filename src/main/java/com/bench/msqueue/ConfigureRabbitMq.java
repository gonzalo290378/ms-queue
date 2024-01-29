//package com.bench.msqueue;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.core.TopicExchange;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ConfigureRabbitMq {
//
//    public static final String QUEUE_NAME = "Queue-1";
//    public static final String EXCHANGE_NAME = "myTopicExchange";
//    public static final String ROUTING_KEY = "myRoutingKey.#";
//
//    @Bean
//    Queue createQueue() {
//        return new Queue(QUEUE_NAME, false);
//    }
//
//    @Bean
//    TopicExchange exchange() {
//        return new TopicExchange(QUEUE_NAME);
//    }
//
//    @Bean
//    Binding binding(Queue queue, TopicExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
//    }
//
//    @Bean
//    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        return container;
//    }
//}
