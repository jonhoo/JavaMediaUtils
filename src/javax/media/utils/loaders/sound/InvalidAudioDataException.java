package javax.media.utils.loaders.sound;

/**
 * Thrown if an audio data stream contains invalid data
 */
@SuppressWarnings ( "serial" )
public class InvalidAudioDataException extends RuntimeException {

    public InvalidAudioDataException ( String message ) {
        super ( message );
    }

}
