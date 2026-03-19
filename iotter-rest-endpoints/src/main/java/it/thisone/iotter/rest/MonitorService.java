package it.thisone.iotter.rest;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;


@Component
@Path("/monitor")
public class MonitorService {
	@PersistenceContext
	private EntityManager entityManager;
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/entitymanager")
	public Response checkEntityManager() {
		Connection connection = null;
		try {
			EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		    connection = info.getDataSource().getConnection();
		    connection.close();
		} catch (Exception e) {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(new Acknowledge("entity manager cannot connect ")).build();
		} finally {
			try {
				if (null != connection && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
			}
		}
		return Response.status(Response.Status.OK).entity(new Acknowledge("entity manager is alive")).build();
	}
	


}

@SuppressWarnings("serial")
class Acknowledge implements Serializable {
	private String message;

	public Acknowledge(String message) {
		super();
		this.message = message;
	}

	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

}
