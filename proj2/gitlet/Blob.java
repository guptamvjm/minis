package gitlet;

import java.io.File;
import java.io.Serializable;

/** Represents a blob object.
 *  A blob represents any file.
 *  @author M. Gupta
 */

public class Blob implements Serializable {
    private String fileName;
    private String contents;

    public Blob(String n, String c) {
        fileName = n;
        contents = c;
    }

    public Blob(String filePath) {
        fileName = filePath;
        File f = new File(filePath);
        contents = Utils.readContentsAsString(f);
    }

    public String contents() {
        return contents;
    }

}
