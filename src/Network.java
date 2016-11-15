import java.io.*;
import java.net.*;

public class Network {
	
//	public static void main(String args[]){
//		
//		DatagramSocket sock = null;
//		
//		//read in IP addresses and port numbers from file (transmitter and receiver)
//		
//		
//		//receive packet from transmitter
//		
//		//perform BER and delays
//		
//		//send packet to receiver
//		
//		//receive reply (ACK) from receiver
//		
//		//send to transmitter
//		
//		//wait for next packet
//	}
	public static void main(String args[])
    {
        DatagramSocket sock = null;
    	int BER;
    	int avgDelay;
    	String receiverIP = "127.0.0.1";
    	String transmitterIP = "127.0.0.1";
    	int receiverPort = 7778, transmitterPort = 0;
    	InetAddress recvAddr = null, transAddr = null;
    	
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
                String s = new String(data, 0, incoming.getLength());
                 
                //echo the details of incoming data - client ip : client port - client message
                //echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
                echo("sending from transmitter to receiver");
                
                //send to receiver
                DatagramPacket outgoing = new DatagramPacket(s.getBytes(), s.getBytes().length, recvAddr, receiverPort);
                sock.send(outgoing);
                
                //wait for reply
                sock.receive(outgoing);
                echo("receiving ACK from receiver");
                data = outgoing.getData();
                s = new String(data, 0, incoming.getLength());
                
                //sends reply to ip of the incoming packet, save this for ACK
                s = "OK : " + s;
                DatagramPacket dp = new DatagramPacket(s.getBytes() , s.getBytes().length , incoming.getAddress() , incoming.getPort());
                sock.send(dp);
                echo("sending ACK to transmitter");
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
