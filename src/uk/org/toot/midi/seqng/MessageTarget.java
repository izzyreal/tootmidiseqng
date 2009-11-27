package uk.org.toot.midi.seqng;

import javax.sound.midi.MidiMessage;

public interface MessageTarget
{
	public void transport(MidiMessage msg);
	
	/**
	 * Called for stop or mute
	 * @param doControllers true for stop, false for mute
	 */
	public void notesOff(boolean doControllers);
}
