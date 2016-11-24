import java.io.*;
import java.net.*;

public class Receiver {
    public static void main(String args[])
    {
        DatagramSocket sock = null;
        int expectedDup = 0;
         
        try
        {
            //1. creating a server socket, parameter is local port number
            sock = new DatagramSocket(7778);
             
            //buffer to receive incoming data
            byte[] buffer = new byte[65536];
            
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            
            //2. Wait for an incoming data
            echo("Server socket created. Waiting for incoming data...");
             
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
                
                //is.close();
                
                if(packet.DuplicateCheck == expectedDup){
	                echo(	"Packet Received\n==========\n" +
	                		"Duplicate Check: " + packet.DuplicateCheck + 
	                		"\nPacket Type: " + packet.PacketType + 
	                		"\nSeqNum: " + packet.SeqNum +
	                		"\nPayloadLen: " + packet.PayloadLen +
	                		"\nData: " + packet.data +
	                		"\nWindowSize: " + packet.WindowSize +
	                		"\nAckNum: " + packet.AckNum);
                }else{
                	echo("Wrong Duplicate Check Number. Expected: " + expectedDup + " Received: " + packet.DuplicateCheck);
                }
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
                ObjectOutput oos = new ObjectOutputStream(baos);
                
                String message = "ACK packet";
                Packet ACKpacket = new Packet(expectedDup, 1, 0 + message.length(), message.length(), message, 5, packet.SeqNum);
               
                oos.flush();
                oos.writeObject(ACKpacket);
                //oos.flush();
                byte[] dataObject = baos.toByteArray();

                DatagramPacket dp = new DatagramPacket(dataObject, dataObject.length, incoming.getAddress(), incoming.getPort());
                                
                // Send the packet
                sock.send(dp);
                
                //close input and output streams
                //oos.close();
                //is.close();
                
                //handles number change for duplicate handling
                if(expectedDup == 0){
                	expectedDup = 1;
                }else{
                	expectedDup = 0;
                }
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
