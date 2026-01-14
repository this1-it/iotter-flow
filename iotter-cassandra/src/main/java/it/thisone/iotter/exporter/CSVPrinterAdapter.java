package it.thisone.iotter.exporter;

import org.apache.commons.csv.CSVPrinter;
import java.io.IOException;
import java.util.Arrays;

/**
 * An adapter that wraps a CSVPrinter to implement the RecordPrinter interface.
 */
public class CSVPrinterAdapter implements RecordPrinter {

    private final CSVPrinter csvPrinter;

    public CSVPrinterAdapter(CSVPrinter csvPrinter) {
        this.csvPrinter = csvPrinter;
    }

    @Override
    public void printRecord(Object... values) throws IOException {
        csvPrinter.printRecord(values);
    }

    // The default method printRecord(Object... values) is inherited from RecordPrinter,
    // which converts the array to a list and calls printRecord(Iterable<?> values).

    @Override
    public void flush() throws IOException {
        csvPrinter.flush();
    }

    @Override
    public void close() throws IOException {
        csvPrinter.close();
    }
}