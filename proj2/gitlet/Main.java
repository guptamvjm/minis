package gitlet;

import java.io.File;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author M.Gupta
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init")) {
            if (args.length == 1 && !Repository.repoExists()) {
                Repository.init();
            } else if (args.length != 1) {
                System.out.println("Incorrect operands.");
            } else {
                System.out.println("A Gitlet version-control system already exists in the current directory.");
            }
        } else {
            String firstArg = args[0];
            if (!Repository.GITLET_DIR.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                System.exit(0);
            }
            switch(firstArg) {
                case "add":
                    if (args.length == 2 && Repository.validFileCWD(args[1])) {
                        Repository.addToStage(args[1]);
                    } else if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                    } else {
                        System.out.println("File does not exist.");
                    }
                    break;
                case "commit":
                    if (args.length == 2) {
                        if (args[1].length() == 0) {
                            System.out.println("Please enter a commit message.");
                        } else {
                            Repository.commit(args[1]);
                        }
                    } else if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "checkout":
                    if (args.length == 3) {
                        Repository.checkout(args[2]);
                    } else if (args.length == 4 && args[2].equals("--")) {
                        Repository.checkout(args[3], Repository.findLargeUID(args[1]));
                    } else if (args.length == 2) {
                        Repository.branchCheckout(args[1]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "log":
                    if (args.length == 1) {
                        System.out.println(Repository.log());
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "branch":
                    if (args.length == 2) {
                        Repository.branch(args[1]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "rm":
                    if (args.length == 2) {
                        Repository.remove(args[1]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "rm-branch":
                    if (args.length == 2) {
                        Repository.removeBranch(args[1]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "global-log":
                    if (args.length == 1) {
                        System.out.println(Repository.globalLog());
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "find":
                    if (args.length == 2) {
                        String s = args[1];
                        System.out.println(Repository.find(s));
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "status":
                    if (args.length == 1) {
                        System.out.println(Repository.status());
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "reset":
                    if (args.length == 2) {
                        Repository.reset(Repository.findLargeUID(args[1]));
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "merge":
                    if (args.length == 2) {
                        Repository.merge(args[1]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }

    }
}
