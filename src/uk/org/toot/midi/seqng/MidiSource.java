package uk.org.toot.midi.seqng;

import java.util.List;
import java.util.Observable;

import javax.sound.midi.MidiEvent;

/**
 * A MidiSource is a composite MidiEvent iterator. This is the contract required
 * by a Player/Renderer to be able to use arbitrary track based representations
 * of music. Such representations may be edited whilst being played. In general
 * the representation need not be known a priori, it could even be generated in
 * real-time. An implementation of this class can be properly decoupled from the
 * underlying representation. The implementation of this class should know about
 * its underlying representation, that representation should not know about this
 * implementation. Clients of this class need not know about any such
 * representation or any specific implementation of this class.
 * 
 * There are a few edge cases in iteration.
 * 
 * Assume we have notes X then Y and X has already been consumed such that
 * peek() and next() would both return Y. At some point peek() will return Y and
 * its tick will indicate that it is ready to play so we would proceed to obtain
 * it with next(). If inbetween peek() and next() the model is altered such that
 * note Z is inserted between X and Y there are two eventualities. next() will
 * now return Z. It may be too late to play in which case it should be ignored
 * but it may be that it should play in which case it will be played and peek()
 * and next() would subsequently obtain note Y which next() was originally
 * expecting. So in this case an apparent race condition with note insertion
 * near the current iterator position is handled properly.
 * 
 * Again, assume we have notes X then Y then Z and X has already been consumed such that
 * peek() and next() would both return Y. At some point peek() will return Y and
 * its tick will indicate that it is ready to play so we would proceed to obtain
 * it with next(). If inbetween peek() and next() the model is altered such that
 * Y is deleted next() will return Z. It may be that Z is too early to be played
 * in which case next() has consumed it too early and Z would not subsequently
 * be played at the appropriate time. In this case the race condition with note deletion
 * near the current iterator position is not handled properly and notes may be lost.
 * It should be noted that this race condition typically only occurs if a note is deleted
 * at exactly the millisecond it becomes playable, it is unlikely but possible.
 * 
 * Assume we have notes X then Y and X has already been consumed such that
 * peek() and next() would both return Y. Assume X is deleted. This may require
 * the iterator to reposition itself such that next() still returns Y.
 * 
 * In general an iterator should not maintain position by referencing the next note.
 * The next note may be varying as new notes are added, so it is arguably best practice to
 * maintain position by referencing the previous note.
 * 
 * @author st
 * 
 */
public abstract class MidiSource extends Observable
{
	public abstract List<EventSource> getEventSources();

	/**
	 * @return the name of this source 
	 */
	public abstract String getName();
	
	/**
	 * Should only be called by MidiPlayer, MidiPlayer will behave incorrectly if
	 * anything else calls it.
	 *
	 */
	public abstract void returnToZero();
	
	/**
	 * 
	 * @return the resolution in ticks per quarter note
	 */
	public abstract int getResolution();
	
	/**
	 * An iterator of MidiEvents.
	 * We don't implement hasNext() because in general there is no iterator
	 * termination, another MidiEvent may be created at ant time. Also, if there
	 * is a next MidiEvent we want to know what it is in order to examine its tick.
	 * @author st
	 *
	 */
	public interface EventSource
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
