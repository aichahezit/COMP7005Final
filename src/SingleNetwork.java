import java.io.*;
import java.net.*;
import java.util.Random;

/* TODO:
 * 
 * 
 * */

public class SingleNetwork {
	
	public static void main(String args[])
    {
        DatagramSocket sock = null;
    	String receiverIP = "";
    	String transmitterIP = "";
    	int receiverPort = 0, transmitterPort = 0;
    	InetAddress recvAddr = null, transAddr = null;
    	
    	int totalPacketsSent = 0;
    	boolean firstPacket = true;
    	int errors = 0;
    	double BER = 0;
    	int avgDelay = 0;
    	
    	try {
    		
			File file = new File("config.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			//read transmitter config info
			
			//get rid of network line
			String line = bufferedReader.readLine();
						
			//transmitter line
			//line = bufferedReader.readLine();
						
			String[] splitted = line.split("-");
			
			transmitterIP = splitted[1];
			transmitterPort = Integer.parseInt(splitted[2]);
			
			echo("Transmitter IP: " + splitted[1] + " Port: " + splitted[2]);
			
//			//read receiver config info
//			line = bufferedReader.readLine();
//						
//			splitted = line.split("-");
			receiverIP = splitted[3];
			receiverPort = Integer.parseInt(splitted[4]);
			
			echo("Receiver IP: " + splitted[3] + " Port: " + splitted[4]);
			
			fileReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	//create IP address objects from config file info
		try {
			recvAddr = InetAddress.getByName(receiverIP);
			transAddr = InetAddress.getByName(transmitterIP);

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        echo("Enter BER rate (0-1): ");
        try {
			BER = Double.parseDouble((String)cin.readLine());
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        try
        {
            //1. creating a server socket, parameter is local port number
            sock = new DatagramSocket(7777);
             
            //buffer to receive incoming data
            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
             
            //2. Wait for an incoming data
            echo("Network socket created. Waiting for incoming data...");
            
            double currentBER = 0;
//            
//            File log = new File("networkLog.txt");
//            FileWriter fw = new FileWriter(log.getAbsoluteFile());
//    		BufferedWriter bw = new BufferedWriter(fw);
             
            //communication loop
            while(true)
            {
                sock.receive(incoming);
                
                byte[] data = incoming.getData();
                
                echo("\nTransmission Round\n========================");
//                bw.write("\nTransmission Round\n========================");
                
                try{
                	//create input stream to receive from Host 1
	                ByteArrayInputStream in = new ByteArrayInputStream(data);
	                ObjectInputStream is = new ObjectInputStream(in);
	
	                Packet packet = (Packet)is.readObject();
	                	                	                
	                try{
	                	currentBER = (double)errors/(double)totalPacketsSent;
		                echo("Total Packets Sent: " + totalPacketsSent + " Errors: " + errors + " Current BER: " + currentBER);
//		                bw.write("Total Packets Sent: " + totalPacketsSent + " Errors: " + errors + " Current BER: " + currentBER);
	                }catch(Exception e){}
	                
	                
	                boolean dropPacket = false;
	                
	                if(firstPacket == false){
		                if(currentBER != BER){
		                	//random number generator based on percentage?
		                	double percentage = BER - currentBER;
		                	Random rn = new Random();
		                	int randomNum = rn.nextInt(100);
		                	
		                	if(randomNum <= (percentage * 100)){
		                		//drop packet
		                		dropPacket = true;	
		                	}
		                }
		                
		                //send to receiver   
		                if(dropPacket == true){
		                	echo("\n!!! Dropped a data packet. !!!\n");
//		                	bw.write("\n!!! Dropped a data packet. !!!\n");
		                	errors++;
		                	continue;
		                }
	                }
	                
	                firstPacket = false;
	                
	                //create output stream to send to Host 2
	                ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
	                ObjectOutput oos = new ObjectOutputStream(baos);
	                
	                oos.flush();
	                oos.writeObject(packet);
	                oos.flush();
	                byte[] dataObject = baos.toByteArray();
	
	                DatagramPacket outgoing = new DatagramPacket(dataObject, dataObject.length, recvAddr, receiverPort);
	                
	                String s = new String(data, 0, incoming.getLength());
	                transAddr = incoming.getAddress();
	
	                echo("Sending data packet from TRANSMITTER to RECEIVER");
//	                bw.write("Sending data packet from TRANSMITTER to RECEIVER");
	                
                	sock.send(outgoing);
	                oos.close();
	                is.close();
	                
	                totalPacketsSent++;
	                
	                //!!!!!!!!!//
	                
	                dropPacket = false;
	                
	                if(currentBER != BER){
	                	//random number generator based on percentage?
	                	double percentage = BER - currentBER;
	                	Random rn = new Random();
	                	int randomNum = rn.nextInt(100);
	                	
	                	if(randomNum <= (percentage * 100)){
	                		//drop packet
	                		dropPacket = true;	
	                	}
	                }
	                
	                //send to receiver   
	                if(dropPacket == true){
	                	echo("\n!!! Dropped an ACK. !!!\n");
//	                	bw.write("\n!!! Dropped an ACK. !!!\n");
	                	errors++;
	                	continue;
	                }
	                
	                echo("Receiving ACK from RECEIVER");
//	                bw.write("Receiving ACK from RECEIVER");
	                sock.receive(outgoing);
	                data = outgoing.getData();
	                
	                //create input stream to receive from Host 2
	                ByteArrayInputStream in2 = new ByteArrayInputStream(data);
	                ObjectInputStream is2 = new ObjectInputStream(in2);
	
	                Packet ACKpacket = (Packet)is2.readObject();
	                
	                //create output stream to send to Host 2
	                ByteArrayOutputStream baos2 = new ByteArrayOutputStream(5000);
	                ObjectOutput oos2 = new ObjectOutputStream(baos2);
	                
	                oos2.writeObject(ACKpacket);
	                
	                byte[] ACKdataObject = baos2.toByteArray();
	
	                DatagramPacket outgoingACK = new DatagramPacket(ACKdataObject, ACKdataObject.length, incoming.getAddress() , incoming.getPort());
		                
	                //send to receiver         
	                sock.send(outgoingACK);
	                oos2.close();
	                is2.close();
	                
	                echo("Sending ACK to TRANSMITTER.");
//	                bw.write("Sending ACK to TRANSMITTER.");
	                
//	                bw.close();
                
                }catch(Exception e){
                	e.printStackTrace();
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
