package org.dieschnittstelle.ess.mip.components.shopping.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

// TODO: PAT1: this is the interface to be provided as a rest service if rest service access is used
@Path("/shoppingcarts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface PurchaseService {

	@POST
	@Path("/{cartId}/purchase")
	void purchaseCartAtTouchpointForCustomer(@PathParam("cartId") long shoppingCartId, @QueryParam("tpId") long touchpointId, @QueryParam("custId") long customerId) throws ShoppingException;
}
