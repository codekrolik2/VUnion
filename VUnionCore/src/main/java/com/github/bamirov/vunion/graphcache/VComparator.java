package com.github.bamirov.vunion.graphcache;

import java.util.Optional;

public class VComparator<V extends Comparable<V>> {
	/**
     * Compares two objects with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as v1 is less
     * than, equal to, or greater than v2.
     * null is considered minimal value.
     * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public int compare(V v1, V v2) {
		if (v1 == v2)
			return 0;
		if (v1 == null)
			return -1;
		if (v2 == null)
			return 1;
		return v1.compareTo(v2);
	}
	
	public int compare(Optional<V> v1, V v2) {
		return compare(v1.isPresent() ? v1.get() : null, v2);
	}
	
	public int compare(V v1, Optional<V> v2) {
		return compare(v1, v2.isPresent() ? v2.get() : null);
	}
	
	public int compare(Optional<V> v1, Optional<V> v2) {
		return compare(v1.isPresent() ? v1.get() : null, v2.isPresent() ? v2.get() : null);
	}
}
