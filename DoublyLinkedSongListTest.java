import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for DoublyLinkedSongList.
 */
public class DoublyLinkedSongListTest{

	private DoublyLinkedSongList list;
	private DoublyLinkedSongNode a;
	private DoublyLinkedSongNode b;
	private DoublyLinkedSongNode c;

	// Runs before every test, so each test starts with a fresh empty list
	@BeforeEach
	void setUp(){
		list = new DoublyLinkedSongList();
		a = makeNode("A");
		b = makeNode("B");
		c = makeNode("C");
	}

	private DoublyLinkedSongNode makeNode(String title){
		return new DoublyLinkedSongNode(new Song(title, "Artist", "music/" + title + ".wav", 100));
	}

	//Adding at the head and the tail
	@Test
	void addFirstAndAddLast(){
		list.addLast(b);   // B
		list.addFirst(a);  // A B
		list.addLast(c);   // A B C

		assertEquals(3, list.size());
		assertSame(a, list.getHead());
		assertSame(c, list.getTail());
	}

	//Removing from the head and the tail
	@Test
	void removeFirstAndRemoveLast(){
		list.addLast(a);
		list.addLast(b);
		list.addLast(c);

		assertSame(a, list.removeFirst()); // B C
		assertSame(b, list.getHead());

		assertSame(c, list.removeLast());  // B
		assertSame(b, list.getTail());
		assertEquals(1, list.size());
	}

	//Forward traversal
	@Test
	void forwardTraversal(){
		list.addLast(a);
		list.addLast(b);
		list.addLast(c);

		String expected = a.getSong().toString() + b.getSong().toString() + c.getSong().toString();
		assertEquals(expected, list.displayForward());
	}

	//Backward traversal
	@Test
	void backwardTraversal(){
		list.addLast(a);
		list.addLast(b);
		list.addLast(c);

		String expected = c.getSong().toString() + b.getSong().toString() + a.getSong().toString();
		assertEquals(expected, list.displayBackwards());
	}

	//Empty list behavior
	@Test
	void emptyList(){
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
		assertNull(list.getHead());
		assertNull(list.getTail());
		assertNull(list.removeFirst());
		assertNull(list.removeLast());
		assertEquals("", list.displayForward());
		assertEquals("", list.displayBackwards());

		// removing the only song should leave the list empty again
		list.addFirst(a);
		list.removeFirst();
		assertTrue(list.isEmpty());
		assertNull(list.getTail());
	}
}
