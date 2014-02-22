package focssy;

import java.util.Arrays;

import net.minecraftforge.common.Configuration;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Focssy extends DummyModContainer{
	public static String modpackUrl;
	public ModMetadata meta;
	
	@Instance(value = "focssy")
	public static Focssy instance;
	
	public Focssy(){
		super(new ModMetadata());
		meta = getMetadata();
		meta.modId = "focssy";
		meta.name = "Focssy";
		meta.version = "0.4";
		meta.credits = "";
		meta.authorList = Arrays.asList("Noxilie");
		meta.description = "Forge client-server synchronizer";
		meta.url = "";
		meta.updateUrl = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";	
		
		instance=this;
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
	
	@Subscribe
	public void preInit(FMLPreInitializationEvent evt) {
    	Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
    	config.load();
    	modpackUrl = config.get(Configuration.CATEGORY_GENERAL, "modpackUrl", "http://place.your.url.here/").getString();
    	config.save();
	}

}
