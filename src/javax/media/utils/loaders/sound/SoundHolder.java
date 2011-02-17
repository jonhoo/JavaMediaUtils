package javax.media.utils.loaders.sound;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

public interface SoundHolder {

    /**
     * Loads the given File as a sound clip
     * 
     * @param soundFile The audio file
     * @throws UnsupportedAudioFileException If the given sound file cannot be played
     * @throws IOException If the given file could not be opened, or the audio device is not available
     */
    public void loadFile ( File soundFile ) throws UnsupportedAudioFileException, IOException;

    /**
     * Starts playing this audio clip
     * 
     * @throws InvalidAudioDataException If the audio clip contains invalid audio data
     */
    public void play ( ) throws InvalidAudioDataException;

    /**
     * Stops playback (and resets position to the start of the file)
     */
    public void stop ( );

    /**
     * Stops playback, but retains the position in the audio stream
     */
    public void pause ( );

    /**
     * Resumes playback from the previous position
     * If {@link #stop()} was called, {@link #play()} should be used over {@link #resume()}
     */
    public void resume ( );

    /**
     * Seeks to the given position of the audio stream given in milliseconds
     * 
     * @param position Position in milliseconds
     */
    public void seek ( long position );

    /**
     * Enables or disables looping of the clip (i.e. start() is called when the stream ends)
     * 
     * @param enable True to enable looping, false otherwise
     */
    public void setLooping ( boolean enable );

    /**
     * Adds the given watcher to this clip.
     * The watcher will be notified when various events occurs on the clip
     * 
     * @param watcher The object to notify
     */
    public void addWatcher ( SoundWatcher watcher );

    /**
     * Removes the given watcher
     * 
     * @param watcher The watcher to remove
     */
    public void removeWatcher ( SoundWatcher watcher );

}
