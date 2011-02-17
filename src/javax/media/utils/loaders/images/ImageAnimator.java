package javax.media.utils.loaders.images;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Timer;

public class ImageAnimator implements ActionListener {
    private Set<ImageWatcher> watchers;
    private boolean isRepeating = false;
    protected int currentImage = 0;
    private ImageHolder source;

    private Timer timer;
    /**
     * Time between ticks in ms
     */
    private long tickPeriod;
    /**
     * How long the animation has gone on for in ms
     */
    private long animationTime = 0;

    /**
     * Create a new image animator from the given source image holder
     * 
     * @param source Source image
     * @param tickrate Duration of each frame
     */
    public ImageAnimator ( ImageHolder source, long tickrate ) {
        this.source = source;
        this.watchers = Collections.synchronizedSet ( new HashSet<ImageWatcher> ( ) );
        this.timer = new Timer ( 0, this );
        this.setAnimationPeriod ( tickrate );
    }

    /**
     * Returns this animators image holder
     * 
     * @return this animators image holder
     */
    public ImageHolder getImageHolder ( ) {
        return this.source;
    }

    /**
     * Adds the given watcher as a listener for sequence-related events
     * 
     * @param watcher Objec to notify
     */
    public void addWatcher ( ImageWatcher watcher ) {
        this.watchers.add ( watcher );
    }

    /**
     * Enable/Disable looping of this animation
     * 
     * @param shouldRepeat True to enable, false to disable
     */
    public void setRepeating ( boolean shouldRepeat ) {
        this.isRepeating = true;
    }

    /**
     * Sets the time between each animation frame in milliseconds
     * If an argument is <= 0, the parameter is not changed
     * 
     * @param tickPeriod Approximate time between ticks in ms
     */
    public void setAnimationPeriod ( long tickPeriod ) {
        if ( tickPeriod >= 0 )
            this.tickPeriod = tickPeriod;
        this.timer.setDelay ( (int) this.tickPeriod );
    }

    /**
     * Updates the current image of the sequence
     * Should only be called by this ImageHolder's Timer
     */
    public void actionPerformed ( ActionEvent e ) {
        if ( this.source.countImages ( ) > 1 && this.tickPeriod > 0 ) {
            /**
             * Total animation time is the current animation time, add the time since last tick,
             * modulo the length of the sequence
             */
            this.animationTime = ( this.animationTime + this.tickPeriod ) % ( this.tickPeriod * this.source.countImages ( ) );

            this.currentImage = (int) ( this.animationTime / this.tickPeriod );
        } else
            this.currentImage = 0;

        if ( this.currentImage == this.source.countImages ( ) - 1 ) {
            // We're at the end of the sequence

            if ( !this.isRepeating ) {
                this.timer.stop ( );

                for ( ImageWatcher iw : this.watchers ) {
                    iw.sequenceEnded ( this );
                }
            } else {
                for ( ImageWatcher iw : this.watchers ) {
                    iw.sequenceLooped ( this );
                }
            }
        }
    }

    /**
     * Returns the current image in the animation sequence
     * 
     * @return the current image in the animation sequence
     */
    public BufferedImage getCurrentImage ( ) {
        return this.source.getImage ( this.currentImage );
    }

    /**
     * Returns the position in the current animation sequence
     * 
     * @return the position in the current animation sequence
     */
    public int getCurrentPosition ( ) {
        return this.currentImage;
    }

    /**
     * Starts the animation
     */
    public void start ( ) {
        this.timer.start ( );
    }

    /**
     * Stops the animation
     */
    public void stop ( ) {
        this.timer.stop ( );
    }

    /**
     * Returns true if the animation is currently stopped
     * 
     * @return true if the animation is currently stopped
     */
    public boolean isStopped ( ) {
        return !this.timer.isRunning ( );
    }

    /**
     * Returns true if the current animation is at the last image, and will not repeat
     * 
     * @return true if the current animation is at the last image, and will not repeat
     */
    public boolean finishedLooping ( ) {
        return ( ( this.currentImage == this.source.countImages ( ) - 1 ) && ( !this.isRepeating ) );
    }

    /**
     * Start showing the images again, starting with image number
     * position.
     * 
     * @param position New image position to start at. Should be in range 0 - (#images - 1)
     */
    public void restartAt ( int position ) {
        if ( position <= 0 || position >= this.source.countImages ( ) )
            return;

        this.currentImage = position;
        this.animationTime = this.tickPeriod * position;
        this.timer.start ( );
    }

    /**
     * Resume animation from where it was previously stopped
     */
    public void resume ( ) {
        this.timer.start ( );
    }

    /**
     * Prepares this animator for destruction by stopping the associated
     * timer and emptying pointers.
     * Only call this if you're never going to use this animator again!
     */
    public void destroy ( ) {
        this.timer.stop ( );
        this.timer.removeActionListener ( this );
        this.timer = null;
        this.watchers = null;
    }
}
