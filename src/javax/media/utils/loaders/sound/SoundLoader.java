package javax.media.utils.loaders.sound;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.media.utils.loaders.BadConfigurationLineException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Provides access to sound files
 * Sounds are addressed by file name without the extension
 */
public class SoundLoader {
    /**
     * Map from sound name to the corresponding SoundHolder
     */
    private Map<String, SoundHolder> soundMap;

    /**
     * We need a separate MIDI Loader since only a single MIDI can play at any given time
     */
    private MidiLoader midi = null;

    /**
     * Sets up the current class and parses the given configuration file
     * 
     * @param configFilePath Path to configuration file
     * @see #loadSoundsFromConfig(File)
     */
    public SoundLoader ( File configurationFile ) throws IOException, BadConfigurationLineException {
        this ( );
        loadSoundsFromConfig ( configurationFile );
    }

    /**
     * Initializes data structures for storing sounds
     */
    public SoundLoader ( ) {
        this.soundMap = new HashMap<String, SoundHolder> ( );
    }

    /**
     * Reads in the given configuration file, and loads sounds according to these rules:
     * 
     * <pre>
     * m FNM // Loads the given file as a MIDI file
     * c FNM // Loads the given file using Java Sound Clip API
     * //    // a comment line
     * </pre>
     * 
     * Sounds are stored in classes implementing the SoundHolder interface,
     * allowing a unified API for different types of sounds
     * 
     * @param configurationFile Path to configuration file
     * @throws IOException if the configuration file could not be read
     * @throws BadConfigurationLineException if the configuration file contains invalid lines
     */
    private void loadSoundsFromConfig ( File configurationFile ) throws IOException, BadConfigurationLineException {
        System.out.println ( "Reading sound configuration file: " + configurationFile );

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

            if ( tokens.countTokens ( ) != 1 )
                throw new BadConfigurationLineException ( "No filename found for sound" );

            File soundFile = new File ( configurationFile.getParentFile ( ), tokens.nextToken ( ) );
            if ( !soundFile.exists ( ) || !soundFile.canRead ( ) )
                throw new BadConfigurationLineException ( "File for sound does not exist or is not readable" );

            char ch = Character.toLowerCase ( line.charAt ( 0 ) );
            try {
                try {
                    SoundHolder s;
                    switch ( ch ) {
                        // MIDI file
                        case 'm':
                            if ( this.midi == null )
                                this.midi = new MidiLoader ( );
                            s = this.midi.getMidiHolder ( SoundLoader.getFileName ( soundFile ) );
                            break;
                        // Java Sound Clip API
                        case 'c':
                            s = new ClipSoundHolder ( );
                            break;
                        default:
                            throw new BadConfigurationLineException ( "No image load command found! First character should be m or c" );
                    }

                    try {
                        s.loadFile ( soundFile );
                        this.soundMap.put ( SoundLoader.getFileName ( soundFile ), s );
                    } catch ( UnsupportedAudioFileException e ) {
                        throw new BadConfigurationLineException ( "Failed to load audo file: " + e.getMessage ( ) );
                    }
                } catch ( KeyAlreadyExistsException e ) {
                    throw new BadConfigurationLineException ( e.getMessage ( ) );
                } catch ( MidiUnavailableException e ) {
                    throw new BadConfigurationLineException ( "Midi unavailable: " + e.getMessage ( ) );
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
     * Returns the sound holder associated with the given name
     * 
     * @param name Name of the holder to retrieve
     * @return The holder associated with the given name
     */
    public SoundHolder getHolder ( String name ) {
        return this.soundMap.get ( name );
    }

    /**
     * Returns true if a sound holder exists by the given name, false otherwise
     * 
     * @param name Name of sound holder
     * @return true if a sound holder exists by the given name, false otherwise
     */
    public boolean isLoaded ( String name ) {
        return this.soundMap.containsKey ( name );
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
}
