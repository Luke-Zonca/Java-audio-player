
/**
 * Represents a single song's metadata: title, artist, the path to its
 * audio file on disk, and its duration in seconds. This class does not
 * touch audio playback at all - it's just the data.
 */
public class Song{
	private String title;
	private String artist;
	private String absoluteFilePath;
	private int duration;

	/**
	 * Builds a Song from its metadata.
	 *
	 * @param title             song title
	 * @param artist            performing artist
	 * @param absoluteFilePath  path to the audio file, e.g. "music/song.wav"
	 * @param duration          length of the song in seconds
	 */
	public Song(String title, String artist, String absoluteFilePath, int duration){
		this.title = title;
		this.artist = artist;
		this.absoluteFilePath = absoluteFilePath;
		this.duration = duration;
	}

	//Getters
	public String getTitle(){
		return this.title;
	}
	public String getArtist(){
		return this.artist;
	}
	public String getAbsoluteFilePath(){
		return this.absoluteFilePath;
	}
	public int getDuration(){
		return this.duration;
	}

	//Setters
	public void setTitle(String title){
		this.title = title;
	}
	public void setArtist(String artist){
		this.artist = artist;
	}
	public void setAbsoluteFilePath(String absoluteFilePath){
		this.absoluteFilePath = absoluteFilePath;
	}
	public void setDuration(int duration){
		this.duration = duration;
	}

	/**
	 * CSV-style representation of this song, used by Main when saving
	 * a playlist back out to file: title,artist,absoluteFilePath,duration
	 */
	@Override
	public String toString(){
		return 	this.title + "," + 
				this.artist + "," +  
				this.duration + "\n";
	}
}