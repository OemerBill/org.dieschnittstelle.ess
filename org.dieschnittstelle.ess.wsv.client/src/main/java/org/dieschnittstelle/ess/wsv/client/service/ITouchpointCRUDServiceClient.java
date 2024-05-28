package org.dieschnittstelle.ess.wsv.client.service;

import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;

import jakarta.ws.rs.*;
import java.util.List;

@Path("/touchpoints")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface ITouchpointCRUDServiceClient {
	
	@GET
	public List<AbstractTouchpoint> readAllTouchpoints();

	@GET
	@Path("/{touchpointId}")
	public AbstractTouchpoint readTouchpoint(@PathParam("touchpointId") long id);

	@POST
	public AbstractTouchpoint createTouchpoint(AbstractTouchpoint touchpoint); 
	
	@DELETE
	@Path("/{touchpointId}")
	public boolean deleteTouchpoint(@PathParam("touchpointId") long id);

	@PUT
	@Path("/{touchpointId}")
	public AbstractTouchpoint updateTouchpoint(@PathParam("touchpointId") long id,AbstractTouchpoint touchpoint);

}
