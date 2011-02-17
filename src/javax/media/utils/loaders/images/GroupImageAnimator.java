package javax.media.utils.loaders.images;

import java.util.NoSuchElementException;

public class GroupImageAnimator extends ImageAnimator {

    private GroupImageHolder group;

    public GroupImageAnimator ( GroupImageHolder source, long tickrate ) {
        super ( source, tickrate );
        this.group = source;
    }

    /**
     * Sets the current image to the one with the given name
     * 
     * @param name Name of image to set current image to
     * @throws NoSuchElementException if the given name is not in this group
     */
    public void setCurrentImage ( String name ) throws NoSuchElementException {
        int index = this.group.getIndexOf ( name );
        if ( index < 0 )
            throw new NoSuchElementException ( "No image named " + name + " in current image group: " + this.group );
        this.currentImage = index;
    }

    /**
     * Returns the name of the current image
     * 
     * @return the name of the current image
     */
    public String getCurrentImageName ( ) {
        return this.group.getImageName ( this.currentImage );
    }
    
    /**
     * Returns this animators image group holder
     * 
     * @return this animators image group holder
     */
    public GroupImageHolder getImageGroupHolder ( ) {
        return this.group;
    }
}
