package de.unibremen.informatik.st.libvcs4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Allows to represent a collection of {@link VCSFile} instances as a file
 * system tree.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FSTree {

	/**
	 * A simple visitor to process {@link FSTree} instances.
	 */
	public static class Visitor {

		/**
		 * Delegates {@code pTree} to {@link #visitDirectory(FSTree)} or
		 * {@link #visitFile(FSTree, VCSFile)} depending on whether
		 * {@code pTree} represents a directory or a file.
		 *
		 * @param pTree
		 * 		The tree to visit and delegate.
		 */
		public void visit(final FSTree pTree) {
			if (pTree.nodes != null) {
				visitDirectory(pTree);
			} else {
				visitFile(pTree, pTree.file);
			}
		}

		/**
		 * Visits the given directory.
		 *
		 * @param pDirectory
		 * 		The directory to visit.
		 */
		protected void visitDirectory(final FSTree pDirectory) {
			pDirectory.nodes.forEach(this::visit);
		}

		/**
		 * Visits the given file.
		 *
		 * @param pTree
		 * 		The tree containing {@code pFile}.
		 * @param pFile
		 * 		The file to visit.
		 */
		protected void visitFile(final FSTree pTree, final VCSFile pFile) {
		}
	}

	/**
	 * The parent of this tree. Is {@code null} for the root node.
	 */
	private final FSTree parent;

	/**
	 * The relative path (relative to {@link VCSEngine#getRoot()}) of the
	 * referenced file (if {@link #file} is present) or directory (if
	 * {@link #nodes} is present).
	 */
	private final String path;

	/**
	 * The referenced file. Is {@code null} if {@link #nodes} is present.
	 */
	private VCSFile file = null;

	/**
	 * The referenced sub files and directories. Is {@code null} if
	 * {@link #file} is present.
	 */
	private List<FSTree> nodes = null;

	/**
	 * Creates a FSTree with given parent and relative path.
	 *
	 * @param pParent
	 * 		The parent of the tree to create. Use {@code null} for root nodes.
	 * @param pPath
	 *      The relative path of the file or directory.
	 * @throws NullPointerException
	 *      If {@code pPath} is {@code null}.
	 */
	private FSTree(final FSTree pParent, final String pPath) {
		parent = pParent;
		path = Objects.requireNonNull(pPath);
	}

	/**
	 * Creates a new tree of the given files.
	 *
	 * @param pFiles
	 *      The files to create the tree from.
	 * @return
	 *      The generated tree.
	 * @throws NullPointerException
	 *      If {@code pFiles} is {@code null}.
	 * @throws IllegalArgumentException
	 *      If {@code pFiles} contains {@code null} values.
	 */
	public static FSTree of(final Collection<VCSFile> pFiles) {
		if (pFiles == null) {
			throw new NullPointerException();
		} else if (pFiles.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException();
		}

		final Set<FSTree> treesWithoutParent = new HashSet<>();
		final Map<String, FSTree> cache = new HashMap<>();
		pFiles.forEach(f -> {
			// (1) "home/user/file.txt" -> "home", "user", "file.txt"
			List<String> parts = new ArrayList<>();
			f.toRelativePath().forEach(p -> parts.add(p.toString()));

			// (2) For each directory ("home", "user")...
			FSTree parent = null;
			for (int i = 0; i < parts.size() - 1; i++) {
				final String path = String.join(
						File.separator, parts.subList(0, i + 1));
				FSTree dir = cache.get(path);
				if (dir == null) {
					dir = new FSTree(parent, path);
					dir.nodes = new ArrayList<>();
					cache.put(path, dir);
					if (parent != null) {
						parent.nodes.add(dir);
					}
				}
				if (parent == null) {
					treesWithoutParent.add(dir);
				}
				parent = dir;
			}

			// (3) Add file ("file.txt").
			final String path = String.join(File.separator, parts);
			FSTree file = cache.get(path);
			if (file == null) {
				file = new FSTree(parent, path);
				cache.put(path, file);
				if (parent != null) {
					parent.nodes.add(file);
				}
			}
			file.file = f;
			if (parent == null) {
				treesWithoutParent.add(file);
			}
		});

		if (treesWithoutParent.isEmpty()) {
			return new FSTree(null, "");
		} else if (treesWithoutParent.size() > 1) {
			final FSTree root = new FSTree(null, "/");
			root.nodes = new ArrayList<>();
			root.nodes.addAll(treesWithoutParent);
			return root;
		} else {
			return treesWithoutParent.iterator().next();
		}
	}

	/**
	 * Returns the parent of this tree.
	 *
	 * @return
	 * 		The parent of this tree.
	 */
	public Optional<FSTree> getParent() {
		return Optional.ofNullable(parent);
	}

	/**
	 * Returns the relative path (relative to {@link VCSEngine#getRoot()}) of
	 * this file (if {@link #getFile()} is present) or directory (if
	 * {@link #getNodes()} is present).
	 *
	 * @return
	 *      The relative path of this file or directory.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the referenced {@link VCSFile} if this tree is a file.
	 *
	 * @return
	 *      The referenced {@link VCSFile}.
	 */
	public Optional<VCSFile> getFile() {
		return Optional.ofNullable(file);
	}

	/**
	 * Returns all files of this tree.
	 *
	 * @return
	 * 		All files of this tree.
	 */
	public List<VCSFile> getFiles() {
		final List<VCSFile> files = new ArrayList<>();
		final Visitor visitor = new Visitor() {
			@Override
			protected void visitFile(final FSTree pTree, final VCSFile pFile) {
				files.add(pFile);
				super.visitFile(pTree, pFile);
			}
		};
		visitor.visit(this);
		return files;
	}

	/**
	 * Returns the sub files and directories if this tree is a directory. If
	 * this tree is a file, an empty list is returned.
	 *
	 * @return
	 *      The sub files and directories of this tree.
	 */
	public List<FSTree> getNodes() {
		return nodes == null
				? Collections.emptyList()
				: new ArrayList<>(nodes);
	}

	/**
	 * Returns the root of this tree.
	 *
	 * @return
	 * 		The root of this tree.
	 */
	public FSTree getRoot() {
		FSTree root = this;
		while (root.getParent().isPresent()) {
			root = root.getParent().get();
		}
		return root;
	}

	/**
	 * Returns whether this tree is a file.
	 *
	 * @return
	 * 		{@code true} if this tree is a file, {@code false} otherwise.
	 */
	public boolean isFile() {
		return file != null;
	}

	/**
	 * Returns whether this tree is a directory.
	 *
	 * @return
	 * 		{@code true} if this tree is a directory, {@code false} otherwise.
	 */
	public boolean isDirectory() {
		return file == null;
	}

	/**
	 * Returns whether this tree is a virtual root directory that has been
	 * created to cover multiple actual roots.
	 *
	 * @return
	 * 		{@code true} if this tree is a virtual root directory,
	 * 		{@code false} otherwise.
	 */
	public boolean isVirtualRoot() {
		return path.isEmpty() || path.equals("/");
	}
}
