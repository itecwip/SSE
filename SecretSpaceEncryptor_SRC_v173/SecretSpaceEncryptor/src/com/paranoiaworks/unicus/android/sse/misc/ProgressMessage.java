package com.paranoiaworks.unicus.android.sse.misc;

/**
 * Helper object for communication between ProgressBar and executor Thread
 * Keeps progress of background operation
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.3
 */
public class ProgressMessage {

	long fullSize, progressAbs = -1, secondaryProgressAbs = -1;
	long fullSizeB, progressAbsB = -1, secondaryProgressAbsB = -1;
	int lastRel = -1, lastRelB = -1;

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

	public long getSecondaryProgressAbs() {
		return secondaryProgressAbs;
	}
	
	public long getProgressAbs() {
		return progressAbs;
	}

	public void setProgressAbs(long progressAbs) {
		this.progressAbs = progressAbs;
	}
	
	public void setSecondaryProgressAbs(long secondaryProgressAbs) {
		this.secondaryProgressAbs = secondaryProgressAbs;
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
	
	public int getSecondaryProgressRel() {
		if (this.fullSize < 0 || this.secondaryProgressAbs < 0) return 0;
		int tempP = (int)Math.ceil(((double)secondaryProgressAbs / fullSize) * 100);
		if (tempP > 100) tempP = 100;
		return tempP;
	}
	
	public void set100B()
	{
		setProgressAbsB(fullSizeB);
	}
	
	public long getFullSizeB() {
		return fullSizeB;
	}

	public void setFullSizeB(long fullSize) {
		this.fullSizeB = fullSize;
	}

	public long getSecondaryProgressAbsB() {
		return secondaryProgressAbsB;
	}
	
	public long getProgressAbsB() {
		return progressAbsB;
	}

	public void setProgressAbsB(long progressAbs) {
		this.progressAbsB = progressAbs;
	}
	
	public void setSecondaryProgressAbsB(long secondaryProgressAbs) {
		this.secondaryProgressAbsB = secondaryProgressAbs;
	}
	
	public boolean isRelSameAsLastB() {
		int last = lastRelB;
		int current = getProgressRelB(false);
		return last == current;
	}
	
	public int getProgressRelB() {
		return getProgressRelB(true);
	}
	
	private int getProgressRelB(boolean storeLastValue) {
		if (this.fullSizeB < 0 || this.progressAbsB < 0) return 0;
		int tempP = (int)Math.ceil(((double)progressAbsB / fullSizeB) * 100);
		if (tempP > 100) tempP = 100;
		if(storeLastValue) lastRelB = tempP;
		return tempP;
	}
	
	public int getSecondaryProgressRelB() {
		if (this.fullSizeB < 0 || this.secondaryProgressAbsB < 0) return 0;
		int tempP = (int)Math.ceil(((double)secondaryProgressAbsB / fullSizeB) * 100);
		if (tempP > 100) tempP = 100;
		return tempP;
	}
}
