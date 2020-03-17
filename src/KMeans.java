import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class KMeans {
    private List<Set<Entry>> classes;
    private Set<Entry> unsortedData;
    private Entry[] centroids;

    private int dataLength = 1;

    private String entrySeparator;

    public KMeans(int numClasses, String entrySeparator) {
        if (numClasses <= 0)
            throw new IllegalArgumentException("Invalid number of classes.");

        this.entrySeparator = entrySeparator;

        classes = new ArrayList<>();
        for (int i = 0; i < numClasses; i++)
            classes.add(new HashSet<>());
        centroids = new Entry[numClasses];

        unsortedData = new HashSet<>();
    }

    public void loadData(String rawFileInput) {
        Scanner input = new Scanner(rawFileInput);

        unsortedData.clear();

        if (input.hasNextLine()) {
            String firstLine = input.nextLine();
            Entry firstEntry;
            try {
                firstEntry = new Entry(-1, firstLine, entrySeparator, 0);
            } catch (Exception e) {
                throw new IllegalArgumentException("Passed invalid dataSet to loadData()");
            }
            unsortedData.add(firstEntry);
            dataLength = firstEntry.values.length;
        }

        int lineIndex = 1;
        while (input.hasNextLine()) {
            String line = "";
            try {
                line = input.nextLine();
                unsortedData.add(new Entry(dataLength, line, entrySeparator, lineIndex));
            } catch (Exception e) {
                System.out.println("Failed to read line:" + line);
            }
            lineIndex++;
        }
    }

    public void saveData(String fileNamePrefix){
        int index = 0;
        for(Set<Entry> entries: classes){
            index++;
            ArrayList<Entry> sortedEntries = new ArrayList<>(entries);
            Collections.sort(sortedEntries);
            try {
                FileOutputStream fout = new FileOutputStream(fileNamePrefix + index + ".data");
                boolean firstLine = true;
                for(Entry e: sortedEntries){
                    if(firstLine)
                        firstLine = false;
                    else
                        fout.write('\n');

                    boolean firstNumber = true;
                    for(int i = 0; i < e.values.length; i++) {
                        if (firstNumber)
                            firstNumber = false;
                        else
                            fout.write(' ');
                        fout.write(String.valueOf(e.values[i]).getBytes());
                    }
                }
                fout.close();
            }catch(Exception ignored){

            }
        }
    }

    public void calculateCentroids() {
        if (unsortedData.size() > 0) {
            System.out.println("Initializing centroids");
            int dataLength = unsortedData.iterator().next().values.length;

            List<Entry> orderedData = new ArrayList<>(unsortedData);

            Random random = new Random();
            for (int i = 0; i < centroids.length; i++) {
                centroids[i] = new Entry(orderedData.get(random.nextInt(orderedData.size())).values);
            }

            System.out.println("Moving centroids");
            boolean moved = true;
            while (moved) {
                classes = classifyCentroids(classes.size(), unsortedData, centroids);
                moved = advanceCentroids(centroids, dataLength, classes);
                if (moved) {
                    System.out.println("Centroids adjusted:");
                    for (Set<Entry> aClass : classes) {
                        List<Entry> bin = new ArrayList<>(aClass);
                        Collections.sort(bin);
                        System.out.print("Centroid: members = " + bin.size() + "\n\t");
                        for (Entry e : bin)
                            System.out.print(e.originalIndex + " ");
                        System.out.print("\n\n");
                    }
                }

            }
        } else
            System.out.println("No classification needed, data is empty");
        System.out.println("Task Complete");
    }

    private static boolean advanceCentroids(Entry[] centroids, int centroidLength, List<Set<Entry>> sortedData) {
        boolean moved = false;
        for (int centroidIndex = 0; centroidIndex < centroids.length; centroidIndex++) {
            Entry newCentroid = averageEntries(centroidLength, sortedData.get(centroidIndex));
            if (!newCentroid.equals(centroids[centroidIndex])) {
                moved = true;
                centroids[centroidIndex] = newCentroid;
            }
        }
        return moved;
    }

    private static List<Set<Entry>> classifyCentroids(int numClasses, Set<Entry> unsortedData, Entry[] centroids) {
        List<Set<Entry>> newClasses = new ArrayList<>();
        for (int i = 0; i < numClasses; i++)
            newClasses.add(new HashSet<>());

        for (Entry entry : unsortedData) {
            int nearestIndex = 0;
            double minDistance = centroids[0].minkowskiDistance(entry, 2);  //euclidean distance for now
            for (int centroidIndex = 1; centroidIndex < centroids.length; centroidIndex++) {
                double distance = centroids[centroidIndex].minkowskiDistance(entry, 2);
                if (distance < minDistance) {
                    nearestIndex = centroidIndex;
                    minDistance = distance;
                }
            }
            newClasses.get(nearestIndex).add(entry);
        }

        return newClasses;
    }

    public List<Set<Entry>> getClasses() {
        return classes;
    }

    public Entry[] getCentroids() {
        return centroids;
    }

    public static class Entry implements Comparable<Entry> {
        private double[] values;
        private int originalIndex = 0;

        private Entry(int size) {
            this.values = new double[size];
        }

        private Entry(double[] values) {
            this.values = Arrays.copyOf(values,values.length);

        }

        private Entry(int expectedSize, String rawEntry, String entrySeparator, int originalIndex) throws Exception {
            rawEntry = rawEntry.replaceAll(" {4}", " ");
            rawEntry = rawEntry.replaceAll(" {3}", " ");
            rawEntry = rawEntry.replaceAll(" {2}", " ");

            this.originalIndex = originalIndex;

            String[] line = rawEntry.split(entrySeparator);
            int dataLength = line.length;
            if (expectedSize == -1 || dataLength == expectedSize) {
                values = new double[dataLength];
                for (int i = 0; i < dataLength; i++)
                    values[i] = Double.parseDouble(line[i]);
            }
        }

        private void add(Entry other) {
            for (int i = 0; i < values.length && i < other.values.length; i++)
                values[i] += other.values[i];
        }

        private void multiply(double scalar) {
            for (int i = 0; i < values.length; i++)
                values[i] *= scalar;
        }

        private double minkowskiDistance(Entry that, double h) {
            double distance = 0;
            for (int i = 0; i < values.length; i++)
                distance += Math.pow(Math.abs(values[i] - that.values[i]), h);
            return Math.pow(distance, 1 / h);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Entry entry = (Entry) o;
            return Arrays.equals(values, entry.values);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }

        @Override
        public int compareTo(Entry o) {
            return this.originalIndex - o.originalIndex;
        }
    }

    private static Entry averageEntries(int entryLength, Set<Entry> toAverage) {
        Entry average = new Entry(entryLength);
        for (Entry entry : toAverage)
            average.add(entry);
        if (!toAverage.isEmpty())
            average.multiply(1 / (double) toAverage.size());
        return average;
    }
}
