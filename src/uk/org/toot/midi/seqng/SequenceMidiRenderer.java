package uk.org.toot.midi.seqng;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;

import uk.org.toot.midi.seqng.MidiSource.EventSource;

/**
 * This class renders MIDI from a MidiSource to a Sequence.
 * @author st
 *
 */
public class SequenceMidiRenderer extends MidiRenderer
{
	private List<MidiSource.EventSource> eventSources;
	
	private Sequence sequence;
	
	private long currentTick = 0;
	
	public void setMidiSource(MidiSource source) {
		super.setMidiSource(source);
		eventSources = source.getEventSources();
	}
	
	/**
	 * Render the current MidiSource to a Sequence.
	 * @return the rendered Sequence
	 * @throws InvalidMidiDataException
	 */
	public Sequence render() throws InvalidMidiDataException {
		sequence = new Sequence(Sequence.PPQ, source.getResolution());
		for ( int i = 0; i < eventSources.size(); i++ ) {
			sequence.createTrack();
		}
		while ( (currentTick = findNextTick()) < Long.MAX_VALUE ) {
			pump(currentTick);
		}
		return sequence;
	}
	
	protected long findNextTick() {
		long earliestTick = Long.MAX_VALUE;
		long tick;
		MidiEvent evt;
		for ( MidiSource.EventSource src : eventSources ) {
			evt = src.peek();
			if ( evt == null ) continue;
			tick = evt.getTick();
			if ( tick < earliestTick ) {
				earliestTick = tick;
			}
		}
		return earliestTick;
	}
	
	@Override
	protected List<MidiSource.EventSource> eventSources() {
		return eventSources;
	}

	@Override
	protected void transport(MidiMessage msg, EventSource src, int i) {
		sequence.getTracks()[i].add(new MidiEvent(msg, currentTick));
	}
}
