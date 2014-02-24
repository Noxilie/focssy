package focssy;

import net.minecraftforge.common.Configuration;

import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid="focssy", useMetadata=true)
@NetworkMod(clientSideRequired=true)
public class Focssy{
	public static String modpackUrl;
	public String version="0.5";
	
	@Instance(value = "focssy")
	public static Focssy instance;
	
	public Focssy(){
		instance=this;
	}
	
	@Subscribe
	public void preInit(FMLPreInitializationEvent evt) {
    	Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
    	config.load();
    	modpackUrl = config.get(Configuration.CATEGORY_GENERAL, "modpackUrl", "http://place.your.url.here/").getString();
    	config.save();
	}

}
