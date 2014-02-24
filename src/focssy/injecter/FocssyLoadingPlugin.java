package focssy.injecter;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@IFMLLoadingPlugin.MCVersion("1.6.4")
public class FocssyLoadingPlugin implements IFMLLoadingPlugin {
public static File location;

	@Override
	@Deprecated
	public String[] getLibraryRequestClass(){
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String[] getASMTransformerClass(){
		return new String[]{FocssyClassTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass(){
		return null;
	}

	@Override
	public String getSetupClass(){
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
	}
}
