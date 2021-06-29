package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.util.*;

import java.io.IOException;

/** Represents a gitlet repository.
 * At a high level, has all the methods that a gitlet repo might execute. That is,
 * it performs all gitlet commands.
 *
 *  @author M. Gupta
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The folder that stores commits */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    /** The folder that stores blobs */
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    /** The file that stores the stage */
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");
    /** The file that stores the hashtable of branch pointers. */
    public static final File HEADS_FILE = join(GITLET_DIR, "HEADS");
    /** The file that stores the hashtable of files to be removed. */
    public static final File REMOVE_FILE = join(GITLET_DIR, "remove");
    /** Key used to store name of current branch in heads. */
    public static final String DUMMY = "HEADHEADHEADHEADHEADHEADHEADHEADHEADHEAD";


    public static boolean repoExists() {
        return GITLET_DIR.exists();
    }

    /** Initializes repository */
    public static void init() {
        makePersistence();
        Date d = new Date();
        d.setTime(0);
        Commit c = new Commit(d, "initial commit", null);
        String cHash = sha1(serialize(c));
        HashMap<String, String> heads = new HashMap<>();
        heads.put(DUMMY, "master");
        heads.put("master", cHash);
        writeObject(HEADS_FILE, heads);
        File f = Utils.join(COMMIT_DIR, cHash);
        makeFile(f);
        writeObject(f, c);
        HashMap<String, String> stage = new HashMap<>();
        writeObject(STAGE_FILE, stage);
        HashMap<String, String> remove = new HashMap<>();
        writeObject(REMOVE_FILE, remove);
    }

    /** Creates files. Part of init process */
    public static void makePersistence() {
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        makeFile(STAGE_FILE);
        makeFile(HEADS_FILE);
        makeFile(REMOVE_FILE);
    }

    /** Home method for add command */
    public static void addToStage(String filename) {
        Commit head = readHead();
        String filepath = Utils.join(CWD, filename).getPath();
        Blob contents = new Blob(filepath);
        String bHash = sha1(serialize(contents));
        HashMap<String, String> stage = readData(STAGE_FILE);
        HashMap<String, String> removals = readData(REMOVE_FILE);
        boolean alreadyThere = fileAlreadyExists(filepath, stage);
        if (removals.containsKey(filepath)) {
            removals.remove(filepath);
            writeObject(REMOVE_FILE, removals);
        }
        if (!alreadyThere) {
            stage.put(filepath, bHash);
            File f = Utils.join(BLOB_DIR, bHash);
            Repository.makeFile(f);
            Utils.writeObject(f, contents);
        }
        if (stage.get(filepath) != null && stage.get(filepath).equals(head.files().get(filepath))) {
            stage.remove(filepath);
        }
        writeObject(STAGE_FILE, stage);
    }

    /** Home method for remove command */
    public static void remove(String filename) {
        File f = join(CWD, filename);
        String filepath = f.getPath();
        HashMap<String, String> stage = readData(STAGE_FILE);
        HashMap<String, String> removals = readData(REMOVE_FILE);
        Commit head = readHead();
        if (!stage.containsKey(filepath) && !head.blobExists(filepath)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        stage.remove(filepath);
        if (head.blobExists(filepath)) {
            removals.put(filepath, head.get(filepath));
            String blobConts = readBlob(head.get(filepath)).contents();
            if (f.exists() && blobConts.equals(readContentsAsString(f))) {
                f.delete();
            }
        }
        writeObject(REMOVE_FILE, removals);
        writeObject(STAGE_FILE, stage);
    }

    /** Returns whether a given file exists in the stage, or if it is the same in the head commit */
    private static boolean fileAlreadyExists(String filepath, HashMap<String, String> stage) {
        Commit head = readHead();
        Blob contents = new Blob(filepath);
        String bHash = sha1(serialize(contents));
        if (!stage.containsKey(filepath)) { //if not in stage, we look to commit
            if (head.files().containsKey(filepath)) { //if commit has version of file
                if (head.get(filepath).equals(bHash)) { //versions are equal
                    return true;
                }
            }
        } else if (bHash.equals(head.get(filepath))) {
            return true;
        }
        return false;
    }

    /** Home method for commit command */
    public static void commit(String message) {
        HashMap<String, String> heads = readData(HEADS_FILE);
        HashMap<String, String> removals = readData(REMOVE_FILE);
        HashMap<String, String> stage = readData(STAGE_FILE);
        if (stage.size() + removals.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Date now = new Date();
        Commit toAdd = new Commit(headHash(), now, message);
        toAdd.removeFilesInMap(removals);
        toAdd.files().putAll(stage);
        clearStage();
        clearRemovals();
        String commitHash = sha1(serialize(toAdd));
        heads.put(heads.get(DUMMY), commitHash);
        Utils.writeObject(HEADS_FILE, heads);
        File f = Utils.join(COMMIT_DIR, commitHash);
        makeFile(f);
        writeObject(f, toAdd);
    }

    /** Method for merge commits */
    public static void commit(String message, String secondParent) {
        HashMap<String, String> heads = readData(HEADS_FILE);
        HashMap<String, String> removals = readData(REMOVE_FILE);
        HashMap<String, String> stage = readData(STAGE_FILE);
        Date now = new Date();
        Commit toAdd = new Commit(headHash(), now, message);
        toAdd.removeFilesInMap(removals);
        toAdd.setSecondParent(secondParent);
        toAdd.files().putAll(stage);
        clearStage();
        String commitHash = sha1(serialize(toAdd));
        heads.put(heads.get(DUMMY), commitHash);
        clearRemovals();
        Utils.writeObject(HEADS_FILE, heads);
        File f = Utils.join(COMMIT_DIR, commitHash);
        makeFile(f);
        writeObject(f, toAdd);
    }

    /** Home method for first version checkout command */
    public static void checkout(String filename) {
        Commit head = readHead();
        String filepath = Utils.join(CWD, filename).getPath();
        if (head.blobExists(filepath)) {
            Blob x = readBlob(head.get(filepath));
            String thingToWrite = x.contents();
            writeContents(new File(filepath), thingToWrite);
        } else {
            firstCheckoutError();
        }
    }

    /** Home method for second version checkout command */
    public static void checkout(String filename, String id) {
        Commit c = findCommit(id);
        if (c == null) {
            secondCheckoutError();
            return;
        }
        File f = Utils.join(CWD, filename);
        String filepath = f.getPath();
        if (c.blobExists(filepath)) {
            if (!f.exists()) {
                makeFile(f);
            }
            Blob x = readBlob(c.get(filepath));
            String thingToWrite = x.contents();
            writeContents(new File(filepath), thingToWrite);
        } else {
            firstCheckoutError();
        }
    }

    /** Home method for third version checkout command */
    public static void branchCheckout(String branch) {
        HashMap<String, String> heads = readData(HEADS_FILE);
        if (!heads.containsKey(branch)) {
            secondBranchCheckoutError();
            return;
        }
        if (heads.get(DUMMY).equals(branch)) {
            firstBranchCheckoutError();
            return;
        }
        Commit nextHead = readCommit(heads.get(branch));
        String[] files = CWD.list();
        if (untrackedExists(nextHead)) {
            thirdBranchCheckoutError();
            return;
        }
        for (String name : files) {
            File f = join(CWD, name);
            String filepath = f.getPath();
            if (!nextHead.blobExists(filepath)) {
                f.delete();
            }
        }
        for (String s : nextHead.files().keySet()) {
            File f = join(s);
            if (!f.exists()) {
                makeFile(f);
            }
            Blob x = readBlob(nextHead.get(s));
            String thingToWrite = x.contents();
            writeContents(f, thingToWrite);
        }
        heads.put(DUMMY, branch);
        clearStage();
        writeObject(HEADS_FILE, heads);
    }

    /** Home method for log command */
    public static String log() {
        String log = "";
        Commit c;
        String nextHash = headHash();
        while (nextHash != null) {
            c = readCommit(nextHash);
            log += "===\n";
            log += "commit " + nextHash + "\n";
            log += mergeLog(c);
            log += "Date: " + c.dateString() + "\n";
            log += c.message() + "\n\n";
            nextHash = c.parent();
        }
        return log.substring(0, log.length() - 1);
    }

    /** Home method for global-log command */
    public static String globalLog() {
        List<String> commits = new ArrayList<>(Utils.plainFilenamesIn(COMMIT_DIR));
        String log = "";
        Commit c;
        String nextHash = "";
        while (commits.size() > 0) {
            nextHash = commits.remove(0);
            c = readCommit(nextHash);
            log += "===\n";
            log += "commit " + nextHash + "\n";
            log += mergeLog(c);
            log += "Date: " + c.dateString() + "\n";
            log += c.message() + "\n\n";
        }
        return log.substring(0, log.length() - 1);
    }

    /** Home method for find command */
    public static String find(String message) {
        List<String> commits = new ArrayList<>(Utils.plainFilenamesIn(COMMIT_DIR));
        String rv = "";
        boolean found = false;
        Commit c;
        String nextHash = "";
        while (commits.size() > 0) {
            nextHash = commits.remove(0);
            c = readCommit(nextHash);
            if (c.message().equals(message)) {
                rv += nextHash + "\n";
                found = true;
            }
        }
        if (!found) {
            return "Found no commit with that message.";
        }
        return rv.substring(0, rv.length() - 1);
    }

    /** Home method for branch command */
    public static void branch(String branchName) {
        HashMap<String, String> h = readData(HEADS_FILE);
        if (h.containsKey(branchName)) {
            makeBranchError();
        } else {
            h.put(branchName, headHash());
            Utils.writeObject(HEADS_FILE, h);
        }
    }

    /** Home method for rm-branch command */
    public static void removeBranch(String branchName) {
        HashMap<String, String> h = readData(HEADS_FILE);
        if (!h.containsKey(branchName)) {
            removeBranchDNE();
        } else if (h.get(DUMMY).equals(branchName)) {
            removeBranchError();
        } else {
            h.remove(branchName);
            Utils.writeObject(HEADS_FILE, h);
        }
    }

    /** Home method for merge command */
    public static void merge(String givenBranch) {
        HashMap<String, String> heads = readData(HEADS_FILE);
        Commit curr = readHead();
        if (stagedStuffExists()) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (!heads.containsKey(givenBranch)) {
            removeBranchDNE();
            return;
        } else if (givenBranch.equals(heads.get(DUMMY))) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit other = readCommit(heads.get(givenBranch));
        if (untrackedExists(other)) {
            thirdBranchCheckoutError();
            return;
        }
        String splitHash = Commit.findSplitPoint(headHash(), heads.get(givenBranch));
        Commit split = readCommit(splitHash);
        if (splitHash.equals(heads.get(givenBranch))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (splitHash.equals(headHash())) {
            branchCheckout(givenBranch);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        TreeSet<String> modCurrent = (TreeSet<String>) Commit.modifiedExistingFiles(split, curr);
        TreeSet<String> modOther = (TreeSet<String>) Commit.modifiedExistingFiles(split, other);
        TreeSet<String> overlaps = (TreeSet<String>) Commit.existingOverlaps(curr, other);
        for (String filepath : overlaps) {
            if (modOther.contains(filepath) && !modCurrent.contains(filepath)) {
                String filename = nameFromPath(filepath, CWD);
                checkout(filename, heads.get(givenBranch));
                addToStage(filename);
            }
        }

        for (String filepath : other.files().keySet()) {
            if (!split.files().containsKey(filepath) && !curr.files().containsKey(filepath)) {
                String filename = nameFromPath(filepath, CWD);
                checkout(filename, heads.get(givenBranch));
                addToStage(filename);
            }
        }

        for (String filepath : Commit.sameVersionFiles(split, curr)) {
            if (!other.files().containsKey(filepath)) {
                remove(nameFromPath(filepath, CWD));
            }
        }
        mergeConflicts(inConflictFiles(split, curr, other), curr, other);
        String s = "Merged " + givenBranch + " into " + heads.get(DUMMY) + ".";
        commit(s, heads.get(givenBranch));
    }

    /** Home method for status command */
    public static String status() {
        String status = "";
        HashMap<String, String> heads = readData(HEADS_FILE);
        String currBranch = heads.get(DUMMY);
        Set<String> branches = heads.keySet();
        TreeSet<String> b2 = new TreeSet<>(branches);
        b2.remove(DUMMY);
        status += "=== Branches ===\n";
        for (String branch: b2) {
            if (branch.equals(currBranch)) {
                branch = "*" + branch;
            }
            status += branch + "\n";
        }
        status += "\n=== Staged Files ===\n";
        for (String filepath : new TreeSet<>(readData(STAGE_FILE).keySet())) {
            status += nameFromPath(filepath, CWD) + "\n";
        }
        status += "\n=== Removed Files ===\n";
        TreeSet<String> rms = new TreeSet<>(readData(REMOVE_FILE).keySet());
        for (String filepath : rms) {
            status += nameFromPath(filepath, CWD) + "\n";
        }
        status += "\n=== Modifications Not Staged For Commit ===\n";
        status += modNotStaged();
        status += "\n=== Untracked Files ===\n";
        status += untrackedStatus();
        return status;
    }

    /** Home method for reset command */
    public static void reset(String id) {
        Commit c = findCommit(id);
        if (c == null) {
            secondCheckoutError();
            return;
        }
        List<String> filenames = plainFilenamesIn(CWD);
        if (untrackedExists(c)) {
            thirdBranchCheckoutError();
            return;
        }
        for (String filepath : c.files().keySet()) {
            String filename = nameFromPath(filepath, CWD);
            checkout(filename, id);
        }
        for (String cwdFile : filenames) {
            String filepath = join(CWD, cwdFile).getPath();
            if (!c.files().containsKey(filepath)) {
                remove(cwdFile);
            }
        }
        clearStage();
        clearRemovals();
        HashMap<String, String> heads = readData(HEADS_FILE);
        heads.put(heads.get(DUMMY), id);
        writeObject(HEADS_FILE, heads);
    }

    /** Error message */
    public static void firstCheckoutError() {
        System.out.println("File does not exist in that commit.");
    }

    /** Error message */
    public static void secondCheckoutError() {
        System.out.println("No commit with that id exists.");
    }

    /** Error message */
    public static void firstBranchCheckoutError() {
        System.out.println("No need to checkout the current branch.");
    }

    /** Error message */
    public static void secondBranchCheckoutError() {
        System.out.println("No such branch exists.");
    }

    /** Error message */
    public static void thirdBranchCheckoutError() {
        String s = "delete it, or add and commit it first.";
        System.out.println("There is an untracked file in the way; " + s);
    }

    /** Error message */
    public static void makeBranchError() {
        System.out.println("A branch with that name already exists.");
    }

    /** Error message */
    public static void removeBranchDNE() {
        System.out.println("A branch with that name does not exist.");
    }

    public static void removeBranchError() {
        System.out.println("Cannot remove the current branch.");
    }

    /** Reads current head commit, returns it as a commit object. */
    private static Commit readHead() {
        String hash = headHash();
        return Utils.readObject(Utils.join(COMMIT_DIR, hash), Commit.class);
    }

    /** Returns a blob given a blob hash */
    private static Blob readBlob(String bHash) {
        if (bHash == null) {
            return null;
        }
        return readObject(join(BLOB_DIR, bHash), Blob.class);
    }

    /** Returns the head hash */
    private static String headHash() {
        HashMap<String, String> h = readData(HEADS_FILE);
        return h.get(h.get(DUMMY));
    }

    /** I have stored the heads, stage, and removal stage as HashMaps.
     * As a result, any of them can be read using this function. */
    private static HashMap<String, String> readData(File f) {
        return Utils.readObject(f, HashMap.class);
    }

    /** Given commit hash, reads commit and returns */
    public static Commit readCommit(String commitHash) {
        return Utils.readObject(Utils.join(COMMIT_DIR, commitHash), Commit.class);
    }

    /** Given commit hash, searches commit directory. If found, reads commit
     * and returns. Otherwise, returns null */
    public static Commit findCommit(String id) {
        File f = Utils.join(COMMIT_DIR, id);
        if (f.exists()) {
            Commit c = readObject(f, Commit.class);
            return c;
        }
        return null;
    }

    /** Simple helper method to create a file. */
    public static void makeFile(File f) {
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Returns whether a given file exists in the CWD. Used in Main. */
    public static boolean validFileCWD(String filename) {
        File f = Utils.join(CWD, filename);
        return f.exists();
    }

    /** Returns the filename from a filepath */
    public static String nameFromPath(String filepath, File directory) {
        String dirStr = directory.getPath();
        return filepath.substring(dirStr.length() + 1, filepath.length());
    }

    /** Clears the stage, writes to file. */
    public static void clearStage() {
        HashMap<String, String> stage = readData(STAGE_FILE);
        stage.clear();
        writeObject(STAGE_FILE, stage);
    }

    /** Clears the removal stage, writes to file. */
    public static void clearRemovals() {
        HashMap<String, String> removals = readData(REMOVE_FILE);
        removals.clear();
        writeObject(REMOVE_FILE, removals);
    }

    /** Returns text to write to conflicted file. */
    public static String mergeConflictText(String bHash1, String bHash2) {
        Blob b1 = readBlob(bHash1);
        Blob b2 = readBlob(bHash2);
        String b1c = "";
        String b2c = "";
        if (b1 != null) {
            b1c = b1.contents();
        }
        if (b2 != null) {
            b2c = b2.contents();
        }
        String s = "<<<<<<< HEAD\n" + b1c + "=======\n" + b2c + ">>>>>>>\n";
        return s;
    }

    /** Returns a set of files in conflict in two commits. */
    public static Set<String> inConflictFiles(Commit split, Commit curr, Commit other) {
        HashSet<String> conflicts = new HashSet<>();
        for (String filepath : split.fileKeys()) {
            String splitPath = split.get(filepath);
            String currPath = curr.get(filepath);
            String otherPath = other.get(filepath);
            boolean oneExists = !(currPath == null && otherPath == null);
            if (oneExists) {
                if (!splitPath.equals(currPath) && otherPath == null) {
                    conflicts.add(filepath);
                }
                if (!splitPath.equals(otherPath) && currPath == null) {
                    conflicts.add(filepath);
                }
                if (currPath != null && otherPath != null) {
                    boolean deviate = !splitPath.equals(currPath) && !splitPath.equals(otherPath);
                    if (deviate && !otherPath.equals(currPath)) {
                        conflicts.add(filepath);
                    }
                }
            }
        }
        for (String filepath : Commit.modifiedExistingFiles(curr, other)) {
            String currPath = curr.get(filepath);
            String otherPath = other.get(filepath);
            if (split.get(filepath) == null && !otherPath.equals(currPath)) {
                conflicts.add(filepath);
            }
        }
        return conflicts;
    }

    /** Method to handle merge conflicts. */
    public static void mergeConflicts(Set<String> conflicts, Commit curr, Commit other) {
        for (String filepath : conflicts) {
            String text = mergeConflictText(curr.get(filepath), other.get(filepath));
            writeContents(new File(filepath), text);
            addToStage(nameFromPath(filepath, CWD));
        }
        if (conflicts.size() > 0) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Returns shortened version of a hash for log of merge commits. */
    private static String compactHash(String hash) {
        return hash.substring(0, 7);
    }

    /** Returns text for log when merge commit is present. */
    private static String mergeLog(Commit c) {
        String log = "";
        if (c.isMerge()) {
            log += "Merge: " + compactHash(c.parent()) + " " + compactHash(c.secondParent()) + "\n";
        }
        return log;
    }

    /** Based off of Ed post #3433. Decides if there are untracked files
     * in the head commit given the given commit. */
    public static boolean untrackedExists(Commit c) {
        Commit head = readHead();
        for (String filename : plainFilenamesIn(CWD)) {
            File f = join(CWD, filename);
            String filepath = f.getPath();
            String filestuff = readContentsAsString(f);
            Blob b = readBlob(c.get(filepath));
            if (!head.blobExists(filepath) && c.blobExists(filepath)
                    && !filestuff.equals(b.contents())) {
                return true;
            }
        }
        return false;
    }

    /** Decides if there are files in the stage. */
    public static boolean stagedStuffExists() {
        if (readData(STAGE_FILE).size() + readData(REMOVE_FILE).size() > 0) {
            return true;
        }
        return false;
    }

    /** Returns large UID given abbreviated version.
     * If DNE, returns dummy UID that (probably) won't collide */
    public static String findLargeUID(String smallUID) {
        if (smallUID.length() >= 40) {
            return smallUID;
        }
        for (String uid : plainFilenamesIn(COMMIT_DIR)) {
            if (smallUID.equals(uid.substring(0, smallUID.length()))) {
                return uid;
            }
        }
        return "INVALIDINVALIDINVALID";
    }

    public static String modNotStaged() {
        TreeSet<String> returns = new TreeSet<>();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        HashMap<String, String> stage = readData(STAGE_FILE);
        HashMap<String, String> removals = readData(REMOVE_FILE);
        Commit head = readHead();
        String returnString = "";
        for (String filepath : stage.keySet()) {
            File f = join(filepath);
            String stageConts = readBlob(stage.get(filepath)).contents();
            if (f.exists() && !stageConts.equals(readContentsAsString(f))) {
                returns.add(filepath + " (modified)");
            } else if (!f.exists()) {
                returns.add(filepath + " (deleted)");
            }
        }
        for (String filepath : head.fileKeys()) {
            String s = head.get(filepath);
            File f = join(filepath);
            boolean x = !stage.containsKey(filepath);
            if (f.exists() && !readBlob(s).contents().equals(readContentsAsString(f)) && x) {
                returns.add(filepath + " (modified)");
            } else if (!removals.containsKey(filepath) && !f.exists()) {
                returns.add(filepath + " (deleted)");
            }
        }
        for (String f : returns) {
            returnString += nameFromPath(f, CWD) + "\n";
        }
        return returnString;
    }

    public static String untrackedStatus() {
        TreeSet<String> returns = new TreeSet<>();
        HashMap<String, String> stage = readData(STAGE_FILE);
        HashMap<String, String> removals = readData(REMOVE_FILE);
        Commit head = readHead();
        String s = "";
        for (String filename : plainFilenamesIn(CWD)) {
            File f = join(CWD, filename);
            String filepath = f.getPath();
            String filestuff = readContentsAsString(f);
            if (!head.blobExists(filepath) && !stage.containsKey(filepath)) {
                returns.add(filepath);
            }
        }
        for (String filepath : removals.keySet()) {
            if (join(filepath).exists()) {
                returns.add(filepath);
            }
        }
        for (String f : returns) {
            s += nameFromPath(f, CWD) + "\n";
        }
        return s;
    }

}
