package org.dieschnittstelle.ess.jrs.client.junit;

import jakarta.ws.rs.NotFoundException;
import org.dieschnittstelle.ess.entities.erp.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestProductRESTService {

	private ProductCRUDRESTClient client;

	// we use static attributes for sharing data between the single cases
	private static IndividualisedProductItem PRODUCT_1 = new IndividualisedProductItem("Schrippe",	ProductType.ROLL, 720);
	private static IndividualisedProductItem PRODUCT_2 = new IndividualisedProductItem("Kirschplunder",ProductType.PASTRY, 1080);
	private static Campaign CAMPAIGN = new Campaign();
	private static List<?> prodlistBefore = new ArrayList<>();

	@Before
	public void prepareClient() throws Exception {
		client = new ProductCRUDRESTClient();
	}

	// this is not a testcase, but  factored-out preparation method
	private void prepareProducts() {
		ProductBundle b1 = new ProductBundle();
		b1.setUnits(10);
		b1.setProduct(PRODUCT_1);
		ProductBundle b2 = new ProductBundle();
		b2.setUnits(3);
		b2.setProduct(PRODUCT_2);
		CAMPAIGN.addBundle(b1);
		CAMPAIGN.addBundle(b2);
	}

	@Test
	public void a_readAll() {
		prodlistBefore = new ArrayList<>();
		prepareProducts();

		// read all products
		assertNotNull("product list can be read", prodlistBefore = client.readAllProducts());
	}

	@Test
	public void b_create() {
		// create two products
		client.createProduct(PRODUCT_1);
		client.createProduct(PRODUCT_2);
		assertEquals("product list is appended on create", 2, client.readAllProducts().size() - prodlistBefore.size());
	}

	@Test
	public void c_read() {
		// read the products and check whether they are equivalent
		AbstractProduct testProduct = client.readProduct(PRODUCT_1.getId());

		assertNotNull("new product can be read", testProduct);
		assertEquals("new product name is correct", PRODUCT_1.getName(), testProduct.getName());
	}

	@Test
	public void d_update() {
		/* UPDATE */
		// change the local name
		PRODUCT_1.setName(PRODUCT_1.getName() + " " + PRODUCT_1.getName());
		// update the product on the server-side
		client.updateProduct(PRODUCT_1);

		// read out the product and compare the names
		AbstractProduct testProduct = client.readProduct(PRODUCT_1.getId());
		assertEquals("product name is updated correctly", PRODUCT_1.getName(), testProduct.getName());
	}

	@Test
	public void e_delete() {
		/* DELETE */
		assertTrue("product can be deleted",  client.deleteProduct(PRODUCT_1.getId()));
		assertThrows("deleted product does not exist anymore", NotFoundException.class,() -> {
			client.readProduct(PRODUCT_1.getId());
		});
		assertEquals("product list is reduced on delete",prodlistBefore.size()+1,client.readAllProducts().size());
	}


	// TODO: activate this method (including the @Test annotation), for testing JRS3
	@Test
	public void f_createCampaign() {
		/* this is for internally testing that campaigns can be written and read via the web api - not part of the exercise */
		assertNotNull("campaign creation works",(Campaign)client.createCampaign(CAMPAIGN));
	}

}
