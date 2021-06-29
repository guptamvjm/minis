package gitlet;

import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Utils.*;


/** Represents a gitlet commit object.
 *  A commit object has files it tracks, a parent, a date, and a message.
 *  This object stores all such information.
 *
 *  @author M. Gupta
 */
public class Commit implements Serializable, Cloneable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** Date and time of creation of this commit */
    private Date date;

    /** HashMap containing name of text files for the commit as keys,
     * and name (i.e. a hash) of corresponding blobs as values.
     */
    private HashMap<String, String> files;

    /** String that holds the hashcode for the parent commit */
    private String parent;

    /** String that holds the hashcode for the second parent commit
     * A commit has a second parent if it is a merge commit. */
    private String secondParent;

    /** Date formatter */
    public static final SimpleDateFormat FORMATTER =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    @Serial
    /** Exists due to a testing procedure I used where I updated
     * code for gitlet and used it on a gitlet directory made by
     * a previous version. */
    private static final long serialVersionUID = 6220933626438135312L;

    public Commit(Date d, String m, String p) {
        date = d;
        message = m;
        parent = p;
        files = new HashMap<>();
    }

    /** NOTE: Directly passing in a commit and hashing it does not work, it will make the parent be
     * something that DNE. */
    public Commit(String hash, Date d, String m) {
        files = Repository.readCommit(hash).files();
        date = d;
        message = m;
        parent = hash;
    }

    public String message() {
        return message;
    }

    public HashMap<String, String> files() {
        return files;
    }

    public String parent() {
        return parent;
    }

    public String secondParent() {
        return secondParent;
    }

    public void setSecondParent(String d) {
        secondParent = d;
    }

    public String dateString() {
        return FORMATTER.format(date);
    }

    /** Returns whether the commit tracks a file. */
    public boolean blobExists(String filepath) {
        return files.containsKey(filepath);
    }

    /** Removes files contained in map from instance variable files. */
    public void removeFilesInMap(Map<String, String> otherFiles) {
        for (String filepath : otherFiles.keySet()) {
            files.remove(filepath);
        }
    }

    /** Returns split point hash of two commits. */
    public static String findSplitPoint(String c1, String c2) {
        List<String> c1BFS = breadthFirst(c1);
        List<String> c2BFS = breadthFirst(c2);
        for (String hash: c1BFS) {
            if (c2BFS.contains(hash)) {
                return hash;
            }
        }
        return null;
    }

    /** Performs breadth-first search of a commit, returns ancestors as a list. */
    public static List<String> breadthFirst(String commitHash) {
        LinkedList<String> fringe = new LinkedList<>();
        TreeSet<String> marked = new TreeSet<>();
        LinkedList<String> bfs = new LinkedList<>();
        String next = "";
        fringe.addFirst(commitHash);
        do {
            next = fringe.removeFirst();
            bfs.add(next);
            Commit c = Repository.readCommit(next);
            String a = c.parent;
            String b = c.secondParent;
            if (a != null && !marked.contains(a)) {
                fringe.addLast(a);
                marked.add(a);
            }
            if (b != null && !marked.contains(b)) {
                fringe.addLast(b);
                marked.add(b);
            }
        } while (fringe.size() > 0);
        return bfs;
    }

    /** Returns a set of files that exist in both given commits. */
    public static Set<String> existingOverlaps(Commit c1, Commit c2) {
        TreeSet<String> overs = new TreeSet<>();
        for (String filepath : c1.files.keySet()) {
            if (c2.files.containsKey(filepath)) {
                overs.add(filepath);
            }
        }
        return overs;
    }

    /** Returns a set of files that both exist in the same form in both c1 and c2 */
    public static Set<String> sameVersionFiles(Commit c1, Commit c2) {
        TreeSet<String> overs = (TreeSet<String>) existingOverlaps(c1, c2);
        TreeSet<String> copy = new TreeSet<>(overs);
        for (String filepath : copy) {
            String bHash = c1.files.get(filepath);
            if (!bHash.equals(c2.files.get(filepath))) {
                overs.remove(filepath);
            }
        }
        return overs;
    }

    /** Returns a set of files that both exist in different forms in both c1 and c2 */
    public static Set<String> modifiedExistingFiles(Commit c1, Commit c2) {
        TreeSet<String> overs = (TreeSet<String>) existingOverlaps(c1, c2);
        TreeSet<String> copy = new TreeSet<>(overs);
        for (String filepath : copy) {
            String bHash = c1.files.get(filepath);
            if (bHash.equals(c2.files.get(filepath))) {
                overs.remove(filepath);
            }
        }
        return overs;
    }

    public Set<String> fileKeys() {
        return files.keySet();
    }

    /** Returns blob hash given a filepath. */
    public String get(String filepath) {
        return files.get(filepath);
    }

    public boolean isMerge() {
        return secondParent != null;
    }
}

