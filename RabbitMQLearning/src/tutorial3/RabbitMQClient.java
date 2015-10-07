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
    private static final String EXCHANGE_CHANNELS = "channelspat";
    
    public static Connection connection;
    public static Channel channel;
    
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

        channel.queueDeclare(EXCHANGE_CHANNELS, false, false, false, null);
        System.out.println(" [*] Client ready!");
        
        channel.basicQos(1);
        
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
              String message = new String(body, "UTF-8");
              System.out.println(" [x] Received '" + message + "'");
              try {
                //doWork(message);
              } finally {
                System.out.println(" [x] Done");
                channel.basicAck(envelope.getDeliveryTag(), false);
              }
            }
        };
        channel.basicConsume(EXCHANGE_CHANNELS, false, consumer);
    }
    public void publishMessage(String message) throws UnsupportedEncodingException, IOException{
        channel.basicPublish("", EXCHANGE_CHANNELS,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes("UTF-8"));
    }
    public static void main(String[] argv) throws Exception {
        RabbitMQClient rabbitMQClient = new RabbitMQClient();
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
