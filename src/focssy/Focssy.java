package focssy;

import java.io.File;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid="focssy", version="0.5.2", useMetadata=true)
@NetworkMod(clientSideRequired=true)
public class Focssy{
	public String modpackUrl;
	public String version;
	public String mcDir;
	
	@Instance(value = "focssy")
	public static Focssy instance;
	
	@SidedProxy(clientSide="focssy.FocssyClientProxy", serverSide="focssy.FocssyServerProxy")
	public static FocssyServerProxy sProxy;
	
	public Focssy(){
		File dir = new File("");
		mcDir = dir.getAbsolutePath()+File.separator;
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

}
