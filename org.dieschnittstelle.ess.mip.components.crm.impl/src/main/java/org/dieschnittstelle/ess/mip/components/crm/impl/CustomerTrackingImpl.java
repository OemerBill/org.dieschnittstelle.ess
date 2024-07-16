package org.dieschnittstelle.ess.mip.components.crm.impl;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import jakarta.interceptor.Interceptor;
import org.dieschnittstelle.ess.entities.crm.Customer;
import org.dieschnittstelle.ess.mip.components.crm.api.CustomerTracking;
import org.dieschnittstelle.ess.entities.crm.CustomerTransactionShoppingCartItem;
import org.dieschnittstelle.ess.entities.crm.CustomerTransaction;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.mip.components.crm.crud.impl.CustomerTransactionCRUD;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

/**
 * allows read/write access to a customer's shopping history
 */
@Alternative
@Priority(Interceptor.Priority.APPLICATION+10)
@Logged
@ApplicationScoped
public class CustomerTrackingImpl implements CustomerTracking {

	protected static Logger logger = org.apache.logging.log4j.LogManager
			.getLogger(CustomerTrackingImpl.class);

	@Inject
	private CustomerTransactionCRUD customerTransactionCRUD;

	public CustomerTrackingImpl() {
		logger.info("<constructor>: " + this);
	}

	public void createTransaction(CustomerTransaction transaction) {
		// in case of using the RESTful shopping cart implementation, product bundles will have been persisted and will
		// be passed with a non-default id. In order to allow a unified treatment, we will keep the respective OneToMany
		// relations to ShoppingCartItem and will reset their ids before creating the transaction
		for (CustomerTransactionShoppingCartItem item : transaction.getItems()) {
			item.setId(0);
		}
		
		customerTransactionCRUD.createTransaction(transaction);
	}

	@Override
	public List<CustomerTransaction> readTransactions(long touchpointId,long customerId) {
		if (customerId == 0 && touchpointId == 0) {
			return customerTransactionCRUD.readAllTransactions();
		}
		else if (customerId != 0 && touchpointId != 0) {
			return customerTransactionCRUD.readAllTransactionsForTouchpointAndCustomer(touchpointId,customerId);
		}
		else if (customerId != 0) {
			return customerTransactionCRUD.readAllTransactionsForCustomer(customerId);
		}
		else {
			return customerTransactionCRUD.readAllTransactionsForTouchpoint(touchpointId);
		}
	}

	@Override
	public List<CustomerTransaction> readTransactionsForProduct(long productId) {
		return customerTransactionCRUD.readAllTransactionsForProduct(productId);
	}

	@Override
	public List<Customer> readAllCustomersForProduct(long productId) {
		return customerTransactionCRUD.readAllCustomersForProduct(productId);
	}

	@PostConstruct
	public void initialise() {
		logger.info("@PostConstruct: customerTransactionCRUD is: " + customerTransactionCRUD);
	}

	@PreDestroy
	public void finalise() {
		logger.info("@PreDestroy");
	}

}
