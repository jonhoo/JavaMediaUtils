package javax.media.utils.loaders.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ImageHolder that can also do tick-based animation
 * It must be supplied with the duration of the animation,
 * and the approximate time between ticks, and will update
 * the image returned by getCurrentImage to the appropriate
 * one in the animation cycle on every tick.
 * 
 * Consumers of this class may register as watchers,
 * and be notified when the animation loops or ends.
 */
public class ImageHolder {

    protected List<BufferedImage> images;

    /**
     * Initializes data storage needed for this image holder
     */
    public ImageHolder ( ) {
        this.images = Collections.synchronizedList ( new ArrayList<BufferedImage> ( ) );
    }

    /**
     * Adds the given image to this image holder
     * Image order is preserved, and used for animation sequencing as well
     * 
     * Note that for GroupImageHolders, imageFile must not be null as it
     * is used to determine the index for the given image.
     * 
     * @param imageFile an object representing the given image on disk
     * @param image the buffered representation of the image
     */
    public void addImage ( File imageFile, BufferedImage image ) {
        this.images.add ( image );
    }

    /**
     * Returns the image at the given index
     * 
     * @param image Index of image
     * @return the image at the given index
     */
    public BufferedImage getImage ( int image ) {
        return this.images.get ( image );
    }

    /**
     * Returns the number of images in this image holder
     * 
     * @return the number of images in this image holder
     */
    public int countImages ( ) {
        return this.images.size ( );
    }
    
    /**
     * Returns a new animator for this image.
     * 
     * @param tickrate Duration of each frame
     * @return a new animator for this image.
     */
    public ImageAnimator getAnimator ( long tickrate ) {
        return new ImageAnimator ( this, tickrate );
    }
}