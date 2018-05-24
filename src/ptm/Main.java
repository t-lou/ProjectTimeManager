package ptm;

public class Main {

    public static void main(String[] args) {
        // MainFrame gui = new MainFrame();
        if (args.length == 0)
        {
            CommandParser.printHelp();
        }
        else
        {
            CommandParser.parseCommand(args);
        }
    }
}
