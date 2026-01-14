package it.thisone.iotter.util;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ModbusProfile;

public class ChannelProfilesComparator implements Comparator<Channel> {
    Map<Channel, Set<ModbusProfile>> base;

    public ChannelProfilesComparator(Map<Channel, Set<ModbusProfile>> base) {
        this.base = base;
    }

    public int compare(Channel a, Channel b) {
        if (base.get(a).size() >= base.get(b).size()) {
            return -1;
        } else {
            return 1;
        } 
        // returning 0 would merge keys
    }
}