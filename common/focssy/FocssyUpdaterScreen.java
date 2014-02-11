package focssy;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.client.FMLClientHandler;

public class FocssyUpdaterScreen extends GuiScreen {

	public ArrayList<String> console  = new ArrayList<String>();
	public int updateStatus;
	
	private GuiButton btnDone;
	private boolean btnStatus;
	
	private FocssyUpdater updater;
	private Thread tUpdater;
	
	public FocssyUpdaterScreen(){
		updater = new FocssyUpdater(this);
		tUpdater = new Thread(updater);
		btnStatus=false;
		updateStatus=-1;
        tUpdater.start();
	}

	
	@Override
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui(){
        this.buttonList.add(btnDone = new GuiSmallButton(1, this.width / 2 - 75, this.height - 38, I18n.getString("gui.done")));
        btnDone.enabled=btnStatus;
    }

    @Override

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton){
        if (par1GuiButton.enabled && par1GuiButton.id == 1){
            if(updateStatus==1){
            	Minecraft.getMinecraft().shutdownMinecraftApplet();
            }else{
            	FMLClientHandler.instance().getClient().displayGuiScreen(null);
            }
        }
    }

    @Override
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3){
    	ArrayList<String> consoleClone;
    	
    	if(updateStatus>-1){
    		btnDone.enabled=true;
        }
    	this.drawDefaultBackground();
        int offset = 10;
        consoleClone = new ArrayList<String>(console);
        for (String v : consoleClone){
        	this.drawCenteredString(this.fontRenderer, v, this.width / 2, offset, 0xFFFFFF);
        	offset += 10;
        }
        super.drawScreen(par1, par2, par3);
    }   
}
