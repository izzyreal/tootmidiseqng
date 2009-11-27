package uk.org.toot.midi.seqng;

import java.util.Collections;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 * This class is an implementation of MidiSource backed by a Sequence.
 * This class is not robust in the face of edits to the Sequence.
 * @author st
 *
 */
public class SequenceMidiSource extends MidiSource
{
	private List<SequenceEventSource> eventSources = 
		new java.util.ArrayList<SequenceEventSource>();
	
	public SequenceMidiSource(Sequence sequence) {
		Track[] tracks = sequence.getTracks();
		for ( int i = 0; i < tracks.length; i++ ) {
			eventSources.add(new SequenceEventSource(tracks[i]));
		}
	}
	
	@Override
	public List<EventSource> getEventSources() {
		return Collections.<EventSource>unmodifiableList(eventSources);
	}
	
	@Override
	public void returnToZero() {
		for ( SequenceEventSource source : eventSources ) {
			source.returnToZero();
		}
	}
	
	protected class SequenceEventSource implements EventSource
	{
		private Track track;
		private int index = 0;
		
		public SequenceEventSource(Track track) {
			this.track = track;
		}
		
		public MidiEvent peek() {
			if ( index >= track.size() ) return null;
			return track.get(index);
		}

		public MidiEvent next() {
			if ( index >= track.size() ) return null;
			return track.get(index++);
		}
		
		public void returnToZero() {
			index = 0;
		}
	}
}
