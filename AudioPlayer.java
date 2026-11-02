import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * full audio player class that suports both WAV and AIFF but not MP3.
 * if MP3 is given it gives an error message and moves on
 */
public class AudioPlayer{

	private Clip clip;
	private Song currentSong;

	/**
	 * Loads the given song's audio file and starts playing it.
	 * Stops whatever was playing before first. Missing files,
	 * unsupported formats, and unavailable audio lines are all
	 * caught and reported rather than thrown.
	 *
	 * @param song the song to play
	 */
	public String playAudio(Song song){
		stopAudio();

		File audioFile = new File(song.getAbsoluteFilePath());
		if(!audioFile.exists()){
			return "Audio file not found: " + song.getAbsoluteFilePath();
		}

		try{
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			clip = AudioSystem.getClip();
			clip.open(audioStream);
			clip.start();
			currentSong = song;
			return "Now playing: " + song.getTitle() + " - " + song.getArtist();
		} catch (UnsupportedAudioFileException e){
			return "Unsupported audio format for " + song.getAbsoluteFilePath();
		} catch (LineUnavailableException e){
			return "No audio line available to play " + song.getTitle();
		} catch (IOException e){
			return "Error reading audio file: " + song.getAbsoluteFilePath();
		}
	}


	/**
	 * Stops playback if something is currently playing and releases
	 * the audio resources it was using.
	 */
	public void stopAudio(){
		if(clip != null){
			if(clip.isRunning()){
				clip.stop();
			}
			clip.close();
			clip = null;
		}
		currentSong = null;
	}

	/**
	 * @return true if a clip is actively playing right now
	 */
	public boolean isPlaying(){
		return clip != null && clip.isRunning();
	}

	/**
	 * @return the song currently loaded/playing, or null if nothing is
	 */
	public Song getCurrentSong(){
		return currentSong;
	}
}
