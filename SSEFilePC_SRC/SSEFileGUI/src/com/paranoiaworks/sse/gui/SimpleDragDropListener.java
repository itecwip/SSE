package com.paranoiaworks.sse.gui;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

/**
 * SimpleDragDropListener Template
 * 
 * @author Paranoia Works
 * @version 1.0.0
 */ 

public abstract class SimpleDragDropListener implements DropTargetListener {
	
	public abstract void drop(DropTargetDropEvent event);

    public void dragEnter(DropTargetDragEvent event) {
    }

    public void dragExit(DropTargetEvent event) {
    }

    public void dragOver(DropTargetDragEvent event) {
    }

    public void dropActionChanged(DropTargetDragEvent event) {
    }
}
