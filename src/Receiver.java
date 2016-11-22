import java.io.*;
import java.net.*;

public class Receiver {
    public static void main(String args[])
    {
        DatagramSocket sock = null;
         
        try
        {
            //1. creating a server socket, parameter is local port number
            sock = new DatagramSocket(7778);
             
            //buffer to receive incoming data
            byte[] buffer = new byte[65536];
            
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            
            //2. Wait for an incoming data
            echo("Server socket created. Waiting for incoming data...");
            
            //sock.receive(incoming);	
            
//            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
//            ObjectInputStream is = new ObjectInputStream(in);
             
            //communication loop
            while(true)
            {
                sock.receive(incoming);	
                
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                ObjectInputStream is = new ObjectInputStream(in);
                
                Packet packet = null;
                
                try {
					packet = (Packet)is.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                echo(	"Packet Received\n==========\nPacket Type: " + packet.PacketType + 
                		"\nSeqNum: " + packet.SeqNum +
                		"\nPayloadLen: " + packet.PayloadLen +
                		"\nData: " + packet.data +
                		"\nWindowSize: " + packet.WindowSize +
                		"\nAckNum: " + packet.AckNum);
                
                byte[] data = incoming.getData();
                String s = new String(data, 0, incoming.getLength());
                 
                //echo the details of incoming data - client ip : client port - client message
                echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
                 
                s = "ACK" + s;
                DatagramPacket dp = new DatagramPacket(s.getBytes() , s.getBytes().length , incoming.getAddress() , incoming.getPort());
                sock.send(dp);	
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
