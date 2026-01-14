package it.thisone.iotter.config;

//import org.eclipse.persistence.internal.sessions.DatabaseSessionImpl;
import org.eclipse.persistence.sessions.Session;
//import org.eclipse.persistence.sessions.coordination.RemoteCommandManager;
//import org.eclipse.persistence.sessions.coordination.jms.JMSTopicTransportManager;

public class JmsSessionCustomizer implements org.eclipse.persistence.config.SessionCustomizer
{
    @Override
    public void customize(final Session session) throws Exception
    {

//      java.util.Properties props = new java.util.Properties(); 
//      props.put(Context.PROVIDER_URL, getProviderURL()); 
//      props.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory()); 
//      JMSTopicTransportManager tm = new JMSTopicTransportManager(rcm); 
//      tm.setLocalContextProperties(props); 
//      tm.setRemoteContextProperties(props); 

    	
//        final RemoteCommandManager rcm = new RemoteCommandManager((DatabaseSessionImpl) session);
//        final JMSTopicTransportManager tm = new JMSTopicTransportManager(rcm);
//
//        tm.setTopicName("java:comp/env/jms/PersistenceTopic");
//        tm.setTopicConnectionFactoryName("java:comp/env/jms/ConnectionFactory");
//        
//        tm.setShouldRemoveConnectionOnError(true);
//
//        rcm.setTransportManager(tm);

//        rcm.getTransportManager().setUserName("");
//        rcm.getTransportManager().setPassword("");
//        rcm.getTransportManager().setInitialContextFactoryName("org.apache.naming.java.javaURLContextFactory");

//        rcm.setShouldPropagateAsynchronously(true);
//        rcm.setServerPlatform(((org.eclipse.persistence.sessions.DatabaseSession) session).getServerPlatform());
//        ((DatabaseSessionImpl) session).setCommandManager(rcm);
//        ((DatabaseSessionImpl) session).setShouldPropagateChanges(true);
//
//        rcm.initialize();
    }

}