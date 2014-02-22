/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     cpw - implementation
 */

package cpw.mods.fml.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringTranslate;
import cpw.mods.fml.common.network.ModMissingPacket;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import focssy.FocssyUpdaterScreen;


public class GuiModsMissingForServer extends GuiScreen
{
    private ModMissingPacket modsMissing;

    public GuiModsMissingForServer(ModMissingPacket modsMissing)
    {
        this.modsMissing = modsMissing;
    }

    @Override

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.add(new GuiSmallButton(1, this.width / 2 - 160, this.height - 38, I18n.getString("gui.yes")));
        this.buttonList.add(new GuiSmallButton(2, this.width / 2 + 10, this.height - 38, I18n.getString("gui.no")));
    }

    @Override

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled && par1GuiButton.id == 2)
        {
            FMLClientHandler.instance().getClient().displayGuiScreen(null);
        }
        else
        {
        	FMLClientHandler.instance().getClient().displayGuiScreen(new FocssyUpdaterScreen());
        }
    }
    @Override

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        int offset = Math.max(85 - modsMissing.getModList().size() * 10, 10);
        this.drawCenteredString(this.fontRenderer, "Forge Mod Loader could not connect to this server", this.width / 2, offset, 0xFFFFFF);
        offset += 10;
        this.drawCenteredString(this.fontRenderer, "Client-server mods versions mismatch!", this.width / 2, offset, 0xFFFFFF);
        offset += 10;
        this.drawCenteredString(this.fontRenderer, "Do you want to sync your mods with this server?", this.width / 2, offset, 0xFFFFFF);
        offset += 5;
        super.drawScreen(par1, par2, par3);
    }
}
