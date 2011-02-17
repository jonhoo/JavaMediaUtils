package javax.media.utils.loaders.images;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.management.openmbean.KeyAlreadyExistsException;
import javax.media.utils.loaders.BadConfigurationLineException;

/**
 * Provides access to image files (dynamic and static)
 * Images are addressed by group name (or file name - extension for single images)
 */
public class ImageLoader {
    /**
     * Map from image group name to the corresponding ImageHolder
     */
    private Map<String, ImageHolder> imagesMap;

    /**
     * Sets up the current class and parses the given configuration file
     * 
     * @param configFilePath Path to configuration file
     * @see #loadImagesFromConfig(File)
     */
    public ImageLoader ( File configurationFile ) throws IOException, BadConfigurationLineException {
        this ( );
        loadImagesFromConfig ( configurationFile );
    }

    /**
     * Initializes data structures for storing images
     */
    public ImageLoader ( ) {
        this.imagesMap = new HashMap<String, ImageHolder> ( );
    }

    /**
     * Reads in the given configuration file, and loads images according to these rules:
     * 
     * <pre>
     * o FNM                // a single image file
     * n FNM*.ext N         // a series of numbered image files, whose filenames use the numbers 0 - N-1
     *                      // Note that the * must be the last character before the extension
     * s FNM N              // a strip file containing a single row of number images
     * g GROUP FNM [ FNM ]* // a group of files with different names; they are accessible via GROUP and position or FNM
     * //                   // a comment line
     * </pre>
     * 
     * Given that fnm is the file name without the extension, and n is the index of the image in the strip, sequence or
     * group:
     * Numbered image files can be accessed with [Loader].getHolder(fnm).getImage(n);
     * Strips can be accessed with [Loader].getHolder(fnm).getImage(n);
     * Single images can be accessed with [Loader].getHolder(fnm).getImage(0); or [Loader].getImage(fnm);
     * Group images can be access with [Loader].getHolder(group_name).getImage(n); or
     * [Loader].getGroupHolder(group).getImage(fnm);
     * 
     * Images are stored as BufferedImage objects so they are managed by the JVM when possible
     * 
     * @param configurationFile Path to configuration file
     * @throws IOException if the configuration file could not be read
     * @throws BadConfigurationLineException if the configuration file contains invalid lines
     */
    private void loadImagesFromConfig ( File configurationFile ) throws IOException, BadConfigurationLineException {
        System.out.println ( "Reading image configuration file: " + configurationFile );

        BufferedReader br = new BufferedReader ( new FileReader ( configurationFile ) );
        String line;
        int lineNumber = 0;

        while ( ( line = br.readLine ( ) ) != null ) {
            line = line.trim ( );
            lineNumber++;

            // Dummy statement for pretty layout of code
            if ( line.isEmpty ( ) )
                continue;
            else if ( line.startsWith ( "//" ) )
                continue; // Comment

            // Tokenize and interpret line
            StringTokenizer tokens = new StringTokenizer ( line );
            tokens.nextToken ( ); // To skip the first character token

            char ch = Character.toLowerCase ( line.charAt ( 0 ) );
            try {
                try {
                    switch ( ch ) {
                        // Single image
                        case 'o':
                            if ( tokens.countTokens ( ) != 1 )
                                throw new BadConfigurationLineException ( "No image filename found for single image" );
                            File image = new File ( configurationFile.getParentFile ( ), tokens.nextToken ( ) );
                            this.loadImages ( ImageLoader.getFileName ( image ), new File[] { image }, new ImageHolder ( ) );
                            break;
                        // Sequence of images (filename pattern)
                        case 'n':
                            if ( tokens.countTokens ( ) == 0 )
                                throw new BadConfigurationLineException ( "No image pattern or image number found" );
                            if ( tokens.countTokens ( ) == 1 )
                                throw new BadConfigurationLineException ( "No image count found" );
                            if ( tokens.countTokens ( ) != 2 )
                                throw new BadConfigurationLineException ( "Too many options for sequenced image" );

                            String pattern = tokens.nextToken ( );
                            int imagesInSequence = ImageLoader.intFromCommandToken ( tokens.nextToken ( ) );

                            String[] parts = pattern.split ( "\\*", 0 );
                            if ( parts.length != 2 || parts[0].isEmpty ( ) || parts[1].isEmpty ( ) )
                                throw new BadConfigurationLineException ( "No * found in filename pattern" );

                            if ( this.imagesMap.containsKey ( parts[0] ) )
                                throw new BadConfigurationLineException ( "Sequence name " + parts[0] + " already defined" );

                            File[] images = new File[imagesInSequence];

                            for ( int i = 0; i < imagesInSequence; i++ ) {
                                images[i] = new File ( configurationFile.getParentFile ( ), parts[0] + i + parts[1] );
                                if ( !images[i].exists ( ) )
                                    throw new BadConfigurationLineException ( "Could not find image #" + i + " in sequence " + parts[0] );
                            }

                            this.loadImages ( parts[0], images, new ImageHolder ( ) );
                            break;
                        // Sequence of images in a single file
                        case 's':

                            if ( tokens.countTokens ( ) == 0 )
                                throw new BadConfigurationLineException ( "No image or image count specified for strip" );
                            if ( tokens.countTokens ( ) == 1 )
                                throw new BadConfigurationLineException ( "No image count found" );
                            if ( tokens.countTokens ( ) != 2 )
                                throw new BadConfigurationLineException ( "Too many options for strip" );

                            File stripFile = new File ( configurationFile.getParentFile ( ), tokens.nextToken ( ) );
                            String stripIndex = ImageLoader.getFileName ( stripFile );
                            int imagesInStrip = ImageLoader.intFromCommandToken ( tokens.nextToken ( ) );

                            BufferedImage stripImage;
                            try {
                                stripImage = this.loadImage ( stripFile );
                            } catch ( IOException e ) {
                                throw new BadConfigurationLineException ( "Failed to read image strip file" );
                            }

                            int imWidth = (int) ( stripImage.getWidth ( ) / imagesInStrip );
                            int imHeight = stripImage.getHeight ( );
                            int transparency = stripImage.getColorModel ( ).getTransparency ( );

                            BufferedImage[] strip = new BufferedImage[imagesInStrip];
                            Graphics2D stripGC;

                            // each BufferedImage from the strip file is stored in strip[]
                            for ( int i = 0; i < imagesInStrip; i++ ) {
                                strip[i] = GraphicsEnvironment.getLocalGraphicsEnvironment ( )
                                                          .getDefaultScreenDevice ( )
                                                          .getDefaultConfiguration ( )
                                                          .createCompatibleImage ( imWidth, imHeight, transparency );

                                // create a graphics context
                                stripGC = strip[i].createGraphics ( );

                                // copy image
                                stripGC.drawImage (
                                        stripImage,
                                        0, 0, imWidth, imHeight,
                                        i * imWidth, 0, ( i * imWidth ) + imWidth, imHeight,
                                        null );
                                stripGC.dispose ( );
                            }

                            this.loadImages ( stripIndex, null, strip, new ImageHolder ( ) );

                            break;
                        // Group of images
                        case 'g':

                            if ( tokens.countTokens ( ) == 0 )
                                throw new BadConfigurationLineException ( "No group name given" );
                            if ( tokens.countTokens ( ) == 1 )
                                throw new BadConfigurationLineException ( "No images found in group" );

                            String groupName = tokens.nextToken ( );

                            File[] groupImages = new File[tokens.countTokens ( )];
                            int i = 0;
                            while ( tokens.hasMoreTokens ( ) )
                                groupImages[i++] = new File ( configurationFile.getParentFile ( ), tokens.nextToken ( ) );

                            this.loadImages ( groupName, groupImages, new GroupImageHolder ( ) );
                            break;
                        default:
                            throw new BadConfigurationLineException ( "No image load command found! First character should be o, n, s or g" );
                    }
                } catch ( KeyAlreadyExistsException e ) {
                    throw new BadConfigurationLineException ( e.getMessage ( ) );
                }
            } catch ( BadConfigurationLineException e ) {
                // Recatch the exception to add additional debug information
                e.setLineNumber ( lineNumber );
                e.setLine ( line );
                e.setConfigurationFile ( configurationFile );
                throw e;
            }
        }
        br.close ( );
    }

