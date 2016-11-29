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
            
            File log = new File("receiverLog.txt");
            FileWriter fw = new FileWriter(log.getAbsoluteFile());
    		BufferedWriter bw = new BufferedWriter(fw);
            
            //2. Wait for an incoming data
            echo("Server socket created. Waiting for incoming data...");
            bw.write("Server socket created. Waiting for incoming data...\n");
             
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
	            bw.write("\nPacket Received\n==========");
                
                if(packet.PacketType == 0){
                	echo("Packet Type: DATA");
                	bw.write("\nPacket Type: DATA");
                }else if(packet.PacketType == 1){
                	echo("Packet Type: ACK");
                	bw.write("\nPacket Type: ACK");
                }else if(packet.PacketType == 2){
                	echo("Packet Type: EOT");
                	bw.write("\nPacket Type: EOT");
                }
                
                echo(   "SeqNum: " + packet.SeqNum +
                		"\nPayloadLen: " + packet.PayloadLen +
                		"\nData: " + packet.data +
                		"\nWindowSize: " + packet.WindowSize +
                		"\nAckNum: " + packet.AckNum);
                
                bw.write("\nSeqNum: " + packet.SeqNum +
                		"\nPayloadLen: " + packet.PayloadLen +
                		"\nData: " + packet.data +
                		"\nWindowSize: " + packet.WindowSize +
                		"\nAckNum: " + packet.AckNum + "\n");
	            
	            if(packet.PacketType == EOT_PACKET){
	            	dataToSend = false;
	            }
                
            	}catch(SocketTimeoutException e){
              		echo("\n!!! TIMOUT !!!\nResending ACK...");
              		bw.write("\n!!! TIMOUT !!!\nResending ACK...");
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
	            bw.write("\nPacket Sent\n==========\n");
                
                if(ACKpacket.PacketType == 0){
                	echo("Packet Type: DATA");
                	bw.write("Packet Type: DATA\n");
                }else if(ACKpacket.PacketType == 1){
                	echo("Packet Type: ACK");
                	bw.write("Packet Type: ACK\n");
                }else if(ACKpacket.PacketType == 2){
                	echo("Packet Type: EOT");
                	bw.write("Packet Type: EOT\n");
                }
                
                echo(   "SeqNum: " + ACKpacket.SeqNum +
                		"\nPayloadLen: " + ACKpacket.PayloadLen +
                		"\nData: " + ACKpacket.data +
                		"\nWindowSize: " + ACKpacket.WindowSize +
                		"\nAckNum: " + ACKpacket.AckNum);
                
                bw.write("SeqNum: " + ACKpacket.SeqNum +
                		"\nPayloadLen: " + ACKpacket.PayloadLen +
                		"\nData: " + ACKpacket.data +
                		"\nWindowSize: " + ACKpacket.WindowSize +
                		"\nAckNum: " + ACKpacket.AckNum);
                
                // Send the packet
                sock.send(dp);
            }
            bw.close();
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
