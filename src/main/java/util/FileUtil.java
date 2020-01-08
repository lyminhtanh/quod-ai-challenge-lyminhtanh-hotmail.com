package util;

import constant.Constant;
import enums.CsvHeader;
import enums.GitHubEventType;
import enums.Metric;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class FileUtil {
    /**
     * download File
     *
     * @param dateTimeString
     */
    public static void downloadAsJsonFile(final String dateTimeString) {
        try {
            System.out.println(String.format("--Downloading: %s", String.format(Constant.BASE_URL, dateTimeString)));

            final String pathname = String.format(Constant.BASE_UNZIPPED_FILE_NAME, dateTimeString);
            final File downloadedFile = new File(pathname);

            final URLConnection conn = new URL(String.format(Constant.BASE_URL, dateTimeString)).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
            conn.connect();

            // decompress gzip then saving to file
            FileUtils.copyInputStreamToFile(new GZIPInputStream(conn.getInputStream()), downloadedFile);

            System.out.println(String.format("-- Saved to: %s.json", dateTimeString));
        } catch (IOException | IllegalFormatException ex) {
            System.out.println(String.format("Failed to download file. %s", ex));
        }
    }

    /**
     * @param filePath
     * @param eventType
     * @return
     */
    public static List<String> readLinesByEventType(final String filePath, final GitHubEventType eventType) {
        List<String> lines = new ArrayList<>();
        try (LineIterator it = FileUtils.lineIterator(new File(filePath), "UTF-8")) {
            while (it.hasNext()) {
                String line = it.nextLine();
                // do something with line
                if (line.contains(eventType.value())) {
                    lines.add(line);
                }
            }
        } catch (IOException ex) {
            System.out.println(String.format("Failed to read lines from file. %s", ex));
        }
        return lines;
    }

    /**
     * list all Json Files
     *
     * @return Set<String>
     * @throws IOException
     */
    public static Set<String> listJsonFiles() {
        try (Stream<Path> stream = Files.walk(Paths.get(""), 1)) {
            return stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .filter(fileName -> fileName.toString().endsWith(".json"))
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        } catch (IOException ex) {
            System.out.println(String.format("Failed list Json files. %s", ex));
        }
        return new HashSet<>();
    }

    /**
     * create CSV File from data rows
     * @param rows
     * @throws IOException
     */
    public static void createCSVFile(List<String[]> rows) throws IOException {
        FileWriter out = new FileWriter(Constant.OUTPUT_FILE_NAME);

        List<String> headers = Stream.of(CsvHeader.values()).map(CsvHeader::name).collect(Collectors.toList());
        headers.addAll(Stream.of(Metric.values()).map(Metric::name).collect(Collectors.toList()));
        
		try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(headers.toArray(new String[0])))) {
            for (String[] row : rows) {
                printer.printRecord(row);
            }
        } catch (IOException ex) {
            System.out.println(String.format("Failed to create CSV file. %s", ex));
        }
    }


}
