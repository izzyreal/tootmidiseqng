// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.seqng;

import java.util.List;
import java.util.Observable;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * This class provides functionality concerned with rendering MidiSources.
 * @author st
 *
 */
public abstract class MidiRenderer extends Observable
{
	protected MidiSource source;

	public void setMidiSource(MidiSource source) {
		if ( source == null ) {
			throw new IllegalArgumentException("MidiSource can't be null");
		}
		this.source = source;
		source.returnToZero(); // just in case it isn't
	}
	
	/**
	 * Pump MidiMessages as they become due.
	 * @param targetTick the tick to pump until.
	 * @return true if every peek() returned null, false otherwise
	 */
	protected boolean pump(long targetTick) {
		MidiEvent evt;
		int srcIdx = 0;
		boolean empty = true;
		long offsetTick;
		for ( MidiSource.EventSource src : eventSources() ) {
			evt = src.peek();
			if ( evt == null ) continue;
			empty = false;
			offsetTick = targetTick - getTickOffset(src, srcIdx);
			while ( evt.getTick() <= offsetTick ) {
				transport(evt.getMessage(), src, srcIdx);
				if ( srcIdx == 0 ) check(evt);
				src.next();
				evt = src.peek();
				if ( evt == null ) break;
			}
			srcIdx += 1;
		}
		return empty;
	}
	
	/**
	 * Provide the List of EventSources.
	 * This may be dynamic, different per call, or static, never changing, depending
	 * on the particular subclass.
	 * @return the List of EventSources
	 */
	protected abstract List<MidiSource.EventSource> eventSources();
	
	/**
	 * Transport a MidiMessage from an EventSource at index i in the List
	 * @param msg the MidiMessage to transport
	 * @param src the EventSource which is the source of the MidiMessage
	 * @param i the index of the EventSource within the List
	 */
	protected abstract void transport(MidiMessage msg, MidiSource.EventSource src, int i);
	
	/**
	 * Check events from the first EventSource in the List.
	 * This is assumed to be the equivalent of the first Track in a MIDI file, and as such,
	 * is the only place where for example Tempo events may exist.
	 * @param event
	 */
	protected void check(MidiEvent event) {
		// default null implementation
	}
	
	/**
	 * Return the offset, in ticks, that the EventSource should be delayed/advanced by.
	 * Typically a 'sequencer' may need to delay messages sent to a hardware MIDI port
	 * in order that they are synchronized with messages sent to a softsynth, with
	 * its consequential audio latency.
	 * @param src the EventSource
	 * @param i the index of the EventSource within the List
	 * @return the offset in ticks, postive for delay, negative for advance.
	 */
	protected long getTickOffset(MidiSource.EventSource src, int i) {
		return 0L;
	}
}
