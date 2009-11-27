package uk.org.toot.midi.seqng;

/**
 * MidiTarget extends MidiSource to provide a 1:1 mapping between EventSources and
 * MessageTargets. This allows the source of MidiEvents to also control the destination
 * of the resulting MidiMessages.
 * @author st
 *
 */
public abstract class MidiTarget extends MidiSource
{
	public abstract class MessageTargetImpl extends AbstractMessageTarget
			implements EventSource
	{

	}
}
