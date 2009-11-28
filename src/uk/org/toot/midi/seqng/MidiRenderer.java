package uk.org.toot.midi.seqng;

import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * This class provides functionality concerned with rendering MidiSources.
 * @author st
 *
 */
public abstract class MidiRenderer
{
	protected MidiSource source;

	public void setMidiSource(MidiSource source) {
		// TODO fail if running
		this.source = source;
	}
	
	/**
	 * Pump MidiMessages as they become due.
	 * @param targetTick the tick to pump until.
	 */
	protected void pump(long targetTick) {
		MidiEvent evt;
		int srcIdx = 0;
		for ( MidiSource.EventSource src : eventSources() ) {
			evt = src.peek();
			if ( evt == null ) continue;
			while ( evt.getTick() <= targetTick ) {
				transport(evt.getMessage(), src, srcIdx);
				if ( srcIdx == 0 ) check(evt);
				src.next();
				evt = src.peek();
			}
			srcIdx += 1;
		}		
	}
	
	/**
	 * Provide the List of EventSources.
	 * This may be dynamic, different per call, or static, nevery changing, depending
	 * on the particular subclass.
	 * @return the List of EventSources
	 */
	protected abstract List<MidiSource.EventSource> eventSources();
	
	/**
	 * Transport a MidiMessage from an EventSource at index i in the List
	 * @param msg the MidiMessage to transport
	 * @param src the EventSource which is the source of the MidiMessage
	 * @param i the index of the EventSource within its List
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
}
