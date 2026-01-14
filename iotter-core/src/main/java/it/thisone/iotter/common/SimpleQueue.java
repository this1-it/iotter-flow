package it.thisone.iotter.common;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple Queue (FIFO) based on LinkedList.
 */
public class SimpleQueue<E> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3446376677506090946L;
	
	private final LinkedList<E> list;

	public SimpleQueue(List<E> list) {
		super();
		this.list = new LinkedList<>(list);
	}

	/**
	 * Puts object in queue.
	 */
	public void put(E o) {
		list.addLast(o);
	}

	/**
	 * Returns an element (object) from queue.
	 *
	 * @return element from queue or <code>null</code> if queue is empty
	 */
	public E pop() {
		if (list.isEmpty()) {
			return null;
		}
		return list.removeFirst();
	}

	/**
	 * Returns all elements from the queue and clears it.
	 */
	public Object[] getAll() {
		Object[] res = new Object[list.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = list.get(i);
		}
		list.clear();
		return res;
	}

	/**
	 * Peeks an element in the queue. Returned elements is not removed from the
	 * queue.
	 */
	public E peek() {
		return list.getFirst();
	}

	/**
	 * Returns <code>true</code> if queue is empty, otherwise <code>false</code>
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Returns queue size.
	 */
	public int size() {
		return list.size();
	}
}
