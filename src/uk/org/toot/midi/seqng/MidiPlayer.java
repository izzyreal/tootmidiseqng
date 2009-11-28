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
	private float bpm;
	private long refTick = 0L;
	private long refMillis;
	private int resolution;
	private float ticksPerMilli;

	public void start() {
		refMillis = getCurrentTimeMillis();
		resolution = source.getResolution();
		setBpm(120);
	}
	
	public void stop() {
		// stop pumping
	}
	
	// to be called when pumping has stopped
	protected void stopped() {
		for ( MidiSource.EventSource src : eventSources() ) {
			if ( src instanceof MidiTarget.MessageTarget ) {
				((MidiTarget.MessageTarget)src).notesOff(true);
			}			
		}
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
	
	protected void pump() {
		pump(getCurrentTimeTicks());
	}
}
