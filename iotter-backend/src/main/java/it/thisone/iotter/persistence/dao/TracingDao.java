package it.thisone.iotter.persistence.dao;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.persistence.ifc.ITracingDao;
import it.thisone.iotter.persistence.model.Tracing;
import it.thisone.iotter.persistence.model.TracingCriteria;

@Repository
public class TracingDao extends BaseEntityDao<Tracing> implements ITracingDao {
    
	public TracingDao() {
        super();
        setClazz(Tracing.class);
    }
    
	
//    @Override
//    public void create(Tracing entity) {
//    	try {
//			entityManager.getTransaction().begin();
//			entityManager.persist(entity);
//			entityManager.flush();
//			entityManager.getTransaction().commit();
//		} catch (RuntimeException e) {
//		}
//    }
	
	@Override
	public void trace(TracingAction action, String username, String administrator, String network, String device, String message) {
		if (username == null) {
			username = Constants.SYSTEM;
		}
		if (administrator == null) {
			administrator = Constants.SYSTEM;
		}
		
		String host = getHostAddress();
		if (host != null && !host.equals("localhost")) {
			username = String.format("%s@%s", username, host);
		}
		
//		try {
//			String host = InetAddress.getLocalHost().getHostAddress();
//			if (host != null && !host.equals("127.0.0.1")) {
//				username = String.format("%s@%s", username, host);
//			}
//		} catch (UnknownHostException e) {
//		}
		
		Tracing entity = new Tracing(action, username, administrator, network, device, message);
		create(entity);
	}


	@Override
	public long count(TracingCriteria criteria) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Tracing> root = cq.from(Tracing.class);
		cq.select(cb.count(root));
		cq.distinct(true);
		Predicate[] predicate = buildCriteria(cb, root, criteria);
		cq.where(predicate);
		return entityManager.createQuery(cq).getSingleResult();
	}

	@Override
	public List<Tracing> search(TracingCriteria criteria, int offset, int limit) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tracing> cq = cb.createQuery(Tracing.class);
		Root<Tracing> root = cq.from(Tracing.class);
		Predicate[] predicates = buildCriteria(cb, root, criteria);
		cq = cq.select(root).where(predicates).orderBy(cb.desc(root.get("timeStamp")));
		return find(cq, offset, limit);
	}
	
	
	private Predicate[] buildCriteria(CriteriaBuilder cb, Root<Tracing> root, TracingCriteria criteria) {
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		List<Predicate> orPredicates = new ArrayList<Predicate>();

		if (criteria.getAdministrator() != null) {
			andPredicates.add(cb.equal(root.get("administrator"), criteria.getAdministrator()));
		}
		
		if (criteria.getNetwork() != null) {
			andPredicates.add(cb.equal(root.get("network"), criteria.getNetwork()));
		}

		if (criteria.getDevice() != null) {
			andPredicates.add(cb.equal(root.get("device"), criteria.getDevice()));
		}

		
		if (criteria.getOwner() != null) {
			andPredicates.add(cb.equal(root.get("owner"), criteria.getOwner()));
		}
		
		if (criteria.getInterval() != null) {
			Path<Date> ts = root.get("timeStamp");
			andPredicates.add(cb.between( ts , criteria.getInterval().lowerEndpoint(), criteria.getInterval().upperEndpoint()));
		}
		
		if (criteria.getActions() != null) {
			for (TracingAction action : criteria.getActions()) {
				orPredicates.add(cb.equal(root.get("action"), action));		
			}
		}
		
		return buildCriteriaPredicates(cb,  andPredicates, orPredicates);

	}

	public String getHostAddress() {
		Properties props = System.getProperties();
		String address = props.getProperty("HostAddress");
		if (address != null) return address;
		try {
			InetAddress ia = getLocalHost();
			address = ia.getHostAddress();
			props.setProperty("HostAddress", address);
		} catch (UnknownHostException e) {

		}
		return address;
	}
	
	private InetAddress getCurrentIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while(nias.hasMoreElements()) {
                    InetAddress ia= (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() 
                     && !ia.isLoopbackAddress()
                     && ia instanceof Inet4Address) {
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
        }
        return null;
    }	
    private InetAddress getLocalHost() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        }
                        else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidateAddress will be non-null.
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC
                // (or it might be running IPv6 which deprecates the "site-local" concept)
                // So return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // So fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        }
        catch (Exception e) { // CHECKSTYLE:SKIP
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e.getMessage());
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }	
}
