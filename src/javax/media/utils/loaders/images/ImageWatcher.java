package javax.media.utils.loaders.images;

/**
 * Used for callbacks when a sequence ends or loops
 */
public interface ImageWatcher {
    /**
     * Called when an animation sequence ends (and will not repeat)
     * @param animator This image animator
     */
    void sequenceEnded ( ImageAnimator animator );
    /**
     * Called when an animation repeats
     * @param animator This image animator
     */
    void sequenceLooped ( ImageAnimator animator );
}
