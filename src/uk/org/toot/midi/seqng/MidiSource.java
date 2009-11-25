package uk.org.toot.midi.seqng;

import java.util.List;
import java.util.Observable;

import javax.sound.midi.MidiEvent;

/**
 * A MidiSource is a composite MidiEvent iterator.
 * This is the contract required by a Player/Renderer to be able to use arbitrary track
 * based representations of music. Such representations maybe be edited whilst being played. 
 * In general the representation need not be known a priori, it could even be generated 
 * in real-time.
 * An implementation of this class can be properly decoupled from the underlying
 * representation. The implementation of this class should know about its underlying
 * representation, that representation should not know about this implementation.
 * Clients of this class need not know about any such representation or any specific
 * implementation of this class.
 * @author st
 *
 */
public abstract class MidiSource extends Observable
{
	public abstract List<TrackSource> getTrackSources();
	
	/**
	 * An iterator of MidiEvents.
	 * We don't implement hasNext() because in general there is no iterator
	 * termination, another MidiEvent may be created at ant time. Also, if there
	 * is a next MidiEvent we want to know what it is in order to examine its tick.
	 * @author st
	 *
	 */
	public interface TrackSource
	{
		/**
		 * Return the next MidiEvent without changing iterator position.
		 * @return the next MidiEvent or null
		 */
		public MidiEvent peek();
		
		/**
		 * Return the next MidiEvent and increment iterator position.
		 * @return the next MidiEvent or null
		 */
		public MidiEvent next();
	}
}
