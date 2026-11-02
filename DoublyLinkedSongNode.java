/**
 * A single node in a SongLinkedList: holds one Song and a pointer to
 * the next node in the list.
 */
public class DoublyLinkedSongNode{
	private Song song;
	private DoublyLinkedSongNode next;
	private DoublyLinkedSongNode previous;
	
	/**
	 * Wraps a Song in a new node. The node starts with no next node. the node starts with no previous node.
	 * @param song the song this node holds
	 */
	public DoublyLinkedSongNode(Song song){
		this.song = song;
		this.next = null;
		this.previous = null;
	}

	public void setSong(Song song){
		this.song = song;
	}
	public void setNext(DoublyLinkedSongNode nextNode){
		this.next = nextNode;
	}
	public void setPrevious(DoublyLinkedSongNode previousNode){
		this.previous = previousNode;
	}

	public Song getSong(){
		return song;
	}
	public DoublyLinkedSongNode getNext(){
		return next;
	}
	public DoublyLinkedSongNode getPrevious(){
		return previous;
	}

	public String toString(){
		return this.song + " - " + this.next + " - " + this.previous;
	}
}