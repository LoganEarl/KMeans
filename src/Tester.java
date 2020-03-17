import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Tester {
    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        System.out.println("Welcome to a KMeans Clustering model by Logan Earl");

        String rawFile = "";
        while (rawFile.isEmpty()) {
            System.out.print("Enter the name of the file containing sequence data:");
            String fileName = kb.nextLine();
            try{
                FileInputStream fin = new FileInputStream(new File(fileName));
                rawFile = new String(fin.readAllBytes());
                fin.close();
                if(rawFile.isEmpty())
                    System.out.println("That file is empty. Please read from a non-empty file");
            }catch(FileNotFoundException e){
                System.out.println("Could not find that file. Please ensure the file in question is either in the root directory of the project, or you are referencing the file via it's full file path.");
            }catch(Exception e){
                System.out.println("Failed to read file data. Are you sure it is formatted correctly? Perhaps the file is in a directory that this program is not authorized to access?");
            }
        }

        KMeans KMeans = new KMeans(6, " ");
        KMeans.loadData(rawFile);
        KMeans.calculateCentroids();
        KMeans.saveData("class");

    }
}
