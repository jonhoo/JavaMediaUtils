package javax.media.utils.loaders.sound;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class represents a sound stored in the MIDI format.
 * It provides methods for playing, pausing, seeking and resuming
 * the clip in accordance with the SoundHolder interface.
 * 
 * Note that since a single sequencer is used for all clips,
 * only a single clip can be playing at the same time.
 */
public class MidiSoundHolder extends BaseSoundHolder implements MetaEventListener, SoundHolder {
    /**
     * MIDI meta-event constant used to signal the end of a track
     */
    private static final int END_OF_TRACK = 47;

    private Sequence sequence = null;
    private MidiLoader loader; // passed in from MidisLoader

    public MidiSoundHolder ( MidiLoader s ) {
        this.loader = s;
    }

    @Override
    public void loadFile ( File soundFile ) throws UnsupportedAudioFileException, IOException {
        try {
            this.sequence = MidiSystem.getSequence ( soundFile );
        } catch ( InvalidMidiDataException e ) {
            throw new UnsupportedAudioFileException ( e.getMessage ( ) );
        }
    }

    @Override
    public void play ( ) throws InvalidAudioDataException {
        if ( this.sequence == null )
            return;

        Sequencer sequencer = this.loader.requestPlay ( this );
        try {
            sequencer.setSequence ( this.sequence );
            sequencer.setTickPosition ( 0 );
            this.state = State.PLAYING;
            sequencer.start ( );
        } catch ( InvalidMidiDataException e ) {
            throw new InvalidAudioDataException ( e.getMessage ( ) );
        }
    }

    @Override
    public void stop ( ) {
        if ( this.sequence == null )
            return;

        Sequencer sequencer = this.loader.requestSequencer ( this );

        if ( sequencer != null ) {
            this.state = State.STOPPED_MANUALLY;
            if ( sequencer.isRunning ( ) )
                sequencer.stop ( );
        }

        this.loader.notifyStoppedPlaying ( this );
    }

    @Override
    public void pause ( ) {
        if ( this.sequence == null )
            return;
        Sequencer sequencer = this.loader.requestSequencer ( this );

        if ( sequencer != null ) {
            this.state = State.PAUSED;
            if ( sequencer.isRunning ( ) )
                sequencer.stop ( );
        }
    }

    @Override
    public void seek ( long position ) {
        if ( this.sequence == null )
            return;
        Sequencer sequencer = this.loader.requestSequencer ( this );
        if ( sequencer != null )
            sequencer.setMicrosecondPosition ( position );
    }

    @Override
    public void resume ( ) {
        if ( this.sequence == null )
            return;
        Sequencer sequencer = this.loader.requestSequencer ( this );
        if ( sequencer != null ) {
            this.state = State.PLAYING;
            if ( !sequencer.isRunning ( ) )
                sequencer.start ( );
        }
    }

    @Override
    public void meta ( MetaMessage meta ) {
        if ( this.sequence == null )
            return;

        // All manual stops should not trigger onFinish
        if ( this.state != State.PLAYING )
            return;

        if ( meta.getType ( ) == END_OF_TRACK ) {
            if ( this.isLooping ) {
                this.onLoop ( );
                this.seek ( 0 );
                this.resume ( );
            } else {
                this.state = State.FINISHED;
                this.onFinish ( );
            }
        }
    }
}