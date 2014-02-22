package focssy.injecter;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import focssy.Focssy;

public class FocssyLoadingPlugin implements IFMLLoadingPlugin {
public static File location;
	
	@Override
	@Deprecated
	public String[] getLibraryRequestClass(){
		return null;
	}

	@Override
	public String[] getASMTransformerClass(){
		return new String[]{FocssyClassTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass(){
		return Focssy.class.getName();
	}

	@Override
	public String getSetupClass(){
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
	}
}
