package focssy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.minecraft.client.Minecraft;

public class FocssyUpdater implements Runnable {

	private String mcDir;
	private String modpackUrl;
	private FocssyUpdaterScreen scrInst;
	
	private ArrayList<String[]> localModlist = new ArrayList<String[]>();  
	private ArrayList<String> bModlist = new ArrayList<String>();           //badModsList       (without mcmod.info)
	private ArrayList<String> uModlist = new ArrayList<String>();           //unwantedModsList
	
    private boolean sayComplete=true;

	public FocssyUpdater(FocssyUpdaterScreen inst){
		scrInst = inst;
		mcDir = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator;
		modpackUrl = Focssy.instance.modpackUrl;
	}
	
	@Override
	public void run(){
		scrInst.updateStatus = update();
	}
	
	/**
	 * 
	 * @param str - string to say
	 * @param onScreen - display this on the updaterScreen?
	 */
  	public void say(String str, boolean onScreen){
  		if(str.contains("...")){
  			System.out.print("[Focssy] "+str);
  			if(onScreen){
  				scrInst.console.add(str);
  			}
  			sayComplete=false;
  		}else{
  			if(sayComplete){
  				System.out.println("[Focssy] "+str);
  				if(onScreen){
  					scrInst.console.add(str);
  				}
  			}else{
  				System.out.println(str);
  				if(onScreen){
  					scrInst.console.set(scrInst.console.size()-1,scrInst.console.get(scrInst.console.size()-1)+str);
  				}
  				sayComplete=true;
  			}	
  		}
  	}
    
    public String preparePath(String path){
		if(File.separator.compareTo("\\")==0){
			String convertedPath=path.replace('/', '\\');
			return convertedPath;
		}else{
			return path;
		}
	}
    
    public int downloadFile(String fileName){
    	say("Downloading "+ fileName +", please wait...",false);
		try{
			File newFile = new File(mcDir+preparePath(fileName));
			URL adress = new URL(modpackUrl+fileName);
			org.apache.commons.io.FileUtils.copyURLToFile(adress, newFile, 5000, 5000);
		}catch(IOException ex){
			say("ERROR!",false);
			//ex.printStackTrace();
			return 0;
		}
		say("DONE!",false);
		return 1;
	}
  
	public boolean loadBModlist(){
    	say("Loading bad mods list...",true);
    	try {
			URL badmods = new URL(modpackUrl+"bmodlist.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(badmods.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				bModlist.add(inputLine);
			}
			in.close();
		}catch (IOException e){
			say("ERROR!",true);
			//e.printStackTrace();
			return false;
		}
    	say("DONE!",true);
    	return true;
	}
    
	public boolean loadUModlist(){
    	say("Loading unwanted mods list...",true);
    	try {
			URL badmods = new URL(modpackUrl+"umodlist.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(badmods.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				uModlist.add(inputLine);
			}
			in.close();
		}catch (IOException e){
			say("ERROR!",true);
			//e.printStackTrace();
			return false;
		}
    	say("DONE!",true);
    	return true;
	}
	
	private boolean isUnwanted(String mod, String fn){
		for (String v : uModlist){
			if(mod.contains(v)){
				File file = new File(mcDir+"mods"+File.separator+fn);
				file.delete();
				return true;
			}
		}
		return false;
	}
	
