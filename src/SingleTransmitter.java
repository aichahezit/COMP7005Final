import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/* TODO:
 * - EOF exception thrown when data is too short, EOT will not work
 * 	- could use packet type as EOT (2 = EOT), set datatosend as false when packet.type = 2
 * */

public class SingleTransmitter {
	
	static final int 	DATA_PACKET = 0;
	static final int 	ACK_PACKET 	= 1;
	static final int	EOT_PACKET 	= 2;
	static final int	WINDOW_SIZE = 1;
	
    public static void main(String args[])
    {
        DatagramSocket sock = null;
        int port = 7777;
        String s;
        Boolean dataToSend = true;
        String networkIP = "";
        int packetstoSend = 0;
        int packetsSent = 0;
         
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
         
    	try {
    		
			File file = new File("config.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			//read transmitter config info
			
			String line = bufferedReader.readLine();
						
			networkIP = line;
			
			echo(networkIP);
			
			fileReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
        
        try
        {
            sock = new DatagramSocket();
            InetAddress host = InetAddress.getByName(networkIP);
            
            //take input and send the packet
            echo("Are you sending? (y/n): ");
            s = (String)cin.readLine();
            
            if(s.equals("y")){
            	
            	int sequenceTracker = 0;
            	Packet savedPacket = null;
            	Random rn = new Random();
            	packetstoSend = rn.nextInt(100) + 1;
            	echo("\nPackets to send: " + packetstoSend + "\n");
            	
                //while there's still shit to send
                while(dataToSend){           
//	                	echo("Message to send: ");
//	                    String message = (String)cin.readLine();	  
                		String message = "Long message sending data with SEQ " + sequenceTracker;
	                    
	                    ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
	                    ObjectOutput oos = new ObjectOutputStream(baos);
	                    
	                    Packet packet = null;
	                    
	                    if(savedPacket == null){
	                    	if(packetsSent < packetstoSend){
	                    		echo("\nPackets sent:" + packetsSent);
	                    		packet = new Packet(DATA_PACKET, sequenceTracker + message.length(), message.length(), message, WINDOW_SIZE, 0);
	                    		sequenceTracker += message.length();
	                    	}else{
	                    		message = "This is the EOT Packet being sent.";
	                    		packet = new Packet(EOT_PACKET, sequenceTracker + message.length(), message.length(), message, WINDOW_SIZE, 0);
	                    		sequenceTracker += message.length();
	                    		echo("\n!!! EOT PACKET SENT !!!");
	                    		dataToSend = false;
	                    	}
	                    }else{
	                    	packet = savedPacket;
	                    	savedPacket = null;
	                    }
	                   
	                    oos.flush();
	                    oos.writeObject(packet);
	                    oos.flush();
	                    byte[] dataObject = baos.toByteArray();
	
	                    DatagramPacket dp = new DatagramPacket(dataObject, dataObject.length, host, port);
	                    
	                    //create timeout
	                    sock.setSoTimeout(3000);
	                    
	                    // Send the packet
	                    sock.send(dp);
	                    packetsSent++;
	                    
	                    echo("\nPacket Sent\n==========");
	                    
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
                    
	                    oos.close();
                            
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    
                    //now receive ACK
                    byte[] buffer = new byte[65536];
                  	DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    
                  	try{
                  		sock.receive(reply);
                  	}catch(SocketTimeoutException e){
                  		echo("\n!!! TIMOUT !!!\nResending packet...");
                  		savedPacket = packet;
                  		continue;
                  	}
                                        
                    ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                    ObjectInputStream is = new ObjectInputStream(in);
                    
                    Packet ACKpacket = null;
                    
                    try {
    					ACKpacket = (Packet)is.readObject();
    				} catch (ClassNotFoundException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
                           
                    echo("\nPacket Received\n==========");
                    
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

                }}else if(s.equals("n")){
            	echo("TODO: become receiver");
            	//do we even need to do this if we use a config file?
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
