package javax.media.utils.loaders.sound;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Loads an audio clip through the JavaSound API
 * All input streams are converted to PCM_SIGNED for output
 * Note that audio clips should be longer than 1 second due
 * to this bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5085008
 */
public class ClipSoundHolder extends BaseSoundHolder implements SoundHolder, LineListener {

    private Clip clip;

    @Override
    public void loadFile ( File soundFile ) throws UnsupportedAudioFileException, IOException {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream ( soundFile );
            AudioFormat format = stream.getFormat ( );

            // Make sure output is PCM
            AudioFormat newFormat =
                    new AudioFormat ( AudioFormat.Encoding.PCM_SIGNED,
                            format.getSampleRate ( ),
                            format.getSampleSizeInBits ( ),
                            format.getChannels ( ),
                            format.getFrameSize ( ),
                            format.getFrameRate ( ),
                            false );
            // Convert input stream for output
            stream = AudioSystem.getAudioInputStream ( newFormat, stream );
            format = newFormat;

            DataLine.Info info = new DataLine.Info ( Clip.class, format );

            // If we can't play the file, throw an exception
            if ( !AudioSystem.isLineSupported ( info ) )
                throw new UnsupportedAudioFileException ( "Audio file cannot be converted to PCM for output" );

            // Get access to the clip
            this.clip = (Clip) AudioSystem.getLine ( info );
            this.clip.addLineListener ( this );
            this.clip.open ( stream );

            // Cleanup
            stream.close ( );
        } catch ( LineUnavailableException e ) {
            throw new IOException ( "No audio line available: " + e.getMessage ( ) );
        }
    }

    @Override
    public void play ( ) throws InvalidAudioDataException {
        if ( this.clip == null )
            return;
        this.state = State.PLAYING;
        this.seek ( 0 );
        this.clip.start ( );
    }

    @Override
    public void stop ( ) {
        if ( this.clip == null )
            return;
        this.state = State.STOPPED_MANUALLY;
        this.clip.stop ( );
    }

    @Override
    public void pause ( ) {
        if ( this.clip == null )
            return;
        this.state = State.PAUSED;
        this.clip.stop ( );
    }

    @Override
    public void resume ( ) {
        if ( this.clip == null )
            return;
        this.state = State.PLAYING;
        this.clip.start ( );
    }
    
    @Override
    public void seek ( long position ) {
        if ( this.clip == null )
            return;
        this.clip.setMicrosecondPosition ( position );
    }

    @Override
    public void update ( LineEvent event ) {
        if ( this.clip == null )
            return;

        // All manual stops should not trigger onFinish
        if ( this.state != State.PLAYING )
            return;

        if ( event.getType ( ) == LineEvent.Type.STOP ) {
            if ( this.isLooping ) {
                this.onLoop ( );
                this.clip.setFramePosition ( 0 );
                this.clip.start ( );
            } else {
                this.state = State.FINISHED;
                this.onFinish ( );
            }
        }
    }

}
