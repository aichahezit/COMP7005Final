import java.io.*;
import java.net.*;

public class Transmitter {
	
    public static void main(String args[])
    {
        DatagramSocket sock = null;
        int port = 7777;
        String s;
         
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
         
        try
        {
            sock = new DatagramSocket();
             
            InetAddress host = InetAddress.getByName("localhost");
             
            ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
            ObjectOutput oos = new ObjectOutputStream(baos);
            
            //while there's still shit to send
            while(true)
            {           	
                //take input and send the packet
                echo("Enter message to send : ");
                s = (String)cin.readLine();
                 
                String message = "This is a test message.";
                Packet packet = new Packet(0, 0 + message.length(), message.length(), message, 5, 0);
               
                oos.flush();
                oos.writeObject(packet);
                oos.flush();
                byte[] dataObject = baos.toByteArray();

                DatagramPacket dp = new DatagramPacket(dataObject, dataObject.length, host, port);
                
                // Send the packet
                sock.send(dp);
                oos.close();
                 
                //now receive reply
                //buffer to receive incoming data
                byte[] buffer = new byte[65536];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                sock.receive(reply);
                 
                byte[] data = reply.getData();
                s = new String(data, 0, reply.getLength());
                 
                //echo the details of incoming data - client ip : client port - client message
                echo(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + s);
            }
        }
         
        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
    }
     
    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
    }
}
