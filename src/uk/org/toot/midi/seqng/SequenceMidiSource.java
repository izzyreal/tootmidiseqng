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
	private List<RenderSource> trackSources = new java.util.ArrayList<RenderSource>();
	
	public SequenceMidiSource(Sequence sequence) {
		Track[] tracks = sequence.getTracks();
		for ( int i = 0; i < tracks.length; i++ ) {
			trackSources.add(new SequenceRenderSource(tracks[i]));
		}
	}
	
	@Override
	public List<RenderSource> getRenderSources() {
		return trackSources;
	}
	
	protected class SequenceRenderSource implements RenderSource
	{
		private Track track;
		private int index = 0;
		
		public SequenceRenderSource(Track track) {
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
