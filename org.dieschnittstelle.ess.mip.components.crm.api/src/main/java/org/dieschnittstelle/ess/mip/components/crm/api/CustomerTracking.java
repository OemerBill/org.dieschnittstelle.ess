package org.dieschnittstelle.ess.mip.components.crm.api;

import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.dieschnittstelle.ess.entities.crm.Customer;
import org.dieschnittstelle.ess.entities.crm.CustomerTransaction;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/tracking")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public interface CustomerTracking {

	@POST
	public void createTransaction(CustomerTransaction transaction);

	@GET
	@Path("/transactions-for-touchpoint-and-customer")
	public List<CustomerTransaction> readTransactions(@QueryParam("touchpointId") long touchpointId,@QueryParam("customerId") long customerId);

	@GET
	@Path("/transactions-for-product")
	public List<CustomerTransaction> readTransactionsForProduct(@QueryParam("productId") long productId);

	@GET
	@Path("/customers-for-product")
	public List<Customer> readAllCustomersForProduct(@QueryParam("productId") long productId);


}
