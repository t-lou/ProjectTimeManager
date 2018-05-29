public class Main {

    public static void main(String[] args) {
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
