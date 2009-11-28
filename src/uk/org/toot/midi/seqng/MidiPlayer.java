package uk.org.toot.midi.seqng;

import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

import uk.org.toot.midi.message.MetaMsg;

/**
 * MidiPlayer plays MIDI from MidiSources in real-time.
 * It is the real-time part of a 'sequencer'.
 * @author st
 *
 */
public class MidiPlayer extends MidiRenderer
{
	private boolean running = false;
	private float bpm;
	private long refTick = 0L;
	private long refMillis;
	private int resolution;
	private float ticksPerMilli;
	private PlayEngine playEngine;

	@Override
	public void setMidiSource(MidiSource source) {
		if ( running ) {
			throw new IllegalStateException("Can't set MidiSource while playing");
		}
		super.setMidiSource(source);
	}
	
	synchronized public void play() {
		if ( source == null ) {
			throw new IllegalStateException("MidiSource is null");
		}
		if ( running ) return;
		refMillis = getCurrentTimeMillis();
		resolution = source.getResolution();
		setBpm(120);
		running = true;
		playEngine = new PlayEngine();
	}
	
	synchronized public void stop() {
		if ( !running ) return;
		playEngine.stop();
	}
	
	// to be called when pumping has stopped
	protected void stopped() {
		for ( MidiSource.EventSource src : eventSources() ) {
			if ( src instanceof MidiTarget.MessageTarget ) {
				((MidiTarget.MessageTarget)src).notesOff(true);
			}			
		}
		running = false;
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
		if ( MetaMsg.isMeta(msg) ) {
			if ( MetaMsg.getType(msg) == MetaMsg.TEMPO ) {
				// set bpm, refTick, refMillis
				setBpm(MetaMsg.getTempo(msg));
				refTick = event.getTick();
				refMillis = getCurrentTimeMillis();
			}
		}
	}
	
	protected void setBpm(float aBpm) {
		bpm = aBpm;
		ticksPerMilli = resolution * bpm / 60000;
	}

	protected long getCurrentTimeMillis() {
		return System.nanoTime() / 1000000L;
	}

	protected long getCurrentTimeTicks() {
		return (long)(refTick + ticksPerMilli * (getCurrentTimeMillis() - refMillis));
	}
	
	// to be called when pumping
	protected void pump() {
		pump(getCurrentTimeTicks());
	}
	
	/**
	 * PlayEngine encapsulates the real-time thread to avoid run() being public in MidiPlayer.
	 * @author st
	 *
	 */
	class PlayEngine implements Runnable {
		private Thread thread;

		PlayEngine() {
			// nearly MAX_PRIORITY
			int priority = Thread.NORM_PRIORITY
			+ ((Thread.MAX_PRIORITY - Thread.NORM_PRIORITY) * 3) / 4;
			thread = new Thread(this);
			thread.setName("Toot MidiPlayer");
			thread.setDaemon(false);
			thread.setPriority(priority);
			thread.start();
		}
		
		public void stop() {
			thread = null;			
		}
		
		public void run() {
			Thread thisThread = Thread.currentThread();
			while (thread == thisThread) {
				pump();

				try {
					Thread.sleep(1);
				} catch (InterruptedException ie) {
					// ignore
				}
			}
			stopped();
		}
	}
}
