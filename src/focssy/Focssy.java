package focssy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid="focssy", version="0.7.1", useMetadata=true)
@NetworkMod(clientSideRequired=true)
public class Focssy{
	public String modpackUrl;
	public String version;
	public String mcDir;
	public boolean isClient;
	
	private ArrayList<String> localModlist = new ArrayList<String>();
	private ArrayList<String> bModlist = new ArrayList<String>();
	private List<ModContainer> aModlist = Loader.instance().getActiveModList();
	
	@Instance(value = "focssy")
	public static Focssy instance;
	
	@SidedProxy(clientSide="focssy.FocssyClientProxy", serverSide="focssy.FocssyServerProxy")
	public static FocssyServerProxy sProxy;
	
	public Focssy(){
		File dir = new File("");
		mcDir = dir.getAbsolutePath()+File.separator;
		isClient = false;
		instance=this;
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent evt){
    	version = evt.getModMetadata().version;
		Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
    	config.load();
    	modpackUrl = config.get(Configuration.CATEGORY_GENERAL, "modpackUrl", "http://place.your.url.here/").getString();
    	config.save();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt){
		if(!isClient){
			FocssyUpdater updater = new FocssyUpdater();
	        updater.serverRun();
		}
	}
}
