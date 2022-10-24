package com.avr.apps.docgen.repository;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.db.Query;
import com.google.inject.Singleton;

import java.util.List;

@Singleton
public class StockMoveLineDocgenRepository {

	 public List<StockMoveLine> findStockMoveLineConsumedByProductFinished(Product productFinished, TrackingNumber trackingNumberOfProductFinished) {
			return Query.of(StockMoveLine.class)
			            .filter(
											"self.finishedProduct = (Select sm.id FROM StockMoveLine as sm WHERE sm.product = :product AND sm.trackingNumber = :trackingNumber AND sm.producedManufOrder IS NOT NULL) AND self.consumedManufOrder IS NOT NULL")
			            .bind("product", productFinished)
			            .bind("trackingNumber", trackingNumberOfProductFinished)
			            .fetch();
	 }

}
