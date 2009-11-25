package uk.org.toot.midi.seqng;

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
	private List<TrackSource> trackSources = new java.util.ArrayList<TrackSource>();
	
	public SequenceMidiSource(Sequence sequence) {
		Track[] tracks = sequence.getTracks();
		for ( int i = 0; i < tracks.length; i++ ) {
			trackSources.add(new SequenceTrackSource(tracks[i]));
		}
	}
	
	@Override
	public List<TrackSource> getTrackSources() {
		return trackSources;
	}
	
	protected class SequenceTrackSource implements TrackSource
	{
		private Track track;
		private int index = 0;
		
		public SequenceTrackSource(Track track) {
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
	}
}
