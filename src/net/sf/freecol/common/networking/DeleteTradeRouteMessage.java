/**
 *  Copyright (C) 2002-2016   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.networking;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.TradeRoute;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when deleting a trade route.
 */
public class DeleteTradeRouteMessage extends DOMMessage {

    public static final String TAG = "deleteTradeRoute";
    private static final String TRADE_ROUTE_TAG = "tradeRoute";

    /** The identifier of the trade route. */
    private final String tradeRouteId;


    /**
     * Create a new {@code DeleteTradeRouteMessage} with the
     * supplied unit and route.
     *
     * @param tradeRoute The {@code TradeRoute} to delete.
     */
    public DeleteTradeRouteMessage(TradeRoute tradeRoute) {
        super(TAG);

        this.tradeRouteId = tradeRoute.getId();
    }

    /**
     * Create a new {@code DeleteTradeRouteMessage} from a
     * supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public DeleteTradeRouteMessage(Game game, Element element) {
        super(TAG);

        this.tradeRouteId = getStringAttribute(element, TRADE_ROUTE_TAG);
    }


    /**
     * Handle a "deleteTradeRoute"-message.
     *
     * @param server The {@code FreeColServer} handling the message.
     * @param player The {@code Player} that sent the message.
     * @param connection The {@code Connection} message was received on.
     * @return An update containing the deleteTradeRouted unit, or an
     *     error {@code Element} on failure.
     */
    public Element handle(FreeColServer server, Player player,
                          Connection connection) {
        final ServerPlayer serverPlayer = server.getPlayer(connection);

        TradeRoute tradeRoute;
        try {
            tradeRoute = serverPlayer.getOurFreeColGameObject(tradeRouteId, 
                TradeRoute.class);
        } catch (RuntimeException e) {
            return serverPlayer.clientError(e.getMessage())
                .build(serverPlayer);
        }

        // Proceed to delete.
        return server.getInGameController()
            .deleteTradeRoute(serverPlayer, tradeRoute)
            .build(serverPlayer);
    }

    /**
     * Convert this DeleteTradeRouteMessage to XML.
     *
     * @return The XML representation of this message.
     */
    @Override
    public Element toXMLElement() {
        return new DOMMessage(TAG,
            "tradeRoute", tradeRouteId).toXMLElement();
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "deleteTradeRoute".
     */
    public static String getTagName() {
        return TAG;
    }
}
