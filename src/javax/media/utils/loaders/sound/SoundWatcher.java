package javax.media.utils.loaders.sound;

/**
 * Used for callbacks when a sequence ends or loops
 */
public interface SoundWatcher {
    /**
     * Called when a sound sequence ends (and will not repeat)
     * 
     * @param holder This sound holder
     */
    void sequenceEnded ( SoundHolder holder );

    /**
     * Called when a sound sequence repeats
     * 
     * @param holder This sound holder
     */
    void sequenceLooped ( SoundHolder holder );
}