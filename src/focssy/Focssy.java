package focssy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.client.Minecraft;
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

@Mod(modid="focssy", useMetadata=true)
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
		System.out.println("[Focssy] Welcome to Focssy"+version+"!");
		FocssyUpdater updater = new FocssyUpdater();
		if(!isClient){
	        updater.serverRun();
		}else{
			if(updater.clientRun()==1){
				summonAssasin();
            	Minecraft.getMinecraft().shutdownMinecraftApplet();
			}
		}
	}
	
	public void summonAssasin(){
		byte[] buffer = new byte[1024];
		File fa = new File("focssyAssasin.jar");
		try {
			if(!fa.exists()){
	    		ZipFile zf = new ZipFile(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()));
				ZipEntry ze = zf.getEntry("focssyAssasin.jar");
				if(ze!=null){
					FileOutputStream fos2 = new FileOutputStream(fa);             
					InputStream zis = zf.getInputStream(ze);
					
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos2.write(buffer, 0, len);
					}
					
					zis.close();
					fos2.close();
				}
				zf.close();
				
			}
			java.lang.Runtime.getRuntime().exec("java -jar focssyAssasin.jar");
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
