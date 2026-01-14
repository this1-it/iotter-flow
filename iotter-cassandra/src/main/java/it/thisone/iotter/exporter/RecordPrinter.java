package it.thisone.iotter.exporter;

import java.io.IOException;

public interface RecordPrinter {
    void printRecord(Object... values) throws IOException;
    void flush() throws IOException;
    void close() throws IOException;
}