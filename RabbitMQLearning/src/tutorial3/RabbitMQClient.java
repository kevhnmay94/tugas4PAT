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
    private static final String EXCHANGE_CHANNELS = "channels";
    
    public final Connection connection;
    public final Channel channel;
    
    /* authenticatin to server */
    private static String authUsername = "adwisatya";
    private static String authPassword = "patpat123";
    private static String authHost = "sg.bangsatya.com";
    
    /* attribute of methods */
    ArrayList<String> channelList =  new ArrayList<>();
    ArrayList<String> userList =  new ArrayList<>();
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();
    
    public RabbitMQClient() throws IOException, TimeoutException{
        ConnectionFactory factory =  new ConnectionFactory();
        factory.setHost(authHost);
        factory.setUsername(authUsername);
        factory.setPassword(authPassword);
        connection = factory.newConnection();
        channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_LOGS, "fanout");
        channel.exchangeDeclare(EXCHANGE_USERS, "fanout");
        channel.exchangeDeclare(EXCHANGE_CHANNELS, "fanout");
        
        String logsQueue = channel.queueDeclare().getQueue();
        channel.queueBind(logsQueue, EXCHANGE_LOGS,"");
        String usersQueue = channel.queueDeclare().getQueue();
        channel.queueBind(usersQueue, EXCHANGE_USERS,"");


        Consumer logsConsumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
              String message = new String(body, "UTF-8");
              System.out.println(" [x] Received '" + message + "'");
            }        
        };
        //channel.queueDeclare(EXCHANGE_LOGS, true, false, false, null);
        channel.basicConsume(logsQueue, true, logsConsumer);
        
        
        Consumer usersConsumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
              String message = new String(body, "UTF-8");
              System.out.println(" [x] Received '" + message + "'");
            }        
        };
        //channel.queueDeclare(EXCHANGE_USERS, true, false, false, null);
        channel.basicConsume(usersQueue, true, usersConsumer);
        
        //channel.close();
        //connection.close();
    }
    
    
    public String randomString( int len ){
       StringBuilder sb = new StringBuilder( len );
       for( int i = 0; i < len; i++ ) 
          sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
       return sb.toString();
    }
    public int getChannel(String channelName){
        for(int i=0;i<channelList.size();i++){
            if(channelList.get(i).contentEquals(channelName)){
                return i;
            }
        }
        return -1;
    }
    public int getUser(String nickName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).contentEquals(nickName)){
                return i;
            }
        }
        return -1;
    }
    public void publishMessage(String message) throws UnsupportedEncodingException, IOException{
        channel.queueDeclare("pattugas4", true, false, false, null);
        channel.basicPublish("", "pattugas4", null, message.getBytes("UTF-8"));
    }
    
    public static void main(String[] args) throws IOException, TimeoutException {
        RabbitMQClient rabbitMQClient = new RabbitMQClient();
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));        
        String input = null;
        String [] splitted;
        System.out.println("Welcome to Simple IRC Client");
        input = console.readLine();

        while(!input.equalsIgnoreCase("/EXIT")){
            rabbitMQClient.publishMessage(input);
            input = console.readLine();
        }
    }

}