    /**
     * Loads the given images into the holder at the given index
     * 
     * @see #loadImage(String, File, BufferedImage, ImageHolder)
     */
    public void loadImages ( String index, File[] images, ImageHolder holder ) {
        for ( File f : images )
            try {
                BufferedImage image = this.loadImage ( f );
                this.loadImage ( index, f, image, holder );
            } catch ( IOException e ) {
                System.err.printf ( "Failed to read image file '%s': %s", f, e.getMessage ( ) );
            }
    }

    /**
     * Loads the given images into the holder at the given index
     * 
     * @see #loadImage(String, File, BufferedImage, ImageHolder)
     */
    public void loadImages ( String index, File[] imageFiles, BufferedImage[] images, ImageHolder holder ) throws KeyAlreadyExistsException {
        for ( int i = 0; i < images.length; i++ )
            this.loadImage ( index, imageFiles[i], images[i], holder );
    }

    /**
     * Loads the given image into a image holder at the given index.
     * 
     * If no image holder exists at the given index, the given holder is used.
     * If the given holder is null, a new ImageHolder object is used.
     * If a different holder exists at the given index *and* a holder is given,
     * a KeyAlreadyExistsException is thrown.
     * 
     * imageFile may be null as long as the ImageHolder is not a GroupImageHolder.
     * 
     * @param index The index to use
     * @param imageFile A file associated with the given image
     * @param image The image to add to the holder at the given index
     * @param holder Holder to use if no holder exists at the given index
     * @throws KeyAlreadyExistsException if a holder exists at the given index *and* a holder is given
     */
    public void loadImage ( String index, File imageFile, BufferedImage image, ImageHolder holder ) throws KeyAlreadyExistsException {
        if ( !this.imagesMap.containsKey ( index ) ) {
            if ( holder == null )
                holder = new ImageHolder ( );
            this.imagesMap.put ( index, holder );
        } else if ( holder != null ) {
            if ( holder != this.imagesMap.get ( index ) )
                throw new KeyAlreadyExistsException ( "Attempted to create new image holder, but a holder is already present at the given index " + index );
        }

        this.imagesMap.get ( index ).addImage ( imageFile, image );
    }

