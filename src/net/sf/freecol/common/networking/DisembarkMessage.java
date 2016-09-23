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
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;
import net.sf.freecol.server.model.ServerUnit;

import org.w3c.dom.Element;


/**
 * The message sent when disembarking.
 */
public class DisembarkMessage extends DOMMessage {

    public static final String TAG = "disembark";
    private static final String UNIT_TAG = "unit";

    /** The identifier of the object disembarking. */
    private final String unitId;


    /**
     * Create a new {@code DisembarkMessage} with the
     * supplied name.
     *
     * @param unit The {@code Unit} that is disembarking.
     */
    public DisembarkMessage(Unit unit) {
        super(TAG);

        this.unitId = unit.getId();
    }

    /**
     * Create a new {@code DisembarkMessage} from a
     * supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public DisembarkMessage(Game game, Element element) {
        super(TAG);

        this.unitId = getStringAttribute(element, UNIT_TAG);
    }


    /**
     * Handle a "disembark"-message.
     *
     * @param server The {@code FreeColServer} handling the message.
     * @param player The {@code Player} the message applies to.
     * @param connection The {@code Connection} message was received on.
     * @return An update containing the disembarked unit, or an error
     *     {@code Element} on failure.
     */
    public Element handle(FreeColServer server, Player player,
                          Connection connection) {
        final ServerPlayer serverPlayer = server.getPlayer(connection);

        ServerUnit unit;
        try {
            unit = player.getOurFreeColGameObject(this.unitId, ServerUnit.class);
        } catch (RuntimeException e) {
            return serverPlayer.clientError(e.getMessage())
                .build(serverPlayer);
        }

        // Do the disembark.
        return server.getInGameController()
            .disembarkUnit(serverPlayer, unit)
            .build(serverPlayer);
    }

    /**
     * Convert this DisembarkMessage to XML.
     *
     * @return The XML representation of this message.
     */
    @Override
    public Element toXMLElement() {
        return new DOMMessage(TAG,
            UNIT_TAG, this.unitId).toXMLElement();
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "disembark".
     */
    public static String getTagName() {
        return TAG;
    }
}
