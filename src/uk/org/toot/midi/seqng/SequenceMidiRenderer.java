package uk.org.toot.midi.seqng;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;

import uk.org.toot.midi.seqng.MidiSource.EventSource;

/**
 * MidiPlayer write MIDI from MidiSources to a Standard MIDI File, type 1.
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
	
	public Sequence render() throws InvalidMidiDataException {
		sequence = new Sequence(Sequence.PPQ, source.getResolution());
		for ( int i = 0; i < eventSources.size(); i++ ) {
			sequence.createTrack();
		}
		// TODO call pump with increasing ticks UNTIL EOF !!!
		return sequence;
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
