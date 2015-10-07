/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tutorial2;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
/**
 *
 * @author adwisatya
 */
public class Worker {
    private static final String TASK_QUEUE_NAME = "pattugas4";
    
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("sg.bangsatya.com");
        factory.setUsername("adwisatya");
        factory.setPassword("patpat123");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        
        channel.basicQos(1);
        
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
              String message = new String(body, "UTF-8");
              System.out.println(" [x] Received '" + message + "'");
              try {
                doWork(message);
              } finally {
                System.out.println(" [x] Done");
                channel.basicAck(envelope.getDeliveryTag(), false);
              }
            }
        };
        channel.basicConsume(TASK_QUEUE_NAME, false, consumer);
    }
    private static void doWork(String task) {
        for (char ch : task.toCharArray()) {
          if (ch == '.') {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException _ignored) {
              Thread.currentThread().interrupt();
            }
          }
        }
    }
}
