package javax.media.utils.loaders;

import java.io.File;

/**
 * Thrown if the parsing of a line in a configuration file yields an error 
 */
@SuppressWarnings ( "serial" )
public class BadConfigurationLineException extends Exception {
    private int lineNumber;
    private String line;
    private File configurationFile;

    public BadConfigurationLineException ( String string ) {
        super ( string );
    }

    public void setLineNumber ( int lineNumber ) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber ( ) {
        return lineNumber;
    }

    public void setLine ( String line ) {
        this.line = line;
    }

    public String getLine ( ) {
        return line;
    }

    public void setConfigurationFile ( File configurationFile ) {
        this.configurationFile = configurationFile;
    }

    public File getConfigurationFile ( ) {
        return configurationFile;
    }
}
