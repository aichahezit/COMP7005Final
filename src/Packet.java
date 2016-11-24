
public class Packet implements java.io.Serializable {
	public int DuplicateCheck;
	public int PacketType;
	public int SeqNum;
	public int PayloadLen;
	public String data;
	public int WindowSize;
	public int AckNum;
	
	public Packet(int DuplicateCheck, int PacketType, int SeqNum, int PayloadLen, String data, int WindowSize, int AckNum){
		this.DuplicateCheck = DuplicateCheck;
		this.PacketType = PacketType;
		this.SeqNum = SeqNum;
		this.PayloadLen = PayloadLen;
		this.data = data;
		this.WindowSize = WindowSize;
		this.AckNum = AckNum;
	}
}
