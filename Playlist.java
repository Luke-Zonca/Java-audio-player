import javax.sound.sampled.*;


/**
 * A named playlist of songs, backed by a DoublyLinkedSongList, with basic
 * sequential playback with an AudioPlayer.
 */
public class Playlist{
	private String name;
	private DoublyLinkedSongList songs;
	private AudioPlayer player;
	private int currentIndex;

	/**
	 * Creates an empty playlist with the given name.
	 * @param name display name for this playlist
	 */
	public Playlist(String name){
		this.name = name;
		this.songs = new DoublyLinkedSongList();
		this.player = new AudioPlayer();
		this.currentIndex = -1;
	}

	/**
	 * Wraps a Song in a DoublyLinkedSongNode and adds it to the end of the playlist.
	 * @param song the song to add
	 */
	public void addSong(Song song){
		addSong(new DoublyLinkedSongNode(song));
	}

	/**
	 * Adds an already-built DoublyLinkedSongNode to the end of the playlist.
	 * @param song the node to add
	 */
	public void addSong(DoublyLinkedSongNode song){
		this.songs.addLast(song);
		if(currentIndex == -1){
			//first song added becomes the current song
			currentIndex = 0;
		}
	}

	/**
	 * @return the next node in playlist if applicable 
	 */
	public boolean hasNext(){
	    return currentIndex + 1 < songs.size();
	}

	/**
	 * @return the previous node in the playlist if applicable
	 */
	public boolean hasPrevious(){
	    return currentIndex > 0;
	}

	/**
	 * Stops playback and removes the current song from the playlist.
	 * @return a message describing what happened
	 */
	public String removeCurrentSong(){
	    if(!hasSongs() || currentIndex < 0){
	        return "there is no song to remove";
	    }
	    Song removed = getCurrentSong();
	    stopPlayback();
	    songs.removeAt(currentIndex);

	    if(songs.isEmpty()){
	        currentIndex = -1;
	    } else if(currentIndex >= songs.size()){
	        currentIndex = songs.size() - 1; // removed the last song, so step back
	    }
	    return "Removed: " + removed.getTitle();
	}

	/**
	 * Prints every song in the playlist with a 1-based number in front
	 * of it, and returns that same listing as a String.
	 * @return the string of the playlist
	 */
	public String displayPlaylist(){
		System.out.println(name + "\n");
		if(songs.isEmpty()){
			return "this playlist is empty.";
		}

		StringBuilder sb = new StringBuilder();
		DoublyLinkedSongNode curr = songs.getHead();
		int number = 1;
		while(curr != null){
			String line = number + ".  " + curr.getSong();
			sb.append(line);
			curr = curr.getNext();
			number++;
		}
		return sb.toString();
	}

	/**
	 * Adds up every song's duration and formats the total as
	 * minutes:seconds, e.g. "12:07".
	 * @return the formatted total playlist duration
	 */
	public String getTotalDuration(){
		int totalSec = 0;
		DoublyLinkedSongNode curr = this.songs.getHead();
		while(curr != null){
			totalSec += curr.getSong().getDuration();
			curr = curr.getNext();
		}
 
		int minutes = totalSec / 60;
		int seconds = totalSec % 60;
 
		String time = String.format("%d:%02d", minutes, seconds);
		System.out.println(time);
		return time;
	}

	/**
	 * Plays the song at currentIndex through the AudioPlayer.
	 * @return gives a String of the next song for the label in the UI
	 */
	public String playCurrentSong(){
		if(hasSongs() == false){
			return "there are no songs";
		}
		Song song = getCurrentSong();
		if(song == null){
			return "no song is currently selected";
		}
		return player.playAudio(song);
	}

	/**
	 * Stops whatever the AudioPlayer is currently playing.
	 */
	public void stopPlayback(){
		player.stopAudio();
	}

	/**
	 * Moves the "current song" pointer to the given 0-based position.
	 * @param index index to move to
	 */
	public void setCurrentSong(int index){
		if(index < 0 || index >= songs.size()){
			System.out.println("invalid song index: " + index);
			return;
		}
		currentIndex = index;
	}

	/**
	 * @return the Song at currentIndex, or null if there isn't one
	 */
	public Song getCurrentSong(){
		if(currentIndex < 0){
			return null;
		}
		DoublyLinkedSongNode curr = songs.getHead();
		int i = 0;
		while(curr != null){
			if(i == currentIndex){
				return curr.getSong();
			}
			curr = curr.getNext();
			i++;
		}
		return null;
	}

	/**
	 * Advances to the next song in the playlist and plays it. Stops
	 * at the end of the playlist rather than wrapping back around.
	 * @return a String to show user in UI what song is playing
	 */
	public String playNext(){
		if(hasSongs() == false){
			return "there are no songs";
		}
		if(currentIndex + 1 >= songs.size()){
			return "reached the end of the playlist";
		}
		currentIndex++;
		return playCurrentSong();
	}

	/**
	 * Plays the previous song in list if it is available. Checks for 
	 * edge cases as well.
	 * @return a String of the previous song that is being played
	 */
	public String playPrevious(){
		if(hasSongs() == false){
			return "there is no previous song";
		}
		if(currentIndex - 1 >= songs.size()){
			return "this its the start of the playlist";
		}
		currentIndex--;
		return playCurrentSong();

	}

	/**
	 * @return true if this playlist contains at least one song
	 */
	public boolean hasSongs(){
		return !songs.isEmpty();
	}

	/**
	 * @return the current index
	 */
	public int getCurrentIndex(){
    	return currentIndex;
	}

	public DoublyLinkedSongList getLL(){
		return this.songs;
	}
}