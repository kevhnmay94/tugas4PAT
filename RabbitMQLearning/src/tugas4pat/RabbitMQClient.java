/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugas4pat;
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
import javax.print.attribute.standard.JobStateReason;

/**
 *
 * @author adwisatya
 */
public class RabbitMQClient {
    private static final String EXCHANGE_LOGS = "logs";
    private static final String EXCHANGE_USER_NAME = "usersrabbit";
    private static final String EXCHANGE_CHANNEL_NAME = "tugaspatrabbits";
    
    public static Connection connection = null;
    public static Channel channel = null;
    
    public static String nickname;
    public static String channelString;
    public String queueName;
    public String queueUser;
    public String queueChannel;
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
        channel.exchangeDeclare(EXCHANGE_CHANNEL_NAME, "direct");
        channel.exchangeDeclare(EXCHANGE_USER_NAME, "fanout");

        queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_CHANNEL_NAME, "");
        
        queueUser = channel.queueDeclare().getQueue();
        channel.queueBind(queueUser, EXCHANGE_USER_NAME, "");
        
        Consumer messageConsumer = new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope,
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            System.out.println("[x] Received '" + message + "'");
          }
        };
        channel.basicConsume(queueName, true, messageConsumer);
        
        Consumer userConsumer = new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope,
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            System.out.println("[x] New user: '" + message + "'");
          }
        };
        channel.basicConsume(queueUser, true, userConsumer);

    }
    public static void publish_message(String messages) throws UnsupportedEncodingException, IOException, TimeoutException{
        channel.basicPublish(EXCHANGE_CHANNEL_NAME, channelString, null, ("["+channelString+"]"+"["+nickname+"]"+messages).getBytes("UTF-8"));
    }
    public static void create_nickname(String _nickname) throws UnsupportedEncodingException, IOException, TimeoutException{
        nickname = _nickname;
        //channel.exchangeDeclare(EXCHANGE_USER_NAME, "fanout");
        channel.basicPublish(EXCHANGE_USER_NAME, "", null, (_nickname+" joining").getBytes("UTF-8"));
    }
    public void join_channel(String _channel) throws IOException{
        channelString = _channel;
        queueChannel = channel.queueDeclare().getQueue();
        channel.queueBind(queueChannel, EXCHANGE_CHANNEL_NAME, _channel);
        
        Consumer channelConsumer = new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope,
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            System.out.println(message);
          }
        };
        channel.basicConsume(queueChannel, true, channelConsumer);
    }
    public boolean isExist(String nickname){
        System.out.println(queueUser);
        return queueUser.contains(nickname);
        
    }
    public static void main(String[] argv) throws Exception {
        RabbitMQClient rabbitMQClient = new RabbitMQClient();
        System.out.println("Welcome to simple IRC");
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));        
        String input = null;
        String [] splitted;
        String result = null;
        
        System.out.print("Type your message:");
        input = console.readLine();
        while(!input.equalsIgnoreCase("/EXIT")){
            splitted =  input.split(" ");
            switch (splitted[0].toLowerCase()){
                case "/nick":
                    System.out.println("You want to set your nickname to: "+splitted[1]);
                    rabbitMQClient.create_nickname(splitted[1]);
                    break;
                case "/join":
                    System.out.println("You want to join to: "+splitted[1]);
                    rabbitMQClient.join_channel(splitted[1]);
                    break;
                default:
                    rabbitMQClient.publish_message(input);
                    break;
            }
            input = console.readLine();
        }
        
    }

}
