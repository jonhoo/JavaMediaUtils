package javax.media.utils.loaders.sound;

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

/**
 * Wrapper class for loading {@link MidiSoundHolder} objects
 * Needed since only one MIDI can play on a sequencer at any given time
 */
public class MidiLoader {
    /**
     * Shared MIDI sequencer
     */
    private Sequencer sequencer;

    /**
     * Map of MIDIs to their corresponding Holder elements
     */
    private Map<String, MidiSoundHolder> midisMap;

    /**
     * Currently playing MIDI file
     */
    private MidiSoundHolder currentMidi;

    /**
     * Initialize the MIDI sequencer and data storage
     */
    public MidiLoader ( ) throws MidiUnavailableException {
        this.midisMap = new HashMap<String, MidiSoundHolder> ( );

        this.sequencer = MidiSystem.getSequencer ( );
        if ( this.sequencer == null )
            throw new MidiUnavailableException ( "No MIDI sequencer found" );

        this.sequencer.open ( );

        /**
         * Link system synthesizer and sequencer if they are not already linked
         */
        if ( !( this.sequencer instanceof Synthesizer ) ) {
            System.out.println ( "Linking the MIDI sequencer and synthesizer" );
            Synthesizer synthesizer = MidiSystem.getSynthesizer ( );
            Receiver synthReceiver = synthesizer.getReceiver ( );
            Transmitter seqTransmitter = this.sequencer.getTransmitter ( );
            seqTransmitter.setReceiver ( synthReceiver );
        }
    }

    /**
     * Returns a new SoundHolder linked to this loader
     * 
     * @param name Name to index the given clip by
     * @return a new SoundHolder linked to this loader
     * @throws KeyAlreadyExistsException
     */
    public SoundHolder getMidiHolder ( String name ) throws KeyAlreadyExistsException {
        if ( this.midisMap.containsKey ( name ) )
            throw new KeyAlreadyExistsException ( "Sound name " + name + " already exists!" );

        this.midisMap.put ( name, new MidiSoundHolder ( this ) );
        return this.midisMap.get ( name );
    }

    /**
     * Returns the SoundHolder by the given name
     * 
     * @param name Name of the SoundHolder to return
     * @return the SoundHolder by the given name
     */
    public MidiSoundHolder getMidi ( String name ) {
        return this.midisMap.get ( name );
    }

    /**
     * Called by SoundHolder objects when they stop playing
     * Useful so that the MidiLoader can allow another clip to play
     * 
     * @param sound SoundHolder that stopped playing
     */
    public void notifyStoppedPlaying ( MidiSoundHolder sound ) {
        if ( this.currentMidi == sound ) {
            this.currentMidi = null;
            this.sequencer.removeMetaEventListener ( sound );
        }
    }

    /**
     * Returns the current sequencer if no other sound is playing
     * 
     * @param sound The SoundHolder requesting the sequencer
     * @return the current sequencer if no other sound is playing
     */
    public Sequencer requestPlay ( MidiSoundHolder sound ) {
        if ( this.currentMidi != null && this.currentMidi != sound )
            return null;
        this.currentMidi = sound;
        this.sequencer.addMetaEventListener ( sound );
        return this.sequencer;
    }

    /**
     * Returns the current sequencer if the given sound is the playing sound
     * 
     * @param sound The SoundHolder requesting the sequencer
     * @return the current sequencer if the given sound is the playing sound
     */
    public Sequencer requestSequencer ( MidiSoundHolder sound ) {
        if ( this.currentMidi != sound )
            return null;
        return this.sequencer;
    }

    /**
     * Closes down the sequencer (and any playing sequence).
     */
    public void close ( ) {
        if ( this.currentMidi != null ) {
            this.currentMidi.stop ( );
            this.sequencer.removeMetaEventListener ( this.currentMidi );
        }

        if ( this.sequencer.isRunning ( ) )
            this.sequencer.stop ( );

        this.sequencer.close ( );
        this.sequencer = null;
    }

}
