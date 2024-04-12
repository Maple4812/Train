import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TempReservation implements FileInterface{
    private static boolean timer = false;
    private String filename;
    ArrayList<List<String>> tempList = new ArrayList<>();

    public void init() {
    }

    public void repos(String file) throws FileNotFoundException {
        Scanner scan = new Scanner(new File(file));
        while(scan.hasNext()) {
            String[] temp = scan.nextLine().split(",");
            List<String> list = Arrays.asList(temp);
            tempList.add(list);
        }
    }

    public static boolean isTimerOn() {
        return timer;
    }
}
