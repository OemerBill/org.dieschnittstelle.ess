package org.dieschnittstelle.ess.mip.components.crm.api;

import java.util.List;

import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/touchpoints")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public interface TouchpointAccess {

	@POST
	public AbstractTouchpoint createTouchpointAndPointOfSale(AbstractTouchpoint touchpoint) throws CrmException;

	@GET
	public List<AbstractTouchpoint> readAllTouchpoints();

	@GET
	@Path("/{id}")
	public AbstractTouchpoint readTouchpoint(@PathParam("id") long id);
	
}
