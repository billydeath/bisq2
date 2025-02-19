/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.primary.main.content.portfolio.openoffers;

import bisq.application.DefaultApplicationService;
import bisq.common.observable.Pin;
import bisq.desktop.common.observable.FxBindings;
import bisq.desktop.common.threading.UIThread;
import bisq.desktop.common.view.Controller;
import bisq.i18n.Res;
import bisq.offer.Offer;
import bisq.offer.OpenOffer;
import bisq.offer.OpenOfferService;
import javafx.beans.property.BooleanProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenOffersController implements Controller {
    public static record InitData(Offer offer, BooleanProperty showTakeOfferTab) {
    }

    private final OpenOffersModel model;
    @Getter
    private final OpenOffersView view;
    private final OpenOfferService openOfferService;

    private Pin offerListPin;


    public OpenOffersController(DefaultApplicationService applicationService) {
        model = new OpenOffersModel(applicationService);
        view = new OpenOffersView(model, this);

        openOfferService = applicationService.getOpenOfferService();
    }

    @Override
    public void onViewAttached() {
        offerListPin = FxBindings.<OpenOffer, OpenOfferListItem>bind(model.getListItems())
                .map(openOffer -> new OpenOfferListItem(openOffer, model.marketPriceService))
                .to(openOfferService.getOpenOffers());

        //todo
        model.priceHeaderTitle.set(Res.get("openOffers.table.header.price"));
        model.baseAmountHeaderTitle.set(Res.get("openOffers.table.header.baseAmount"));
        model.quoteAmountHeaderTitle.set(Res.get("openOffers.table.header.quoteAmount"));
    }

    @Override
    public void onViewDetached() {
        offerListPin.unbind();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // UI
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    void onRemoveOffer(OpenOfferListItem item) {
        Offer offer = item.getOffer();
        openOfferService.removeMyOffer(item.getOffer())
                .whenComplete((broadCastResultFutures, throwable2) -> {
                    if (throwable2 != null) {
                        UIThread.run(() -> model.setRemoveOfferError(offer, throwable2));
                        return;
                    }
                    broadCastResultFutures.forEach((key, value) -> value.whenComplete((broadcastResult, throwable3) -> {
                        if (throwable3 != null) {
                            UIThread.run(() -> model.setRemoveOfferError(offer, throwable3));
                            return;
                        }
                        UIThread.run(() -> model.setRemoveOfferResult(offer, broadcastResult));
                    }));
                });
    }
}
