import java.io.*;
import java.net.*;

public class SingleReceiver {
	
	static final int 	DATA_PACKET = 0;
	static final int 	ACK_PACKET 	= 1;
	static final int	EOT_PACKET 	= 2;
	static final int 	WINDOW_SIZE = 1;
	
    public static void main(String args[])
    {
        DatagramSocket sock = null;
        boolean dataToSend = true;
        
        //will fail if first is error
        Packet packet = null;
         
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
            while(dataToSend)
            {
            	sock.setSoTimeout(2000);
                
            	try{
            	
            	sock.receive(incoming);
                
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                ObjectInputStream is = new ObjectInputStream(in);
                                
                try {
					packet = (Packet)is.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	            echo("\nPacket Received\n==========");
                
                if(packet.PacketType == 0){
                	echo("Packet Type: DATA");
                }else if(packet.PacketType == 1){
                	echo("Packet Type: ACK");
                }else if(packet.PacketType == 2){
                	echo("Packet Type: EOT");
                }
                
                echo(   "SeqNum: " + packet.SeqNum +
                		"\nPayloadLen: " + packet.PayloadLen +
                		"\nData: " + packet.data +
                		"\nWindowSize: " + packet.WindowSize +
                		"\nAckNum: " + packet.AckNum);
	            
	            if(packet.PacketType == EOT_PACKET){
	            	dataToSend = false;
	            }
                
            	}catch(SocketTimeoutException e){
              		echo("\n!!! TIMOUT !!!\nResending ACK...");
            	}
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
                ObjectOutput oos = new ObjectOutputStream(baos);
                
                Packet ACKpacket = null;
                
	            String message = "ACK packet";
	            ACKpacket = new Packet(ACK_PACKET, 0 + message.length(), message.length(), message, WINDOW_SIZE, packet.SeqNum);
                
                oos.flush();
                oos.writeObject(ACKpacket);

                byte[] dataObject = baos.toByteArray();

                DatagramPacket dp = new DatagramPacket(dataObject, dataObject.length, incoming.getAddress(), incoming.getPort());                                
	            
	            echo("\nPacket Sent\n==========");
                
                if(ACKpacket.PacketType == 0){
                	echo("Packet Type: DATA");
                }else if(ACKpacket.PacketType == 1){
                	echo("Packet Type: ACK");
                }else if(ACKpacket.PacketType == 2){
                	echo("Packet Type: EOT");
                }
                
                echo(   "SeqNum: " + ACKpacket.SeqNum +
                		"\nPayloadLen: " + ACKpacket.PayloadLen +
                		"\nData: " + ACKpacket.data +
                		"\nWindowSize: " + ACKpacket.WindowSize +
                		"\nAckNum: " + ACKpacket.AckNum);
                
                // Send the packet
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
