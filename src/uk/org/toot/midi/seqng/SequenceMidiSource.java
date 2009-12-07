package uk.org.toot.midi.seqng;

import static uk.org.toot.midi.message.MetaMsg.getString;
import static uk.org.toot.midi.message.MetaMsg.getType;
import static uk.org.toot.midi.message.MetaMsg.isMeta;
import static uk.org.toot.midi.message.MetaMsg.TRACK_NAME;

import java.util.Collections;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
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
	private Sequence sequence;
	
	private List<SequenceEventSource> eventSources = 
		new java.util.ArrayList<SequenceEventSource>();
	
	public SequenceMidiSource(Sequence sequence) {
		this.sequence = sequence;
		Track[] tracks = sequence.getTracks();
		for ( int i = 0; i < tracks.length; i++ ) {
			eventSources.add(new SequenceEventSource(i, tracks[i]));
		}
	}
	
	public Sequence getSequence() { return sequence; }
	
	@Override
	public List<EventSource> getEventSources() {
		return Collections.<EventSource>unmodifiableList(eventSources);
	}
	
	@Override
	public int getResolution() {
		return sequence.getResolution();
	}
	
	@Override
	public String getName() {
		// TODO scan the first track of the Sequence to determine its name
		return "sequence";
	}
	
	/**
	 * Should only be called by the client.
	 */
	public void returnToZero() {
		for ( MidiSource.EventSource src : eventSources ) {
			((SequenceEventSource)src).returnToZero();
		}
	}
	
	protected class SequenceEventSource implements EventSource
	{
		private Track track;
		private String name;
		private int index = 0;
		
		public SequenceEventSource(int trk, Track track) {
			this.track = track;
			String aname = getMetaName(TRACK_NAME);
			name = aname == null ? "Player: Track "+(1+trk) : "Player: "+aname;
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
		
		public String getName() {
			return name;
		}
		
	    protected String getMetaName(int type) {
	        MidiEvent event = getFirstMetaEvent(type);
	        if ( event == null ) return null;
	        MidiMessage msg = event.getMessage();
	        if ( isMeta(msg) ) {
	            return getString(msg);
	        }
	        return null;
	    }

	    protected MidiEvent getFirstMetaEvent(int type) {
	        for ( int i = 0; i < track.size() - 1; i++ ) {
	            MidiEvent event = track.get(i);
	            MidiMessage msg = event.getMessage();
	            if ( isMeta(msg) ) {
	                if (getType(msg) == type) {
	                    return event;
	                }
	            }
	        }
	        return null;
	    }
	}
}
