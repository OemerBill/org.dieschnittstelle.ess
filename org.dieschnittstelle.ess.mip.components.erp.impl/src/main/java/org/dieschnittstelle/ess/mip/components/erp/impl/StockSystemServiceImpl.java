package org.dieschnittstelle.ess.mip.components.erp.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.entities.erp.PointOfSale;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystem;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystemService;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.ProductCRUD;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

import java.util.List;

@ApplicationScoped
@Transactional
@Logged
public class StockSystemServiceImpl implements StockSystemService {

    @Inject
    private StockSystem stockSystem;

    @Inject
    private ProductCRUD productCRUD;

    @Override
    public void addToStock(long productId, long pointOfSaleId, int units) {
        IndividualisedProductItem prod = (IndividualisedProductItem) productCRUD.readProduct(productId);
        stockSystem.addToStock(prod, pointOfSaleId, units);
    }

    @Override
    public void removeFromStock(long productId, long pointOfSaleId, int units) {
        IndividualisedProductItem prod = (IndividualisedProductItem) productCRUD.readProduct(productId);
        stockSystem.removeFromStock(prod, pointOfSaleId, units);
    }

    @Override
    public List<IndividualisedProductItem> getProductsOnStock(long pointOfSaleId) {
        return stockSystem.getProductsOnStock(pointOfSaleId);
    }

    @Override
    public int getUnitsOnStock(long productId, long pointOfSaleId) {
        IndividualisedProductItem prod = (IndividualisedProductItem) productCRUD.readProduct(productId);
        return stockSystem.getUnitsOnStock(prod, pointOfSaleId);
    }

    @Override
    public List<Long> getPointsOfSale(long productId) {
        return stockSystem.getPointsOfSale((IndividualisedProductItem) productCRUD.readProduct(productId));
    }
}
