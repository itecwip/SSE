package com.paranoiaworks.sse;

/**
 * Helper object for communication between ProgressBar and executor Thread
 * Keeps progress of background operation
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.1
 */
public class ProgressMessage {

	long fullSize, progressAbs = -1;
	int lastRel = -1;

	public void set100()
	{
		setProgressAbs(fullSize);
	}
	
	public long getFullSize() {
		return fullSize;
	}

	public void setFullSize(long fullSize) {
		this.fullSize = fullSize;
	}

	public long getProgressAbs() {
		return progressAbs;
	}

	public void setProgressAbs(long progressAbs) {
		this.progressAbs = progressAbs;
	}
	
	public boolean isRelSameAsLast() {
		int last = lastRel;
		int current = getProgressRel(false);
		return last == current;
	}
	
	public int getProgressRel() {
		return getProgressRel(true);
	}
	
	private int getProgressRel(boolean storeLastValue) {
		if (this.fullSize < 0 || this.progressAbs < 0) return 0;
		int tempP = (int)Math.ceil(((double)progressAbs / fullSize) * 100);
		if (tempP > 100) tempP = 100;
		if(storeLastValue) lastRel = tempP;
		return tempP;
	}
}
