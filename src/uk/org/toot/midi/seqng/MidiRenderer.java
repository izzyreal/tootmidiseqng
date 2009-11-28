package uk.org.toot.midi.seqng;

import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * This class provides functionality concerned with using MidiSources.
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
	
	protected void pump(long targetTick) {
		MidiEvent evt;
		int srcIdx = 0;
		for ( MidiSource.EventSource src : eventSources() ) {
			evt = src.peek();
			while ( evt.getTick() <= targetTick ) {
				transport(evt.getMessage(), src, srcIdx);
				src.next();
				evt = src.peek();
			}
			srcIdx += 1;
		}		
	}
	
	protected abstract List<MidiSource.EventSource> eventSources();
	
	protected abstract void transport(MidiMessage msg, MidiSource.EventSource src, int i);
}
