/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tutorial1;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author adwisatya
 */
public class Send {
    private final static String QUEUE_NAME = "pattugas4";

    public static void main(String[] argv) throws java.io.IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("sg.bangsatya.com");
        factory.setUsername("adwisatya");
        factory.setPassword("patpat123");
        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (TimeoutException ex) {
            Logger.getLogger(Send.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Hello World! ketiga";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + message + "'");

        try {
            channel.close();
        } catch (TimeoutException ex) {
            Logger.getLogger(Send.class.getName()).log(Level.SEVERE, null, ex);
        }
        connection.close();
    } 
}
