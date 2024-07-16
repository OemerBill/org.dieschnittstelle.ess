package org.dieschnittstelle.ess.mip.components.crm.crud.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.dieschnittstelle.ess.entities.crm.Customer;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/customers")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public interface CustomerCRUD {

	@POST
	public Customer createCustomer(Customer customer);

	@GET
	@Path("/{customerId}")
	public Customer readCustomer(@PathParam("customerId") long id);

	// the update method is unorthodox, as it should normally identify the customer via its id in the path
	@PUT
	public Customer updateCustomer(Customer customer);

	@PUT
	public Customer updateCustomerWithSleep(Customer customer,@QueryParam("sleep") long sleep);

	@DELETE
	@Path("/{customerId}")
	public boolean deleteCustomer(@PathParam("customerId") int id);

	@GET
	public Customer readCustomerForEmail(@QueryParam("email") String email);

}
