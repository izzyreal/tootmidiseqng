package uk.org.toot.midi.seqng;

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
		// notesOff on MidiTarget
	}
}
