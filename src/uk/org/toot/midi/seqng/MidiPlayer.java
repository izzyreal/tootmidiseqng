package uk.org.toot.midi.seqng;

import java.util.List;

import javax.sound.midi.MidiMessage;

/**
 * MidiPlayer plays MIDI from MidiSources in real-time.
 * It is the real-time part of a 'sequencer'.
 * @author st
 *
 */
public class MidiPlayer extends MidiRenderer
{

	public void start() {
		// start iterating MidiSource
	}
	
	public void stop() {
		// stop iterating MidiSource
		// notesOff on MidiTarget.MessageTargets
	}
	
	protected List<MidiSource.EventSource> eventSources() {
		return source.getEventSources();
	}
	
	protected void transport(MidiMessage msg, MidiSource.EventSource src, int idx) {
		if ( src instanceof MidiTarget.MessageTarget ) {
			((MidiTarget.MessageTarget)src).transport(msg);
		}
	}
	
}
