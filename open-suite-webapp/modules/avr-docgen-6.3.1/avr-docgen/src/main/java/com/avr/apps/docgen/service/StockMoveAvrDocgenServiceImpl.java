package com.avr.apps.docgen.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
public class StockMoveAvrDocgenServiceImpl extends StockMoveServiceImpl {

    protected StockMoveLineRepository stockMoveLineRepo;
    protected StockMoveToolService stockMoveToolService;

    @Inject
    public StockMoveAvrDocgenServiceImpl(
        StockMoveLineService stockMoveLineService,
        StockMoveToolService stockMoveToolService,
        StockMoveLineRepository stockMoveLineRepository,
        AppBaseService appBaseService,
        StockMoveRepository stockMoveRepository,
        PartnerProductQualityRatingService partnerProductQualityRatingService,
        AppSupplychainService appSupplyChainService,
        PurchaseOrderRepository purchaseOrderRepo,
        SaleOrderRepository saleOrderRepo,
        UnitConversionService unitConversionService,
        ReservedQtyService reservedQtyService,
        ProductRepository productRepository
    ) {
        super(
            stockMoveLineService,
            stockMoveToolService,
            stockMoveLineRepository,
            appBaseService,
            stockMoveRepository,
            partnerProductQualityRatingService,
            productRepository
        );
        this.stockMoveToolService = stockMoveToolService;
        this.stockMoveLineRepo = stockMoveLineRepository;
    }

    @Override
    public Optional<StockMove> copyAndSplitStockMoveReverse(
        StockMove stockMove,
        List<StockMoveLine> stockMoveLines,
        boolean split
    ) throws AxelorException {
        stockMoveLines = MoreObjects.firstNonNull(stockMoveLines, Collections.emptyList());
        StockMove newStockMove = createStockMove(
            stockMove.getToAddress(),
            stockMove.getFromAddress(),
            stockMove.getCompany(),
            stockMove.getPartner(),
            stockMove.getToStockLocation(),
            stockMove.getFromStockLocation(),
            null,
            stockMove.getEstimatedDate(),
            null,
            null,
            null,
            null,
            null,
            stockMove.getIncoterm(),
            0
        );

        if (stockMove.getToAddress() != null) newStockMove.setFromAddress(stockMove.getToAddress());
        if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) newStockMove.setTypeSelect(
            StockMoveRepository.TYPE_OUTGOING
        );
        if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) newStockMove.setTypeSelect(
            StockMoveRepository.TYPE_INCOMING
        );
        if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INTERNAL) newStockMove.setTypeSelect(
            StockMoveRepository.TYPE_INTERNAL
        );
        newStockMove.setStockMoveSeq(
            stockMoveToolService.getSequenceStockMove(newStockMove.getTypeSelect(), newStockMove.getCompany())
        );

        for (StockMoveLine stockMoveLine : stockMoveLines) {
            if (!split || stockMoveLine.getRealQty().compareTo(stockMoveLine.getQty()) > 0) {
                StockMoveLine newStockMoveLine = stockMoveLineRepo.copy(stockMoveLine, false);

                if (split) {
                    newStockMoveLine.setQty(stockMoveLine.getRealQty().subtract(stockMoveLine.getQty()));
                    newStockMoveLine.setRealQty(newStockMoveLine.getQty());
                } else {
                    newStockMoveLine.setQty(stockMoveLine.getRealQty());
                    newStockMoveLine.setRealQty(stockMoveLine.getRealQty());
                }

                newStockMove.addStockMoveLineListItem(newStockMoveLine);
            }
        }

        if (ObjectUtils.isEmpty(newStockMove.getStockMoveLineList())) {
            return Optional.empty();
        }

        newStockMove.setStockMoveSeq(
            stockMoveToolService.getSequenceStockMove(newStockMove.getTypeSelect(), newStockMove.getCompany())
        );
        newStockMove.setName(
            stockMoveToolService.computeName(
                newStockMove,
                newStockMove.getStockMoveSeq() +
                " " +
                I18n.get(IExceptionMessage.STOCK_MOVE_8) +
                " " +
                stockMove.getStockMoveSeq() +
                " )"
            )
        );
        if (stockMove.getPartner() != null) {
            newStockMove.setShipmentMode(stockMove.getPartner().getShipmentMode());
            newStockMove.setFreightCarrierMode(stockMove.getPartner().getFreightCarrierMode());
            newStockMove.setCarrierPartner(stockMove.getPartner().getCarrierPartner());
        }
        newStockMove.setReversionOriginStockMove(stockMove);
        newStockMove.setFromAddressStr(stockMove.getFromAddressStr());
        newStockMove.setNote(stockMove.getNote());
        newStockMove.setNumOfPackages(stockMove.getNumOfPackages());
        newStockMove.setNumOfPalettes(stockMove.getNumOfPalettes());
        newStockMove.setGrossMass(stockMove.getGrossMass());
        newStockMove.setExTaxTotal(stockMoveToolService.compute(newStockMove));
        newStockMove.setIsReversion(true);
        newStockMove.setIsWithBackorder(stockMove.getIsWithBackorder());
        newStockMove.setOrigin(stockMove.getOrigin());
        newStockMove.setOriginId(stockMove.getOriginId());
        newStockMove.setOriginTypeSelect(stockMove.getOriginTypeSelect());
        newStockMove.setTitle(stockMove.getTitle());

        return Optional.of(stockMoveRepo.save(newStockMove));
    }
}
