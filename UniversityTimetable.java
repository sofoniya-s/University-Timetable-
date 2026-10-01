import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * University Timetable
 * A clean timetable layout for organizing weekly university class schedules.
 *
 * Compile: javac UniversityTimetable.java
 * Run:     java UniversityTimetable
 * Requires Java 16+ (uses records).
 */
public class UniversityTimetable {

    enum Day { MON, TUE, WED, THU, FRI, SAT }

    record ClassSession(String course, String room, Day day, int start, int end) {
        boolean overlaps(ClassSession o) {
            return day == o.day && start < o.end && o.start < end;
        }
    }

    static class Timetable {
        private static final int FIRST_HOUR = 8;   // 08:00
        private static final int LAST_HOUR = 18;   // grid ends at 18:00
        private static final int TIME_W = 7;
        private static final int CELL_W = 15;

        private final List<ClassSession> sessions = new ArrayList<>();

        /** Adds a class if it is valid and does not clash with another one. */
        String add(ClassSession s) {
            if (s.start() < FIRST_HOUR || s.end() > LAST_HOUR || s.start() >= s.end()) {
                return "Invalid time. Use hours between " + FIRST_HOUR + " and " + LAST_HOUR
                        + ", with start before end.";
            }
            for (ClassSession other : sessions) {
                if (s.overlaps(other)) {
                    return "Clash with " + other.course() + " (" + other.day() + " "
                            + other.start() + ":00-" + other.end() + ":00).";
                }
            }
            sessions.add(s);
            return null; // success
        }

        boolean remove(String course, Day day) {
            return sessions.removeIf(s -> s.course().equalsIgnoreCase(course) && s.day() == day);
        }

        private ClassSession at(Day d, int hour) {
            for (ClassSession s : sessions) {
                if (s.day() == d && hour >= s.start() && hour < s.end()) return s;
            }
            return null;
        }

        private static String center(String text, int width) {
            if (text.length() > width) text = text.substring(0, width);
            int left = (width - text.length()) / 2;
            return " ".repeat(left) + text + " ".repeat(width - text.length() - left);
        }

        void print() {
            Day[] days = Day.values();
            String line = "+" + "-".repeat(TIME_W) + ("+" + "-".repeat(CELL_W)).repeat(days.length) + "+";

            System.out.println(line);
            StringBuilder header = new StringBuilder("|" + center("Time", TIME_W));
            for (Day d : days) header.append("|").append(center(d.name(), CELL_W));
            System.out.println(header.append("|"));
            System.out.println(line);

            for (int h = FIRST_HOUR; h < LAST_HOUR; h++) {
                StringBuilder top = new StringBuilder("|" + center(String.format("%02d:00", h), TIME_W));
                StringBuilder bottom = new StringBuilder("|" + " ".repeat(TIME_W));

                for (Day d : days) {
                    ClassSession s = at(d, h);
                    String t = "", b = "";
                    if (s != null) {
                        if (h == s.start()) {
                            t = s.course();
                            b = s.room();
                        } else {
                            t = "  \" \"";
                        }
                    }
                    top.append("|").append(center(t, CELL_W));
                    bottom.append("|").append(center(b, CELL_W));
                }
                System.out.println(top.append("|"));
                System.out.println(bottom.append("|"));
                System.out.println(line);
            }
        }

        void printList() {
            if (sessions.isEmpty()) {
                System.out.println("No classes yet.");
                return;
            }
            sessions.stream()
                    .sorted(Comparator.comparing(ClassSession::day).thenComparing(ClassSession::start))
                    .forEach(s -> System.out.printf("%s  %02d:00-%02d:00  %-12s %s%n",
                            s.day(), s.start(), s.end(), s.course(), s.room()));
        }
    }

    // ---------- Console menu ----------

    private static Day readDay(Scanner in) {
        while (true) {
            System.out.print("Day (MON/TUE/WED/THU/FRI/SAT): ");
            try {
                return Day.valueOf(in.nextLine().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter a valid day.");
            }
        }
    }

    private static int readHour(Scanner in, String label) {
        while (true) {
            System.out.print(label + " (hour, e.g. 9): ");
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    public static void main(String[] args) {
        Timetable tt = new Timetable();

        // Sample data so the timetable is not empty on first run
        tt.add(new ClassSession("Automata", "Room 101", Day.MON, 8, 10));
        tt.add(new ClassSession("Java Prog.", "Lab 3", Day.TUE, 10, 12));
        tt.add(new ClassSession("Mathematics", "Room 204", Day.WED, 9, 11));
        tt.add(new ClassSession("English", "Room 110", Day.THU, 14, 15));
        tt.add(new ClassSession("Java Prog.", "Lab 3", Day.FRI, 8, 10));

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== UNIVERSITY TIMETABLE ===");
            System.out.println("1. View timetable");
            System.out.println("2. Add class");
            System.out.println("3. Remove class");
            System.out.println("4. List classes");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            switch (in.nextLine().trim()) {
                case "1" -> tt.print();
                case "2" -> {
                    System.out.print("Course name: ");
                    String course = in.nextLine().trim();
                    System.out.print("Room: ");
                    String room = in.nextLine().trim();
                    Day day = readDay(in);
                    int start = readHour(in, "Start");
                    int end = readHour(in, "End");
                    String error = tt.add(new ClassSession(course, room, day, start, end));
                    System.out.println(error == null ? "Class added." : "Could not add: " + error);
                }
                case "3" -> {
                    System.out.print("Course name to remove: ");
                    String course = in.nextLine().trim();
                    Day day = readDay(in);
                    System.out.println(tt.remove(course, day) ? "Class removed." : "No matching class found.");
                }
                case "4" -> tt.printList();
                case "5" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
