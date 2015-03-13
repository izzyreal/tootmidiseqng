The JavaSound Sequencer implementation is a highly complex beast that still suffers major disadvantages with its tightly coupled Sequence.

We take the view that a 'sequence' need not be known a priori, that only the next MidiEvent is required at any instant, and that this naturally permits real-time editing of the underlying model or even real-time accompaniment.