package org.dieschnittstelle.ess.mip.components.crm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.enterprise.inject.Alternative;
import jakarta.interceptor.Interceptor;
import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;
import org.dieschnittstelle.ess.entities.crm.CampaignExecution;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.mip.components.crm.api.CampaignTracking;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

/**
 * tracks the execution of a compaign
 */
@Alternative
@Priority(Interceptor.Priority.APPLICATION+10)
@Logged
@ApplicationScoped
public class CampaignTrackingImplSingleton implements CampaignTracking {

	protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(CampaignTrackingImplSingleton.class);
	
	/**
	 * a map that associates touchpoint ids with campaign ids (we assume that
	 * for each touchpoint id there may only exist a single campaign execution
	 * for a given campaign id)
	 */
	private Map<Long, Map<Long, CampaignExecution>> campaignExecutionsAtTouchpoint = new HashMap<Long, Map<Long, CampaignExecution>>();

	public CampaignTrackingImplSingleton() {
		logger.info("<constructor>: " + this);
	}
	
	/**
	 * add a campaign execution
	 */
	public void addCampaignExecution(CampaignExecution campaign) {
		if (!this.campaignExecutionsAtTouchpoint.containsKey(campaign
				.getTouchpoint().getId())) {
			this.campaignExecutionsAtTouchpoint.put(campaign.getTouchpoint()
					.getId(), new HashMap<Long, CampaignExecution>());
		}
		this.campaignExecutionsAtTouchpoint.get(
				campaign.getTouchpoint().getId()).put(
				campaign.getErpCampaignId(), campaign);
	}

	/**
	 * check whether for some product id there exists a campaign at some
	 * touchpoint and return its available units
	 */
	public int existsValidCampaignExecutionAtTouchpoint(long erpProductId,
			AbstractTouchpoint tp) {
		Map<Long, CampaignExecution> campaignExecutions = this.campaignExecutionsAtTouchpoint
				.get(tp.getId());
		if (campaignExecutions == null) {
			logger.warn("no CampaignExecution found for touchpoint " + tp + " in " + this.campaignExecutionsAtTouchpoint);
			return 0;
		}
		CampaignExecution ce = campaignExecutions.get(erpProductId);
		if (ce == null) {
			logger.warn("no CampaignExecution found for product id " + erpProductId + " in " + this.campaignExecutionsAtTouchpoint);
			return 0;
		}
		if (!ce.isValid()) {
			logger.warn("CampaignExecution " + ce + " is not valid!");
			return 0;
		}

		return ce.getUnitsLeft();
	}

	/**
	 * purchase some units of some campaign at some touchpoint
	 */
	public void purchaseCampaignAtTouchpoint(long erpProductId,
			AbstractTouchpoint tp, int units) {
		this.campaignExecutionsAtTouchpoint.get(tp.getId()).get(erpProductId)
				.purchase(units);
	}
	
	public List<CampaignExecution> getAllCampaignExecutions() {
		List<CampaignExecution> campaigns = new ArrayList<CampaignExecution>();
		for (long tpid : campaignExecutionsAtTouchpoint.keySet()) {
			for (long cpid : campaignExecutionsAtTouchpoint.get(tpid).keySet()) {
				campaigns.add(campaignExecutionsAtTouchpoint.get(tpid).get(cpid));
			}
		}
		
		return campaigns;
	}
	
	@PostConstruct
	public void start() {
		logger.info("@PostConstruct");
	}

	@PreDestroy
	public void ende() {
		logger.info("@PreDestroy");
	}


}
