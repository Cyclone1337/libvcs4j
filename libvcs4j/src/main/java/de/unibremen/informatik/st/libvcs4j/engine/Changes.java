package de.unibremen.informatik.st.libvcs4j.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Stores all files (absolute paths without directories) that have been changed
 * between two revisions.
 */
public class Changes {

	/**
	 * Added files.
	 */
	private final List<String> added = new ArrayList<>();

	/**
	 * Removed files.
	 */
	private final List<String> removed = new ArrayList<>();

	/**
	 * Modified files.
	 */
	private final List<String> modified = new ArrayList<>();

	/**
	 * Relocated files (from -> to).
	 */
	private final List<Entry<String, String>> relocated = new ArrayList<>();

	public List<String> getAdded() {
		return added;
	}

	public List<String> getRemoved() {
		return removed;
	}

	public List<String> getModified() {
		return modified;
	}

	public List<Entry<String, String>> getRelocated() {
		return relocated;
	}
}
