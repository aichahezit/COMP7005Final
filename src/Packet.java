
public class Packet {
	public int PacketType;
	public int SeqNum;
	public int PayloadLen;
	public String[] data = new String[PayloadLen];
	public int WindowSize;
	public int AckNum;
}
