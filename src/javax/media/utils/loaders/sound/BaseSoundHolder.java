package javax.media.utils.loaders.sound;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements looping and watchers functionality common to all SoundHolders
 */
public abstract class BaseSoundHolder implements SoundHolder {

    private Set<SoundWatcher> watchers = new HashSet<SoundWatcher>();

    protected boolean isLooping = false;

    protected enum State {
        STOPPED_MANUALLY,
        PAUSED,
        PLAYING,
        FINISHED
    };

    protected State state = State.FINISHED;
    
    @Override
    public void setLooping ( boolean enable ) {
        this.isLooping = enable;
    }

    @Override
    public void addWatcher ( SoundWatcher watcher ) {
        this.watchers.add ( watcher );
    }

    @Override
    public void removeWatcher ( SoundWatcher watcher ) {
        this.watchers.remove ( watcher );
    }

    /**
     * Should be called by the implementing SoundHolder class
     * when the playing audio clip finishes (reaches the end)
     * and is not looped
     */
    protected void onFinish ( ) {
        for ( SoundWatcher watcher : this.watchers )
            watcher.sequenceEnded ( this );
    }

    /**
     * Should be called by the implementing SoundHolder class
     * when the playing audio clip finishes (reaches the end)
     * and is looped
     */
    protected void onLoop ( ) {
        for ( SoundWatcher watcher : this.watchers )
            watcher.sequenceLooped ( this );
    }

}
