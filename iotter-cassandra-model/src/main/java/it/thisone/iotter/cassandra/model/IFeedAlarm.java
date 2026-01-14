package it.thisone.iotter.cassandra.model;

import java.util.Date;

public interface IFeedAlarm {

	String getKey();

	String getSerial();

	String getStatus();

	Float getValue();

	Date getTimestamp();

}