	public void scanLocalMods(){
		int completor;
		
		String modId="";
		String modVer;
		String modFn;
		
		say("Scanning mods dir...",true);
    	File folder = new File(mcDir+"mods");
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && (listOfFiles[i].getName().contains(".jar")||listOfFiles[i].getName().contains(".zip"))) {
				modVer="---";
				modFn=listOfFiles[i].getName();
				
				try{
					ZipFile zf = new ZipFile(new File(mcDir+"mods"+File.separator+listOfFiles[i].getName()));
					ZipEntry ze = zf.getEntry("mcmod.info");
					if(ze!=null){
						completor=0;
						
						BufferedReader in = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
						String inputLine;
						while ((inputLine = in.readLine()) != null){
							if(inputLine.contains("\"modid\"")){
								completor=completor+1;
								modId=inputLine.substring(inputLine.indexOf('\"',inputLine.indexOf(':'))+1,inputLine.lastIndexOf('\"'));
							}else if(inputLine.contains("\"version\"")){
								completor=completor+1;
								modVer=inputLine.substring(inputLine.indexOf('\"',inputLine.indexOf(':'))+1,inputLine.lastIndexOf('\"'));
							}
							if(completor==2){
								break;	
							}
						}
						in.close();
						zf.close();
					}else{
						for (int j = 0; j < bModlist.size(); j++){
							if(listOfFiles[i].getName().contains(bModlist.get(j))){
								modId=bModlist.get(j);
							}
						}
					}
				}catch (IOException e){
					e.printStackTrace();
				}
				if(!isUnwanted(modId,modFn)){
					localModlist.add(new String[]{modId,modVer,modFn});
					//System.out.println(modId+"   "+modVer+"   "+modFn);
				}
			}
		}
		say("DONE!",true);
		say("Modfiles found: "+ localModlist.size(),true);
	}
    
	public int checkModpack(){
		int res=0;
		say("Sinchronizing mods, please wait.",true);
		try {
			URL modlist = new URL(modpackUrl+"modlist.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(modlist.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				if(checkMod(inputLine)!=0){
					res=1;
				}
			}
			in.close();
		}catch (IOException e){
			e.printStackTrace();
			return 0;
		}
		return res;
	}
	
	public int checkMod(String line){
		String[] smod = line.split("   ",3);
		int res=-1;
		int i;
		for(i = 0; i<localModlist.size(); i++){
			if(localModlist.get(i)[0].compareTo(smod[0])==0){
				if(localModlist.get(i)[1].compareTo("---")==0){
					//badmod it is
					if(localModlist.get(i)[2].compareTo(smod[2])!=0){
						res=1;
					}else{
						res=0;
					}
					break;
				}else{
					if(localModlist.get(i)[1].compareTo(smod[1])!=0){
						res=1;
					}else{
						res=0;
					}
					break;
				}
			}
		}
				
		if(res==-1){
			say("New mod! Downloading " + smod[0],false);
			res=downloadFile("mods"+File.separator+smod[2]);	
		}else if(res==1){
			say("New version! Updating " + smod[0],false);
			File file = new File(mcDir+"mods"+File.separator+preparePath(localModlist.get(i)[2]));
			file.delete();
			res=downloadFile("mods"+File.separator+smod[2]);
		}
		return res;
	}
	
	public int syncConfigs(){
		say("Synchronizing mods configs",true);
		byte[] buffer = new byte[1024];
		String workingDir=mcDir;
		
		if(downloadFile("config.zip")==0){
			return 0;
		}
		
		try{
		ZipInputStream zis = new ZipInputStream(new FileInputStream(mcDir+"config.zip"));
		ZipEntry ze = zis.getNextEntry();
		
		//check the first ze if it's a dir "config"
		if(!ze.getName().contains("config")){
			workingDir=mcDir+"config"+File.separator;
		}
		
		while(ze!=null){
			File newFile = new File(workingDir + ze.getName());
			
			if(ze.isDirectory()){
				newFile.mkdir();
			}else{
				//just in case
				//new File(newFile.getParent()).mkdirs();
				FileOutputStream fos2 = new FileOutputStream(newFile);             
			 
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos2.write(buffer, 0, len);
				}
			 
				fos2.close();
			}
			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();
		
		File file = new File(mcDir+"config.zip");
		file.delete();
		
		}catch (IOException e){
			e.printStackTrace();
			return 0;
		}
		return 0;
	}
	
	public int update(){
		int res=0;
			if(!loadBModlist()){
	  		say("Couldn't connect to "+modpackUrl,true);
	  		return 0;
	  	}
		loadUModlist();
	  	scanLocalMods();
	  	res=checkModpack();
	  	
		if(res!=0){
  		syncConfigs();
			say("./------------------------------------------------------------------------\\.",true);
			say("<|WE'VE GOT UPDATE! CLIENT WILL BE STOPPED! PLEASE, RESTART IT MANUALLY!!!|>",true);
			say(".\\------------------------------------------------------------------------/.",true);
			return 1;
		}else{
			say("Can't find any new mods in server rep. Contact your server overlord.",true);
			return 0;
		}	
	}
}
