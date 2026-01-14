package it.thisone.iotter.cassandra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;


/*
 * 
 * https://stackoverflow.com/questions/25359928/spring-data-cassandra-2-0-select-blob-column-returns-incorrect-bytebuffer-data
 * 
 * Write a blob content to Cassandra using datastax\java-driver
 * https://gist.github.com/devsprint/5363023
 * 
 * Wraps an async execution of Datastax Java driver into an observable.
 * https://gist.github.com/devsprint/8078765
 * 
 * https://stackoverflow.com/questions/25197685/how-can-i-store-objects-in-cassandra-using-the-blob-datatype
 * 
 * Java: Efficiently converting an array of longs to an array of bytes
 * https://stackoverflow.com/questions/29927238/java-efficiently-converting-an-array-of-longs-to-an-array-of-bytes
 * 
 */

public interface Bufferable extends Serializable {


    default ByteBuffer serialize() {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bytes);) {
            oos.writeObject(this);
            return ByteBuffer.wrap(bytes.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    public static Bufferable deserialize(ByteBuffer bytes) {
        ByteBuffer copy = bytes.duplicate();
        byte[] data = new byte[copy.remaining()];
        copy.get(data);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));) {
            return (Bufferable) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
    }
}
