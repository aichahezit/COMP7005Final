import java.io.*;
import java.net.*;

/* TODO:
 * 
 * 
 * */

public class Network {
	
	public static void main(String args[])
    {
        DatagramSocket sock = null;
    	int BER;
    	int avgDelay;
    	String receiverIP = "127.0.0.1";
    	String transmitterIP = "127.0.0.1";
    	int receiverPort = 7778, transmitterPort = 0;
    	InetAddress recvAddr = null, transAddr = null;
    	
//    	try {
//    		
//			File file = new File("config.txt");
//			FileReader fileReader = new FileReader(file);
//			BufferedReader bufferedReader = new BufferedReader(fileReader);
//			
//			//read transmitter config info
//			String line = bufferedReader.readLine();
//			
//			String[] splitted = line.split("\\s+");
//			transmitterIP = splitted[0];
//			echo(splitted[0]);
//			transmitterPort = Integer.parseInt(splitted[1]);
//			
//			//read receiver config info
//			line = bufferedReader.readLine();
//			
//			splitted = line.split("\\s+");
//			receiverIP = splitted[0];
//			receiverPort = Integer.parseInt(splitted[1]);
//			
//			fileReader.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    	
    	//create IP address objects from config file info
		try {
			recvAddr = InetAddress.getByName(receiverIP);
			transAddr = InetAddress.getByName(transmitterIP);

		} catch (UnknownHostException e1) {
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
            echo("Server socket created. Waiting for incoming data...");
             
            //communication loop
            while(true)
            {
                sock.receive(incoming);
                
                byte[] data = incoming.getData();
                
                try{
                	//create input stream to receive from Host 1
	                ByteArrayInputStream in = new ByteArrayInputStream(data);
	                ObjectInputStream is = new ObjectInputStream(in);
	
	                Packet packet = (Packet)is.readObject();
	                
	                echo(packet.data);
	                
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
	
	                echo("sending from transmitter to receiver");
	                
	                //send to receiver         
	                sock.send(outgoing);
	                oos.close();
	                is.close();
	                
	                //!!!!!!!!!//
	                
	                echo("receiving ACK from receiver");
	                sock.receive(outgoing);
	                data = outgoing.getData();
	                
	                //create input stream to receive from Host 2
	                ByteArrayInputStream in2 = new ByteArrayInputStream(data);
	                ObjectInputStream is2 = new ObjectInputStream(in2);
	
	                Packet ACKpacket = (Packet)is2.readObject();
	                echo(ACKpacket.data);
	                
	                //create output stream to send to Host 2
	                ByteArrayOutputStream baos2 = new ByteArrayOutputStream(5000);
	                ObjectOutput oos2 = new ObjectOutputStream(baos2);
	                
	                oos2.flush();
	                oos2.writeObject(ACKpacket);
	                oos2.flush();
	                byte[] ACKdataObject = baos2.toByteArray();
	
	                DatagramPacket outgoingACK = new DatagramPacket(ACKdataObject, ACKdataObject.length, incoming.getAddress() , incoming.getPort());
		                
	                //send to receiver         
	                sock.send(outgoingACK);
	                oos2.close();
	                is2.close();
	                echo("sending ACK to transmitter");
                
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
