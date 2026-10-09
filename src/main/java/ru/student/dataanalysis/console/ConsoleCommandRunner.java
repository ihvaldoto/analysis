package ru.student.dataanalysis.console;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.student.dataanalysis.model.DataPoint;
import ru.student.dataanalysis.model.DataSet;
import ru.student.dataanalysis.model.User;
import ru.student.dataanalysis.service.DataAnalysisService;
import ru.student.dataanalysis.service.DataPointService;
import ru.student.dataanalysis.service.DataSetService;
import ru.student.dataanalysis.service.UserService;

import java.util.Scanner;

@Component
public class ConsoleCommandRunner implements CommandLineRunner {
    private final UserService userService;
    private final DataSetService dataSetService;
    private final DataPointService dataPointService;
    private final DataAnalysisService analysisService;
    private volatile User currentUser;
    private volatile boolean running = true;

    public ConsoleCommandRunner(UserService userService, DataSetService dataSetService,
                               DataPointService dataPointService, DataAnalysisService analysisService) {
        this.userService = userService;
        this.dataSetService = dataSetService;
        this.dataPointService = dataPointService;
        this.analysisService = analysisService;
    }

    @Override
    public void run(String... args) {
        Thread consoleThread = new Thread(this::runConsole, "data-analysis-console");
        consoleThread.start();
    }

