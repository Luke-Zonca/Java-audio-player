/**
 * A singly linked list of DoublyLinkedSongNode objects, backing a Playlist. Keeps
 * both a head and a tail pointer so adding to either end is O(1).
 */
public class DoublyLinkedSongList{
	private DoublyLinkedSongNode head;
	private DoublyLinkedSongNode tail;

	/**
	 * Builds a list that starts with a single node already in it.
	 * @param song the first node in the list
	 */
	public DoublyLinkedSongList(DoublyLinkedSongNode song){
		this.head = song;
		this.tail = song;
	}

	/**
	 * Builds an empty list.
	 */
	public DoublyLinkedSongList(){
		this.head = null;
		this.tail = null;
	}

	public DoublyLinkedSongNode getHead(){
		return this.head;
	}

	public DoublyLinkedSongNode getTail(){
		return this.tail;
	}


	/**
	 * Adds a node to the front of the list.
	 * @param next the node to add
	 */
	public void addFirst(DoublyLinkedSongNode next){
		if(isEmpty()){
			this.head = next;
			this.tail = next;
			return;
		}
		head.setPrevious(next);
		next.setNext(head);
		this.head = next;
	}


	/**
	 * Adds a node to the end of the list.
	 * @param next the node to add
	 */
	public void addLast(DoublyLinkedSongNode next){
		if(isEmpty()){
			this.head = next;
			this.tail = next;
			return;
		}
		tail.setNext(next);
		next.setPrevious(tail);
		this.tail = next;
	}


	/**
	 * Removes and returns the node at the front of the list.
	 * @return the removed node, or null if the list was already empty
	 */
	public DoublyLinkedSongNode removeFirst(){
		if(isEmpty()){
			return null;
		}
		DoublyLinkedSongNode curr = head;
		head = head.getNext();
		if(head == null){
			//we just removed the only node
			tail = null;
		}
		curr.setNext(null);
		if(head != null){
		    head.setPrevious(null);
		}
		return curr;
	}


	/**
	 * Removes and returns the node at the end of the list.
	 * @return the removed node, or null if the list was already empty
	 */
	public DoublyLinkedSongNode removeLast(){
		if(isEmpty()){
			return null;
		}
 
		DoublyLinkedSongNode last = tail;
		if(head == tail){
			//only one node in the list
			head = null;
			tail = null;
			return last;
		}
 
		tail = last.getPrevious();
		last.setPrevious(null);
		tail.setNext(null);
		return last;
	}

	/**
	 * @return how many songs are currently in the list
	 */
	public int size(){
		int counter = 0;
		DoublyLinkedSongNode curr = this.head;
		while(curr != null){
			counter++;
			curr = curr.getNext();
		}
		return counter;
	}


	/**
	 * @return true if the list has no songs in it
	 */
	public boolean isEmpty(){
		return head == null;
	}	
 
	/**
	 * @return every song in the list, in order, concatenated together
	 */
	public String displayForward(){
		StringBuilder sb = new StringBuilder();
		DoublyLinkedSongNode curr = head;
		while(curr != null){
			sb.append(curr.getSong());
			curr = curr.getNext();
		}
		return sb.toString();
	}

	/**
	 *@return every song in the list in reverse order 
	 */
	public String displayBackwards(){
		StringBuilder sb = new StringBuilder();
		DoublyLinkedSongNode curr = tail;
		while(curr != null){
			sb.append(curr.getSong());
			curr = curr.getPrevious();
		}
		return sb.toString();
	}

	/**
	 * Removes and returns the node at the given 0-based index.
	 * @return the removed node, or null if the index is out of range
	 */
	public DoublyLinkedSongNode removeAt(int index){
	    if(index < 0 || index >= size()){
	        return null;
	    }
	    if(index == 0){
	        return removeFirst();
	    }
	    if(index == size() - 1){
	        return removeLast();
	    }
	    DoublyLinkedSongNode curr = head;
	    for(int i = 0; i < index; i++){
	        curr = curr.getNext();
	    }
	    curr.getPrevious().setNext(curr.getNext());
	    curr.getNext().setPrevious(curr.getPrevious());
	    curr.setNext(null);
	    curr.setPrevious(null);
	    return curr;
	}
 
}