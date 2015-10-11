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
    private static final String EXCHANGE_USER_NAME = "rabbitusers";
    private static final String EXCHANGE_CHANNEL_NAME = "rabbitchannels";
    
    public static Connection connection = null;
    public static Channel channel = null;
    
    public static String nickname = "";
    public static String channelString = "";
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
            switch (message.substring(0,1)){
                case "+":
                    userList.add(message.substring(1));
                    System.out.println("Add "+message.substring(1)+" to list");
                    break;
                case "-":
                    userList.remove(userList.indexOf(message.substring(1)));
                    System.out.println("Remove "+message.substring(1)+" from list");
                    break;
            }
            System.out.println("[x] New user : '" + message.substring(1) + "'");
          }
        };
        channel.basicConsume(queueUser, true, userConsumer);

    }
    public String get_auth_host(){
        return authHost;
    }
    public String generate_nickname(){
        int len = 7;
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ ){
           sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        }
        return sb.toString();
    }
    public static void publish_message(String _channel,String messages) throws UnsupportedEncodingException, IOException, TimeoutException{
        
        channel.basicPublish(EXCHANGE_CHANNEL_NAME, _channel, null, ("["+_channel+"]"+"["+nickname+"]"+messages).getBytes("UTF-8"));
    }
    public static void create_nickname(String _nickname) throws UnsupportedEncodingException, IOException, TimeoutException{
        nickname = _nickname;
        //channel.exchangeDeclare(EXCHANGE_USER_NAME, "fanout");
        channel.basicPublish(EXCHANGE_USER_NAME, "", null, ("+"+_nickname).getBytes("UTF-8"));
    }
    public static void exit() throws IOException{
        channel.basicPublish(EXCHANGE_USER_NAME, "", null, ("-"+nickname).getBytes("UTF-8"));
    }
    public void join_channel(String _channel) throws IOException{
        try{
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
            channelList.add(_channel);
        }finally{
            System.out.println("You have joined "+_channel);
        }
    }
    public void leave_channel(String _channel) throws IOException, IOException{
        try{
            queueChannel = channel.queueDeclare().getQueue();
            channel.queueUnbind(queueChannel, EXCHANGE_CHANNEL_NAME, _channel);
            channelList.remove(channelList.indexOf(_channel));
        }finally{
            System.out.println("You are leaving from "+_channel);
            channelString = "";
        }
    }
    public void exit_me() throws IOException{
        for(int i=0;i<channelList.size();i++){
            leave_channel(channelList.get(i));
            System.out.println("You are leaving "+channelList.get(i));
        }
    }
    public static void main(String[] argv) throws Exception {
        RabbitMQClient rabbitMQClient = new RabbitMQClient();
        System.out.println("Welcome to simple IRC server @"+rabbitMQClient.get_auth_host());
        System.out.println("/nick [nickname]");
        System.out.println("/join [channel_name]");
        System.out.println("/leave [channel_name]");
        System.out.println("@[channel_name] [message]");
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
                    if(!rabbitMQClient.userList.contains(splitted[1])){
                        rabbitMQClient.create_nickname(splitted[1]);    
                        System.out.println("Your nickname is "+splitted[1]);
                    }else{
                        String finalnickname = rabbitMQClient.generate_nickname();
                        rabbitMQClient.create_nickname(finalnickname);
                        System.out.println("Your nickname is "+finalnickname);
                    }
                    break;
                case "/join":
                    if(!rabbitMQClient.nickname.isEmpty() && !splitted[1].isEmpty()){
                        if(rabbitMQClient.channelList.contains(splitted[1])){
                            System.out.println("You are already join the channel.");
                        }else{
                            System.out.println("You want to join to: "+splitted[1]);
                            rabbitMQClient.join_channel(splitted[1]);        
                        }
                        
                    }else{
                        System.out.println("Please use nickname first!.");
                    }
                    break;
                case "/leave":
                    if(rabbitMQClient.channelString.isEmpty() || rabbitMQClient.nickname.isEmpty() || splitted[1].isEmpty()){
                        System.out.println("Please use the right format!");
                    }else{
                        if(rabbitMQClient.channelList.contains(splitted[1])){
                            rabbitMQClient.leave_channel(splitted[1]);
                        }else{
                            System.out.println("You aren't member of "+splitted[1]);
                        }
                    }
                    break;
                default:
                    if(rabbitMQClient.channelList.isEmpty() || rabbitMQClient.nickname.isEmpty()){
                        System.out.println("Please user nickname or join channel first!");
                    }else{
                        if(input.substring(0,1).contains("@")){
                            String channel = splitted[0].substring(1);
                            if(rabbitMQClient.channelList.contains(channel)){
                            rabbitMQClient.publish_message(channel,input.substring(input.indexOf(splitted[0])));
                            }else{
                                System.out.println("You aren't member of "+channel);
                            }
                        }else{
                            System.out.println("Please you the right format");
                        }
                    }
                    break;
            }
            input = console.readLine();
        }  
        rabbitMQClient.exit_me();
        
    }
}
