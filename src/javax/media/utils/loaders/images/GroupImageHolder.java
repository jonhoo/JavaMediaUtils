package javax.media.utils.loaders.images;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GroupImageHolder extends ImageHolder {
    private List<String> imageNames;

    public GroupImageHolder ( ) {
        super ( );
        this.imageNames = new ArrayList<String> ( );
    }

    @Override
    public void addImage ( String imageName, BufferedImage image ) {
        // We can't allow images in a group that don't have a name!
        if ( imageName == null )
            return;

        super.addImage ( imageName, image );
        this.imageNames.add ( ImageLoader.getResourceIndex ( imageName ) );
    }

    /**
     * Gets image by image name
     * 
     * @param name Name of image to retrieve
     * @return The image with the given name or null
     */
    public BufferedImage getImage ( String name ) {
        if ( !this.imageNames.contains ( name ) )
            return null;
        return this.images.get ( this.imageNames.indexOf ( name ) );
    }

    /**
     * Returns the index of the image with the given name
     * 
     * @param name
     * @return the index of the image with the given name
     */
    public int getIndexOf ( String name ) {
        return this.imageNames.indexOf ( name );
    }

    /**
     * Returns the name of the image at the given position
     * 
     * @param index
     * @return the name of the image at the given position
     */
    public String getImageName ( int index ) {
        return this.imageNames.get ( index );
    }

    /**
     * Returns a new animator for this image group.
     * 
     * @param tickrate Duration of each frame
     * @return a new animator for this image group
     */
    public GroupImageAnimator getGroupAnimator ( long tickrate ) {
        return new GroupImageAnimator ( this, tickrate );
    }

    @Override
    public String toString ( ) {
        return this.imageNames.toString ( );
    }
}
