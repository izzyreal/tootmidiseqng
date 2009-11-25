package uk.org.toot.midi.seqng;

/**
 * This class provides functionality concerned with using MidiSources.
 * @author st
 *
 */
public class MidiEngine
{

	/**
	 * A NoteOnCache is used for each MidiMessage destination in order that
	 * active note ONs may be turned OFF on a stop condition.
	 * @author st
	 *
	 */
	public class NoteOnCache
	{
		private int[] cache = new int[128]; // bit-mask of notes that are currently on

		/**
		 * Call for note ON with velocity > 0
		 * @param note the pitch to set
		 * @param ch the channel
		 */
		public void set(int note, int ch) {
			cache[note] |= 1 << ch;
		}
		
		/**
		 * Call for note OFF and note ON with velocity == 0
		 * @param note the pitch to set
		 * @param ch the channel
		 */
		public void clear(int note, int ch) {
			cache[note] &= (0xFFFF ^ (1 << ch));	
		}
		
		/**
		 * Return true if pitch is active on channel, and clear relevant bit if
		 * so, otherwise return false.
		 * 
		 * @param i the pitch to test
		 * @param ch the channel to test
		 * @return true if pitch is active on channel
		 */
		public boolean testAndClear(int i, int ch) {
			int channelMask = (1 << ch);
			if ((cache[i] & channelMask) != 0) {
				cache[i] ^= channelMask;
				return true;
			}
			return false;
		}
		
		/**
		 * Clear the entire cache
		 *
		 */
		public void clear() {
			for ( int i = 0; i < 128; i++ ) {
				cache[i] = 0;
			}
		}
	}
}
