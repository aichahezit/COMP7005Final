import java.io.*;
import java.net.*;

/* TODO:
 * - EOF exception thrown when data is too short, EOT will not work
 * 
 * */

public class Transmitter {
	
    public static void main(String args[])
    {
        DatagramSocket sock = null;
        int port = 7777;
        String s;
        Boolean dataToSend = true;
        int currentDup = 0;
         
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
                    
                    //this won't work
                    if(message.equals("EOT")){
                    	dataToSend = false;
                    }
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
                    ObjectOutput oos = new ObjectOutputStream(baos);
                    
                    Packet packet = new Packet(currentDup, 0, sequenceTracker + message.length(), message.length(), message, 5, 0);
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
                    
                    if(ACKpacket.DuplicateCheck == currentDup && ACKpacket.AckNum == sequenceTracker){
                    echo(	"Packet Received\n==========\n" +
                    		"Duplicate Check: " + ACKpacket.DuplicateCheck +
                    		"\nPacket Type: " + ACKpacket.PacketType + 
                    		"\nSeqNum: " + ACKpacket.SeqNum +
                    		"\nPayloadLen: " + ACKpacket.PayloadLen +
                    		"\nData: " + ACKpacket.data +
                    		"\nWindowSize: " + ACKpacket.WindowSize +
                    		"\nAckNum: " + ACKpacket.AckNum);
                    }else{
                    	echo("Wrong Duplicate Check Number. Expected: " + currentDup + " Received: " + ACKpacket.DuplicateCheck);
                    	echo("\n OR wrong ACK num. Expected: " + sequenceTracker + " Received: " + ACKpacket.AckNum);
                    }
                    
                  //handles number change for duplicate handling
                    if(currentDup == 0){
                    	currentDup = 1;
                    }else{
                    	currentDup = 0;
                    }
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
