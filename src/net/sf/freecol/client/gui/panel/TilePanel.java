/**
 *  Copyright (C) 2002-2007  The FreeCol Team
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


package net.sf.freecol.client.gui.panel;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.ComponentInputMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.freecol.FreeCol;
import net.sf.freecol.client.gui.Canvas;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.AbstractGoods;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileType;

import net.miginfocom.swing.MigLayout;


/**
 * This panel is used to show information about a tile.
 */
public final class TilePanel extends FreeColDialog<Boolean> implements ActionListener {

    private static final Logger logger = Logger.getLogger(TilePanel.class.getName());

    private static final int OK = 0;
    private static final int COLOPEDIA = 1;
    private final JButton okButton = new JButton(Messages.message("ok"));

    private TileType tileType;


    /**
     * The constructor that will add the items to this panel.
     * @param parent The parent panel.
     * @param tile a <code>Tile</code> value
     */
    public TilePanel(Canvas parent, Tile tile) {
        super(parent);

        tileType = tile.getType();

        setLayout(new MigLayout("wrap 1, insets 20 30 10 30", "[center]", ""));

        JButton colopediaButton = new JButton(Messages.message("menuBar.colopedia"));
        colopediaButton.setActionCommand(String.valueOf(COLOPEDIA));
        colopediaButton.addActionListener(this);
        enterPressesWhenFocused(colopediaButton);

        okButton.setActionCommand(String.valueOf(OK));
        okButton.addActionListener(this);
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterPressesWhenFocused(okButton);

        // Use ESCAPE for closing the panel:
        InputMap inputMap = new ComponentInputMap(okButton);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), "released");
        SwingUtilities.replaceUIInputMap(okButton, JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);        

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(colopediaButton);
        buttonPanel.add(okButton);
        add(buttonPanel);


        String name = tile.getLabel() + " (" + tile.getX() + ", " + tile.getY() + ")";
        add(new JLabel(name));

        int width = getLibrary().getTerrainImageWidth(tileType);
        int height = getLibrary().getTerrainImageHeight(tileType);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        getCanvas().getGUI().displayColonyTile((Graphics2D) image.getGraphics(), tile.getMap(),
                                          tile, 0, 0, null);
        add(new JLabel(new ImageIcon(image)));

        if (tile.getRegion() != null) {
            add(new JLabel(tile.getRegion().getDisplayName()));
        }
        if (tile.getOwner() != null) {
            String ownerName = tile.getOwner().getNationAsString();
            if (ownerName != null) {
                add(new JLabel(ownerName));
            }
        }

        if (tileType != null) {
            List<AbstractGoods> production = tileType.getProduction();
            if (!production.isEmpty()) {
                GoodsType goodsType = production.get(0).getType();
                add(new JLabel(String.valueOf(tile.potential(goodsType, null)),
                               getLibrary().getGoodsImageIcon(goodsType),
                               JLabel.CENTER),
                    "split " + production.size());
                for (int index = 1; index < production.size(); index++) {
                    goodsType = production.get(index).getType();
                    add(new JLabel(String.valueOf(tile.potential(goodsType, null)),
                                   getLibrary().getGoodsImageIcon(goodsType),
                                   JLabel.CENTER));
                }

            }
        }

        add(okButton, "newline 30, split 2, align center");
        add(colopediaButton);

        setSize(getPreferredSize());

    }


    public void requestFocus() {
        okButton.requestFocus();
    }

    /**
     * This function analyses an event and calls the right methods to take
     * care of the user's requests.
     * @param event The incoming ActionEvent.
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        try {
            switch (Integer.valueOf(command).intValue()) {
            case OK:
                setResponse(new Boolean(true));
                break;
            case COLOPEDIA:
                setResponse(new Boolean(true));
                getCanvas().showColopediaPanel(ColopediaPanel.PanelType.TERRAIN, tileType);
                break;
            default:
                logger.warning("Invalid Actioncommand: invalid number.");
            }
        } catch (NumberFormatException e) {
            logger.warning("Invalid Actioncommand: not a number.");
        }
    }
}
