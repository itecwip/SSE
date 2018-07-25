package f5.helpers;

public class CoeffWrapper {
	
	private boolean[] bits;
	private boolean[] nulls;
	private int size = -1;
	
	public CoeffWrapper(boolean[] bits, boolean[] nulls, int size) {
		this.bits = new boolean[size];
		this.nulls = new boolean[size];
		System.arraycopy(bits, 0, this.bits, 0, size);
		System.arraycopy(nulls, 0, this.nulls, 0, size);
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getBit(int index) {
		if(nulls[index]) return -1;
		else return bits[index] ? 1 : 0;
	}
}