    /**
     * Parses the given token as an int, and throws a BadConfigurationLineException if the token is not a positive int
     * 
     * @param token Token to parse
     * @return token parsed as an integer
     * @throws BadConfigurationLineException if the token is not a positive integer
     */
    private static int intFromCommandToken ( String token ) throws BadConfigurationLineException {
        int imageCount;
        try {
            imageCount = Integer.parseInt ( token );
            if ( imageCount <= 0 )
                throw new BadConfigurationLineException ( "Number of images must be > 0" );
            return imageCount;
        } catch ( NumberFormatException e ) {
            throw new BadConfigurationLineException ( "Number of images must be an integer" );
        }
    }

    /**
     * Returns the image holder associated with the given name
     * 
     * @param name Name of the holder to retrieve
     * @return The holder associated with the given name
     * @throws NoSuchElementException if no image by the given name exists
     */
    public ImageHolder getHolder ( String name ) throws NoSuchElementException {
        ImageHolder holder = this.imagesMap.get ( name );
        if ( holder == null )
            throw new NoSuchElementException ( "No such image: " + name );
        return holder;
    }

    /**
     * Returns the group image holder associated with the given name
     * If the given name is not associated with a group holder, null is returned
     * 
     * @param name Name of the group holder to retrieve
     * @return The group holder associated with the given name or null
     * @throws NoSuchElementException if no image by the given group name exists
     */
    public GroupImageHolder getGroupHolder ( String name ) throws NoSuchElementException {
        ImageHolder ih = this.getHolder ( name );
        if ( ih != null && ih instanceof GroupImageHolder )
            return (GroupImageHolder) ih;
        return null;
    }

    /**
     * Gets the first image of the image holder by the given name
     * 
     * @param name Name of the holder to get the first image of
     * @return the first image of the image holder by the given name or null
     * @throws NoSuchElementException if no image by the given group name exists
     */
    public BufferedImage getImage ( String name ) throws NoSuchElementException {
        return this.getHolder ( name ).getImage ( 0 );
    }

    /**
     * Returns true if an image holder exists by the given name, false otherwise
     * 
     * @param name Name of image holder
     * @return true if an image holder exists by the given name, false otherwise
     */
    public boolean isLoaded ( String name ) {
        return this.imagesMap.containsKey ( name );
    }

    /**
     * Returns the number of images in the given image holder
     * Returns 0 if the given image holder does not exist
     * 
     * @param name Name of the image holder
     * @return the number of images in the given image holder
     */
    public int numImages ( String name ) {
        if ( !this.imagesMap.containsKey ( name ) )
            return 0;
        return this.imagesMap.get ( name ).countImages ( );
    }

    /**
     * Returns the file name of the given file (without the extension)
     * 
     * @param file File to get name of
     * @return Name of file without extension
     */
    public static String getFileName ( File file ) {
        int index = file.getName ( ).lastIndexOf ( '.' );
        if ( index > 0 && index <= file.getName ( ).length ( ) - 2 )
            return file.getName ( ).substring ( 0, index );
        return "";
    }

    /**
     * Load the given file name into a BufferedImage object
     * that is compatible with the current graphics device.
     * 
     * @param f The file to load
     * @throws IOException If the given file could not be read
     */
    public BufferedImage loadImage ( File f ) throws IOException {
        return ImageIO.read ( f );
    }
}
