package uk.org.toot.midi.seqng;

import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

import static uk.org.toot.midi.message.MetaMsg.*;

/**
 * MidiPlayer plays MIDI from MidiSources in real-time.
 * It is the real-time part of a 'sequencer'.
 * It cannot support controller chasing, repositioning while running, or looping.
 * It cannot easily support mute/solo because the List of EventSources may be dynamic.
 * These operations must be provided by individual MidiSource implementations
 * as appropriate.
 * @author st
 *
 */
public class MidiPlayer extends MidiRenderer
{
	private boolean running = false;
	private long refTick = 0L;
	private long refMillis;
	private float ticksPerMilli;
	private PlayEngine playEngine;
	private boolean stopOnEmpty = true;

	@Override
	public void setMidiSource(MidiSource source) {
		if ( running ) {
			throw new IllegalStateException("Can't set MidiSource while playing");
		}
		super.setMidiSource(source);
		setBpm(120);
		refTick = 0L;
	}
	
	/**
	 * Start playing
	 */
	public void play() {
		if ( source == null ) {
			throw new IllegalStateException("MidiSource is null");
		}
		if ( running ) return;
		running = true;
		playEngine = new PlayEngine();
		notifyObservers();
	}
	
	/**
	 * Commence stopping.
	 */
	public void stop() {
		if ( !running ) return;
		playEngine.stop();
	}
	
	/**
	 * As if setMidiSource() had been called again.
	 */
	public void returnToZero() {
		// to avoid synchronisation issues
		if ( running ) {
			throw new IllegalStateException("Can't returnToZero while playing");
		}
		source.returnToZero();
		setBpm(120);
		refTick = 0L;
	}
	
	/**
	 * Return whether we're currently playing.
	 * Note that observers are notified on transitions.
	 * @return true if playing (or stopping), false if stopped
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Allow the default stop on empty to be changed in the unlikely event playing
	 * should proceed even when there is nothing to play at any point in the future.
	 * @param soe
	 */
	public void setStopOnEmpty(boolean soe) {
		stopOnEmpty = soe;
	}
	
	// to be called when pumping has stopped
	protected void stopped() {
		for ( MidiSource.EventSource src : eventSources() ) {
			if ( src instanceof MidiTarget.MessageTarget ) {
				((MidiTarget.MessageTarget)src).notesOff(true);
			}			
		}
		running = false;
		notifyObservers();
	}
	
	@Override
	protected List<MidiSource.EventSource> eventSources() {
		return source.getEventSources();
	}
	
	@Override
	protected void transport(MidiMessage msg, MidiSource.EventSource src, int idx) {
		if ( src instanceof MidiTarget.MessageTarget ) {
			((MidiTarget.MessageTarget)src).transport(msg);
		}
	}

	@Override
	protected void check(MidiEvent event) {
		MidiMessage msg = event.getMessage();
		if ( isMeta(msg) ) {
			if ( getType(msg) == TEMPO ) {
				setBpm(getTempo(msg));
				refTick = event.getTick();
				refMillis = getCurrentTimeMillis();
			}
		}
	}
	
	protected void setBpm(float bpm) {
		ticksPerMilli = source.getResolution() * bpm / 60000;
	}

	protected long getCurrentTimeMillis() {
		return System.nanoTime() / 1000000L;
	}

	// only valid when running
	protected long getCurrentTimeTicks() {
		return (long)(refTick + ticksPerMilli * (getCurrentTimeMillis() - refMillis));
	}
	
	/**
	 * to be called when pumping
	 * @return true if peek() on all MidiSource.Events sources returne null, false otherwise.
	 */ 
	protected boolean pump() {
		return pump(getCurrentTimeTicks());
	}
	
	/**
	 * PlayEngine encapsulates the real-time thread to avoid run() being public in MidiPlayer.
	 * @author st
	 *
	 */
	private class PlayEngine implements Runnable 
	{
		private Thread thread;

		PlayEngine() {
			// nearly MAX_PRIORITY
			int priority = Thread.NORM_PRIORITY
			+ ((Thread.MAX_PRIORITY - Thread.NORM_PRIORITY) * 3) / 4;
			thread = new Thread(this);
			thread.setName("Toot MidiPlayer - "+source.getName());
			thread.setPriority(priority);
			thread.start();
		}
		
		public void stop() {
			thread = null;			
		}
		
		public void run() {
			refMillis = getCurrentTimeMillis();
			Thread thisThread = Thread.currentThread();
			boolean complete = false;
			while ( (thread == thisThread) && !complete ) {
				complete = pump() && stopOnEmpty;

				try {
					Thread.sleep(1);
				} catch (InterruptedException ie) {
					// ignore
				}
			}
			// recalculate refTick, compensating for sleep(1)
			// getCurrentTimeTicks() will now be correct on next play, when refMillis is reset
			refTick = (long)(getCurrentTimeTicks() - ticksPerMilli);
			stopped(); // turns off active notes, resets some controllers
		}
	}
}
