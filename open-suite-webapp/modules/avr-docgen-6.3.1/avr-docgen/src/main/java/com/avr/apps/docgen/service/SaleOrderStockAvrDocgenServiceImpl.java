package com.avr.apps.docgen.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.SaleOrderStockServiceImpl;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;

public class SaleOrderStockAvrDocgenServiceImpl extends SaleOrderStockServiceImpl {

    @Inject
    public SaleOrderStockAvrDocgenServiceImpl(
        StockMoveService stockMoveService,
        StockMoveLineService stockMoveLineService,
        StockConfigService stockConfigService,
        UnitConversionService unitConversionService,
        SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
        StockMoveLineServiceSupplychain stockMoveLineSupplychainService,
        StockMoveLineRepository stockMoveLineRepository,
        AppBaseService appBaseService,
        SaleOrderRepository saleOrderRepository,
        AppSupplychainService appSupplychainService,
        SupplyChainConfigService supplyChainConfigService,
        ProductCompanyService productCompanyService,
        PartnerStockSettingsService partnerStockSettingsService
    ) {
        super(
            stockMoveService,
            stockMoveLineService,
            stockConfigService,
            unitConversionService,
            saleOrderLineServiceSupplyChain,
            stockMoveLineSupplychainService,
            stockMoveLineRepository,
            appBaseService,
            saleOrderRepository,
            appSupplychainService,
            supplyChainConfigService,
            productCompanyService,
            partnerStockSettingsService
        );
    }

    @Override
    public StockMove createStockMove(SaleOrder saleOrder, Company company, LocalDate estimatedDeliveryDate)
        throws AxelorException {
        StockLocation toStockLocation = saleOrder.getToStockLocation();
        if (toStockLocation == null) {
            toStockLocation = partnerStockSettingsService.getDefaultExternalStockLocation(saleOrder.getClientPartner(), company);
        }
        if (toStockLocation == null) {
            toStockLocation = stockConfigService.getCustomerVirtualStockLocation(stockConfigService.getStockConfig(company));
        }

        StockMove stockMove = stockMoveService.createStockMove(
            null,
            saleOrder.getDeliveryAddress(),
            company,
            saleOrder.getClientPartner(),
            saleOrder.getStockLocation(),
            toStockLocation,
            null,
            estimatedDeliveryDate,
            saleOrder.getDescription(),
            saleOrder.getShipmentMode(),
            saleOrder.getFreightCarrierMode(),
            saleOrder.getCarrierPartner(),
            saleOrder.getForwarderPartner(),
            saleOrder.getIncoterm(),
            StockMoveRepository.TYPE_OUTGOING
        );

        stockMove.setToAddressStr(saleOrder.getDeliveryAddressStr());
        stockMove.setOriginId(saleOrder.getId());
        stockMove.setOriginTypeSelect(StockMoveRepository.ORIGIN_SALE_ORDER);
        stockMove.setOrigin(saleOrder.getSaleOrderSeq());
        stockMove.setStockMoveLineList(new ArrayList<>());
        stockMove.setTradingName(saleOrder.getTradingName());
        stockMove.setSpecificPackage(saleOrder.getSpecificPackage());
        stockMove.setNote(saleOrder.getDeliveryComments());
        stockMove.setTitle(saleOrder.getTitle());
        stockMove.setPickingOrderComments(saleOrder.getPickingOrderComments());
        if (stockMove.getPartner() != null) {
            setDefaultAutoMailSettings(stockMove);
        }
        return stockMove;
    }
}
