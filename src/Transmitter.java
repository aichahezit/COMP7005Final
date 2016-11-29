import java.io.*;
import java.net.*;
import java.util.ArrayList;

/* TODO:
 * - EOF exception thrown when data is too short, EOT will not work
 * 	- could use packet type as EOT (2 = EOT), set datatosend as false when packet.type = 2
 * */

public class Transmitter {
	
	static final int 	DATA_PACKET = 0;
	static final int 	ACK_PACKET 	= 1;
	static final int	EOT_PACKET 	= 2;
	static final int 	WINDOW_SIZE = 5;
	
    public static void main(String args[])
    {
        DatagramSocket sock = null;
        int port = 7777;
        String s;
        Boolean dataToSend = true;
        int currentDup = 0;
        
        ArrayList<Packet> packetWindow = new ArrayList<Packet>();
        ArrayList<Packet> ACKList = new ArrayList<Packet>();
         
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
            	String message = "Long message please work you fucking bitch.";
            	
                //while there's still shit to send
                while(dataToSend)
                {           
                    //if array != to window size, keep sending
                	if(packetWindow.size() != WINDOW_SIZE){
//	                	echo("Message to send: ");
//	                    String message = (String)cin.readLine();	                   
	                    
	                    ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
	                    ObjectOutput oos = new ObjectOutputStream(baos);
	                    
	                    Packet packet = new Packet(currentDup, DATA_PACKET, sequenceTracker + message.length(), message.length(), message, WINDOW_SIZE, 0);
	                    sequenceTracker += message.length();
	                   
	                    oos.flush();
	                    oos.writeObject(packet);
	                    oos.flush();
	                    byte[] dataObject = baos.toByteArray();
	
	                    DatagramPacket dp = new DatagramPacket(dataObject, dataObject.length, host, port);
	                    
	                    //create timer
	                    
	                    // Send the packet
	                    sock.send(dp);
	                    
	                    //timer.start();
	                    
	                    //add to window list
	                    packetWindow.add(packet);
	                    	                    
	                    oos.close();
                	}
                     
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    
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
                    
                    ACKList.add(ACKpacket);
                    
                    echo(	"Packet Received\n==========\n" +
                    		"Duplicate Check: " + ACKpacket.DuplicateCheck +
                    		"\nPacket Type: " + ACKpacket.PacketType + 
                    		"\nSeqNum: " + ACKpacket.SeqNum +
                    		"\nPayloadLen: " + ACKpacket.PayloadLen +
                    		"\nData: " + ACKpacket.data +
                    		"\nWindowSize: " + ACKpacket.WindowSize +
                    		"\nAckNum: " + ACKpacket.AckNum);
                    
                    ArrayList<Packet> tempPackets = new ArrayList<Packet>();
                    ArrayList<Packet> tempACKs = new ArrayList<Packet>();
                    
                    for(Packet pack : packetWindow)
                    {
                        tempPackets.add(pack);
                    }
                    
                    for(Packet pack : ACKList)
                    {
                        tempACKs.add(pack);
                    }
                    
                    for(Packet waitingPacket : tempPackets){
                    	for(Packet recvACK : tempACKs){
                    		if(waitingPacket.DuplicateCheck == recvACK.DuplicateCheck
                    				&& waitingPacket.SeqNum == recvACK.AckNum){
                    			packetWindow.remove(waitingPacket);
                    			ACKList.remove(recvACK);
                    			//timer.stop();
                    		}else{
                    			//here should handle.... something?
                    			echo("NO MATCH");
                    			//maybe add to not matched list?
                    		}
                    	}
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