    private void runConsole() {
        printHelp();
        try (Scanner scanner = new Scanner(System.in)) {
            while (running && scanner.hasNextLine()) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;
                try {
                    process(input);
                } catch (RuntimeException exception) {
                    System.out.println("Error: " + exception.getMessage());
                }
            }
        }
    }

    private void process(String input) {
        String[] args = input.split("\\s+");
        switch (args[0].toLowerCase()) {
            case "help" -> printHelp();
            case "exit" -> {
                running = false;
                System.out.println("Console closed.");
            }
            case "register" -> register(args);
            case "login" -> login(args);
            case "logout" -> logout();
            case "users" -> users(args);
            case "sets" -> dataSets(args);
            case "points" -> dataPoints(args);
            case "analyze" -> analyze(args);
            default -> System.out.println("Unknown command. Type 'help'.");
        }
    }

    private void register(String[] args) {
        requireArgs(args, 4, "register <login> <password> <displayName>");
        User user = userService.register(args[1], args[2], args[3]);
        System.out.println("Registered: " + user);
    }

    private void login(String[] args) {
        requireArgs(args, 3, "login <login> <password>");
        currentUser = userService.authenticate(args[1], args[2]);
        System.out.println("Signed in as " + currentUser.getDisplayName() + " (id=" + currentUser.getId() + ").");
    }

    private void logout() {
        currentUser = null;
        System.out.println("Signed out.");
    }

    private void users(String[] args) {
        requireMinArgs(args, 2, "users <list|get|rename|delete> ...");
        switch (args[1].toLowerCase()) {
            case "list" -> userService.findAll().forEach(System.out::println);
            case "get" -> {
                requireArgs(args, 3, "users get <id>");
                System.out.println(userService.findById(Long.parseLong(args[2])));
            }
            case "rename" -> {
                requireArgs(args, 3, "users rename <displayName>");
                User user = requireLogin();
                userService.updateDisplayName(user.getId(), args[2]);
                user.setDisplayName(args[2]);
                System.out.println("Profile updated.");
            }
            case "delete" -> {
                User user = requireLogin();
                userService.delete(user.getId());
                currentUser = null;
                System.out.println("Account deleted.");
            }
            default -> System.out.println("Unknown users command.");
        }
    }

    private void dataSets(String[] args) {
        requireLogin();
        requireMinArgs(args, 2, "sets <list|create|get|update|delete> ...");
        switch (args[1].toLowerCase()) {
            case "list" -> dataSetService.findByOwner(currentUser.getId()).forEach(System.out::println);
            case "create" -> {
                requireArgs(args, 4, "sets create <name> <description>");
                System.out.println("Created: " + dataSetService.create(args[2], args[3], currentUser.getId()));
            }
            case "get" -> {
                requireArgs(args, 3, "sets get <id>");
                System.out.println(ownedDataSet(Long.parseLong(args[2])));
            }
            case "update" -> {
                requireArgs(args, 5, "sets update <id> <name> <description>");
                long id = Long.parseLong(args[2]);
                ownedDataSet(id);
                dataSetService.update(id, args[3], args[4], currentUser.getId());
                System.out.println("Data set updated.");
            }
            case "delete" -> {
                requireArgs(args, 3, "sets delete <id>");
                dataSetService.delete(Long.parseLong(args[2]), currentUser.getId());
                System.out.println("Data set deleted.");
            }
            default -> System.out.println("Unknown sets command.");
        }
    }

    private void dataPoints(String[] args) {
        requireLogin();
        requireMinArgs(args, 2, "points <list|add|get|update|delete> ...");
        switch (args[1].toLowerCase()) {
            case "list" -> dataPointService.findByDataSet(ownedDataSetId(args, 2)).forEach(System.out::println);
            case "add" -> {
                requireArgs(args, 6, "points add <dataSetId> <metric> <value> <unit>");
                long dataSetId = Long.parseLong(args[2]);
                ownedDataSet(dataSetId);
                DataPoint point = dataPointService.create(dataSetId, args[3], Double.parseDouble(args[4]), args[5]);
                System.out.println("Created: " + point);
            }
            case "get" -> {
                requireArgs(args, 3, "points get <id>");
                DataPoint point = dataPointService.findById(Long.parseLong(args[2]));
                ownedDataSet(point.getDataSetId());
                System.out.println(point);
            }
            case "update" -> {
                requireArgs(args, 7, "points update <id> <dataSetId> <metric> <value> <unit>");
                long pointId = Long.parseLong(args[2]);
                DataPoint current = dataPointService.findById(pointId);
                ownedDataSet(current.getDataSetId());
                long dataSetId = Long.parseLong(args[3]);
                ownedDataSet(dataSetId);
                dataPointService.update(pointId, dataSetId, args[4], Double.parseDouble(args[5]), args[6]);
                System.out.println("Data point updated.");
            }
            case "delete" -> {
                requireArgs(args, 3, "points delete <id>");
                long pointId = Long.parseLong(args[2]);
                ownedDataSet(dataPointService.findById(pointId).getDataSetId());
                dataPointService.delete(pointId);
                System.out.println("Data point deleted.");
            }
            default -> System.out.println("Unknown points command.");
        }
    }

    private void analyze(String[] args) {
        requireLogin();
        requireArgs(args, 3, "analyze <dataSetId> <metric>");
        long dataSetId = Long.parseLong(args[1]);
        ownedDataSet(dataSetId);
        System.out.println(analysisService.format(dataSetId, args[2]));
    }

    private long ownedDataSetId(String[] args, int index) {
        requireArgs(args, index + 1, "points list <dataSetId>");
        long dataSetId = Long.parseLong(args[index]);
        ownedDataSet(dataSetId);
        return dataSetId;
    }

    private DataSet ownedDataSet(long id) {
        DataSet dataSet = dataSetService.findById(id);
        if (!dataSet.getOwnerId().equals(requireLogin().getId())) {
            throw new IllegalArgumentException("This data set belongs to another user");
        }
        return dataSet;
    }

    private User requireLogin() {
        if (currentUser == null) throw new IllegalStateException("Sign in first with 'login <login> <password>'");
        return currentUser;
    }

    private static void requireMinArgs(String[] args, int count, String usage) {
        if (args.length < count) throw new IllegalArgumentException("Usage: " + usage);
    }

    private static void requireArgs(String[] args, int count, String usage) {
        if (args.length != count) throw new IllegalArgumentException("Usage: " + usage);
    }

    private static void printHelp() {
        System.out.println("Data Analysis console. Arguments with spaces should be written with underscores.");
        System.out.println("register <login> <password> <displayName> | login <login> <password> | logout | exit");
        System.out.println("users list | users get <id> | users rename <displayName> | users delete");
        System.out.println("sets list | sets create <name> <description> | sets get <id>");
        System.out.println("sets update <id> <name> <description> | sets delete <id>");
        System.out.println("points list <dataSetId> | points add <dataSetId> <metric> <value> <unit>");
        System.out.println("points get <id> | points update <id> <dataSetId> <metric> <value> <unit> | points delete <id>");
        System.out.println("analyze <dataSetId> <metric> | help");
    }
}
