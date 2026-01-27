package zelda;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;

public class AudioManager {
    private Clip currentMusic;
    private HashMap<String, Clip> sfxCache = new HashMap<>();
    private String musicPath = "sounds/";
    
    private float musicVolume = 0.7f;
    private float sfxVolume = 1.0f;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    
    public AudioManager() {}
    
    public void playMusic(String filename) {
        if (!musicEnabled) return;
        
        stopMusic();
        
        try {
            File musicFile = new File(musicPath + filename);
            if (!musicFile.exists()) {
                System.err.println("Music file not found: " + musicFile.getAbsolutePath());
                return;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            currentMusic = AudioSystem.getClip();
            currentMusic.open(audioStream);
            
            setClipVolume(currentMusic, musicVolume);
            
            currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
            currentMusic.start();
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio format: " + filename);
            System.err.println("Note: MP3 requires additional libraries. Consider converting to WAV.");
        } catch (Exception e) {
            System.err.println("Error playing music: " + e.getMessage());
        }
    }
    
    public void playMusicOnce(String filename) {
        if (!musicEnabled) return;
        
        stopMusic();
        
        try {
            File musicFile = new File(musicPath + filename);
            if (!musicFile.exists()) return;
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            currentMusic = AudioSystem.getClip();
            currentMusic.open(audioStream);
            setClipVolume(currentMusic, musicVolume);
            currentMusic.start();
            
        } catch (Exception e) {
            System.err.println("Error playing music: " + e.getMessage());
        }
    }
    
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.close();
            currentMusic = null;
        }
    }
    
    public void playSFX(String filename) {
        if (!sfxEnabled) return;
        
        try {
            Clip sfx = sfxCache.get(filename);
            
            if (sfx == null || !sfx.isOpen()) {
                File sfxFile = new File(musicPath + filename);
                if (!sfxFile.exists()) {
                    System.err.println("SFX file not found: " + filename);
                    return;
                }
                
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(sfxFile);
                sfx = AudioSystem.getClip();
                sfx.open(audioStream);
                sfxCache.put(filename, sfx);
            }
            
            setClipVolume(sfx, sfxVolume);
            
            sfx.setFramePosition(0);
            sfx.start();
            
        } catch (Exception e) {
            System.err.println("Error playing SFX: " + e.getMessage());
        }
    }
    
    private void setClipVolume(Clip clip, float volume) {
        try {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            dB = Math.max(volumeControl.getMinimum(), Math.min(dB, volumeControl.getMaximum()));
            volumeControl.setValue(dB);
        } catch (Exception e) {}
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (currentMusic != null && currentMusic.isOpen()) {
            setClipVolume(currentMusic, musicVolume);
        }
    }
    
    public void setSFXVolume(float volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) stopMusic();
    }
    
    public void setSFXEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
    }
    
    public void cleanup() {
        stopMusic();
        for (Clip sfx : sfxCache.values()) {
            if (sfx != null && sfx.isOpen()) {
                sfx.close();
            }
        }
        sfxCache.clear();
    }
}
