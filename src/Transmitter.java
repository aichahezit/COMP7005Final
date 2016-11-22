import java.io.*;
import java.net.*;

public class Transmitter {
	
    public static void main(String args[])
    {
        DatagramSocket sock = null;
        int port = 7777;
        String s;
        Boolean dataToSend = true;
         
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
         
        try
        {
            sock = new DatagramSocket();
  
            InetAddress host = InetAddress.getByName("localhost");
            
            //take input and send the packet
            echo("Are you sending? (y/n): ");
            s = (String)cin.readLine();
            
            if(s.equals("y")){
            	
            	int sequenceTracker = 0;
            	
                //while there's still shit to send
                while(dataToSend)
                {           
                	echo("Message to send: ");
                    String message = (String)cin.readLine();
                    
                    if(message.equals("EOT")){
                    	dataToSend = false;
                    }
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
                    ObjectOutput oos = new ObjectOutputStream(baos);
                    
                    Packet packet = new Packet(0, sequenceTracker + message.length(), message.length(), message, 5, 0);
                    sequenceTracker += message.length();
                   
                    oos.flush();
                    oos.writeObject(packet);
                    oos.flush();
                    byte[] dataObject = baos.toByteArray();

                    DatagramPacket dp = new DatagramPacket(dataObject, dataObject.length, host, port);
                    
                    // Send the packet
                    sock.send(dp);
                    oos.close();
                     
                    //now receive ACK
                    byte[] buffer = new byte[65536];
                  	DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    sock.receive(reply);
                    
                    ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                    ObjectInputStream is = new ObjectInputStream(in);
                    
                    Packet ACKpacket = null;
                    
                    try {
    					ACKpacket = (Packet)is.readObject();
    				} catch (ClassNotFoundException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
                    
                    echo(	"Packet Received\n==========\nPacket Type: " + ACKpacket.PacketType + 
                    		"\nSeqNum: " + ACKpacket.SeqNum +
                    		"\nPayloadLen: " + ACKpacket.PayloadLen +
                    		"\nData: " + ACKpacket.data +
                    		"\nWindowSize: " + ACKpacket.WindowSize +
                    		"\nAckNum: " + ACKpacket.AckNum);
                }
            }
            else if(s.equals("n")){
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
