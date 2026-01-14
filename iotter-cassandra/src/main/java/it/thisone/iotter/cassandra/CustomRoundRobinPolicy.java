/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package it.thisone.iotter.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.datastax.oss.driver.api.core.loadbalancing.LoadBalancingPolicy;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.tracker.RequestTracker;

/**
 * A Round-robin load balancing policy.
 * <p/>
 * This policy queries nodes in a round-robin fashion. For a given query,
 * if an host fail, the next one (following the round-robin order) is
 * tried, until all hosts have been tried.
 * <p/>
 * This policy is not datacenter aware and will include every known
 * Cassandra host in its round robin algorithm. If you use multiple
 * datacenter this will be inefficient and you will want to use the
 * {@link DCAwareRoundRobinPolicy} load balancing policy instead.
 */
public class CustomRoundRobinPolicy implements LoadBalancingPolicy {

    private static final Logger logger = LoggerFactory.getLogger(CustomRoundRobinPolicy.class);

    private final CopyOnWriteArrayList<Node> liveHosts = new CopyOnWriteArrayList<Node>();
    private final AtomicInteger index = new AtomicInteger();

    /**
     * Creates a load balancing policy that picks host to query in a round robin
     * fashion (on all the hosts of the Cassandra cluster).
     */
    public CustomRoundRobinPolicy() {
    }

    @Override
    public void init(java.util.Map<java.util.UUID, Node> nodes, LoadBalancingPolicy.DistanceReporter distanceReporter) {
        this.liveHosts.addAll(nodes.values());
        this.index.set(new Random().nextInt(Math.max(nodes.size(), 1)));
    }

    @Override
    public Queue<Node> newQueryPlan(Request request, Session session) {
        final List<Node> hosts = (List<Node>) liveHosts.clone();
        final int startIdx = index.getAndIncrement();

        if (startIdx > Integer.MAX_VALUE - 10000) {
            index.set(0);
        }

        Queue<Node> plan = new ArrayDeque<Node>(hosts.size());
        for (int i = 0; i < hosts.size(); i++) {
            int c = (startIdx + i) % hosts.size();
            if (c < 0) {
                c += hosts.size();
            }
            Node host = hosts.get(c);
            if (request instanceof com.datastax.oss.driver.api.core.cql.Statement) {
                @SuppressWarnings("unchecked")
                com.datastax.oss.driver.api.core.cql.Statement<?> statement =
                        (com.datastax.oss.driver.api.core.cql.Statement<?>) request;
                logger.debug("{} {}", CassandraQueryBuilder.getQueryString(statement), host.getEndPoint());
            }
            plan.add(host);
        }
        return plan;
    }

    @Override
    public void onUp(Node host) {
        liveHosts.addIfAbsent(host);
    }

    @Override
    public void onDown(Node host) {
        liveHosts.remove(host);
    }

    @Override
    public void onAdd(Node host) {
        onUp(host);
    }

    @Override
    public void onRemove(Node host) {
        onDown(host);
    }

    @Override
    public java.util.Optional<RequestTracker> getRequestTracker() {
        return java.util.Optional.empty();
    }

    @Override
    public void close() {
        // nothing to do
    }

}
