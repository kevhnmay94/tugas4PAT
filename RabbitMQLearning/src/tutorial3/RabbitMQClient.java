/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tutorial3;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adwisatya
 */
public class RabbitMQClient {
    private static final String EXCHANGE_LOGS = "logs";
    private static final String EXCHANGE_USERS = "users";
    private static final String EXCHANGE_NAME = "logs2";
    
    public static Connection connection = null;
    public static Channel channel = null;
    
    /* authenticatin to server */
    private static String authUsername = "adwisatya";
    private static String authPassword = "patpat123";
    private static String authHost = "sg.bangsatya.com";
    
    /* attribute of methods */
    ArrayList<String> channelList =  new ArrayList<>();
    ArrayList<String> userList =  new ArrayList<>();
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();
      
    public RabbitMQClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(authHost);
        factory.setUsername(authUsername);
        factory.setPassword(authPassword);

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        
        Consumer consumer = new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope,
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
          }
        };
        channel.basicConsume(queueName, true, consumer);
    }
    public static void publishMessage(String message) throws UnsupportedEncodingException, IOException, TimeoutException{

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        String messages = "bismillah";

        channel.basicPublish(EXCHANGE_NAME, "", null, messages.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + messages + "'");

    }
    public static void main(String[] argv) throws Exception {
        RabbitMQClient rabbitMQClient = new RabbitMQClient();
        System.out.println("Welcome to simple IRC");
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));        
        String input = null;
        String [] splitted;
        String result = null;
        
        input = console.readLine();
        while(!input.equalsIgnoreCase("/EXIT")){
            rabbitMQClient.publishMessage(input);
            input = console.readLine();
        }
        
    }

}
