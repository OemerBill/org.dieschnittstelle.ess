package org.dieschnittstelle.ess.mip.components.crm.impl;

import java.util.List;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import jakarta.interceptor.Interceptor;
import jakarta.transaction.Transactional;
import org.dieschnittstelle.ess.mip.components.crm.api.CrmException;
import org.dieschnittstelle.ess.mip.components.crm.api.TouchpointAccess;
import org.dieschnittstelle.ess.mip.components.crm.crud.api.TouchpointCRUD;
import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;
import org.dieschnittstelle.ess.entities.crm.CustomerTransactionShoppingCartItem;
import org.dieschnittstelle.ess.entities.erp.PointOfSale;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.PointOfSaleCRUD;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

@Alternative
@Priority(Interceptor.Priority.APPLICATION+10)
@Logged
@ApplicationScoped
@Transactional
public class TouchpointAccessImpl implements TouchpointAccess {

	protected static Logger logger = org.apache.logging.log4j.LogManager
			.getLogger(TouchpointAccessImpl.class);

	@Inject
	private TouchpointCRUD touchpointCRUD;

	@Inject
	private PointOfSaleCRUD posCRUD;

	@Override
	public AbstractTouchpoint createTouchpointAndPointOfSale(
			AbstractTouchpoint touchpoint) throws CrmException {
//		logProductBundleKlass();

		// we first create the posCRUD
		PointOfSale pos = posCRUD.createPointOfSale(new PointOfSale());
		logger.info("createTouchpointAndPointOfSale(): created pointOfSale: "
				+ pos);

		// we pass the id to the touchpoint
		touchpoint.setErpPointOfSaleId(pos.getId());

		// then we persist the touchpoint
		touchpoint = touchpointCRUD.createTouchpoint(touchpoint);
		logger.info("createTouchpointAndPointOfSale(): created touchpoint: "
				+ touchpoint);

		// return it
		return touchpoint;
	}
	
	// for testing class loading
	private void logProductBundleKlass() {
		StringBuffer log = new StringBuffer();
		log.append(CustomerTransactionShoppingCartItem.class + "\n");
		ClassLoader cl = CustomerTransactionShoppingCartItem.class.getClassLoader();
		do {
			log.append("\t"+ cl + "\n");
			cl = cl.getParent();
		}
		while (cl != null);
		
		logger.info("class loader hierarchy of ShoppingCartItem is: \n" + log);
	}

	@Override
	public List<AbstractTouchpoint> readAllTouchpoints() {
		return touchpointCRUD.readAllTouchpoints();
	}

	public AbstractTouchpoint readTouchpoint(long id) {
		return touchpointCRUD.readTouchpoint(id);
	}


}
