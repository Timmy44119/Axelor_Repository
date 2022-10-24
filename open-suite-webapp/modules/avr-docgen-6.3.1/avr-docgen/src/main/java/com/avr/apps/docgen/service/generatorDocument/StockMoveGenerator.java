package com.avr.apps.docgen.service.generatorDocument;

import com.avr.apps.docgen.common.I18n;
import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.exception.FieldRequiredException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 12:24
 * @Update 13/10/2021
 */
public class StockMoveGenerator extends GeneratorDocument<StockMove> {

    @Override
    protected String getSequence(StockMove stockMove) {
        return stockMove.getStockMoveSeq();
    }

    @Override
    protected Partner getPartner(StockMove stockMove) throws FieldRequiredException {
        checkValidityField(stockMove, StockMove::getPartner);
        return stockMove.getPartner();
    }

    @Override
    public String getTypeName(StockMove stockMove) {
        return I18n.get(isBR(stockMove) ? "Bon de retour" : "Bon de livraison");
    }

    @Override
    protected DocgenSubType getType(StockMove stockMove) {
        return isBR(stockMove) ? getApp().getSubTypeStockMoveReturnVoucher() : getApp().getSubTypeStockMoveDeliveryNote();
    }

    private boolean isBR(StockMove stockMove) {
        return stockMove.getTypeSelect().equals(3);
    }
}
