package focssy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class FocssyUpdater implements Runnable {

	private String mcDir;
	private String modpackUrl;
	private FocssyUpdaterScreen scrInst;
	private int newModsCount;
	private int updModsCount;
	private boolean isClient;
	private boolean newBadMod;
	
	private ArrayList<String[]> localModlist = new ArrayList<String[]>();  
	private ArrayList<String> bModlist = new ArrayList<String>();          //badModsList       (without mcmod.info)
	private ArrayList<String> uModlist = new ArrayList<String>();           //unwantedModsList
	private List<ModContainer> aModlist = Loader.instance().getActiveModList();
	
    private boolean sayComplete=true;

	public FocssyUpdater(FocssyUpdaterScreen inst){
		scrInst = inst;
		init();
	}
	
	public FocssyUpdater(){
		scrInst = null;
		init();
	}
	
	private void init(){
		newBadMod = false;
		mcDir = focssy.Focssy.instance.mcDir;
		modpackUrl = focssy.Focssy.instance.modpackUrl;
		isClient = focssy.Focssy.instance.isClient;
		newModsCount=0;
		updModsCount=0;
	}
	
	@Override
	public void run(){
		if(isClient){
			scrInst.updateStatus = update();
		}else{
			createLists();
		}
	}
	
	/**
	 * 
	 * @param str - string to say
	 * @param onScreen - display this on the updaterScreen?
	 */
  	public void say(String str, boolean onScreen){
  		if(str.contains("...")){
  			System.out.print("[Focssy] "+str);
  			if(onScreen && scrInst!=null){
  				scrInst.console.add(str);
  			}
  			sayComplete=false;
  		}else{
  			if(sayComplete){
  				System.out.println("[Focssy] "+str);
  				if(onScreen  && scrInst!=null){
  					scrInst.console.add(str);
  				}
  			}else{
  				System.out.println(str);
  				if(onScreen  && scrInst!=null){
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
			ex.printStackTrace();
			return 0;
		}
		say("DONE!",false);
		return 1;
	}
  
	public boolean loadrModlist(ArrayList<String> lst, String Fn){
    	say("Loading "+Fn+"...",true);
    	try {
			URL remoteFile = new URL(modpackUrl+Fn);
			BufferedReader in = new BufferedReader(new InputStreamReader(remoteFile.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				lst.add(inputLine);
			}
			in.close();
		}catch (IOException e){
			say("ERROR!",true);
			e.printStackTrace();
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
	
	public void scanDir(String dir){
		int completor;
		
		String modId;
		String modVer;
		String modFn;
		
    	File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		
		say("Scanning "+ dir, false);
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()){
				if((listOfFiles[i].getName().contains(".jar")||listOfFiles[i].getName().contains(".zip"))) {
					modId="---";
					modVer="---";
					modFn=listOfFiles[i].getPath().replace(mcDir+"mods"+File.separator, "");
					
					try{
						ZipFile zf = new ZipFile(new File(dir+File.separator+listOfFiles[i].getName()));
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
							//no mcmod.info, try to search in activeMods
							for (int j = 0; j < aModlist.size(); j++){
								if(listOfFiles[i].getName().equals(aModlist.get(j).getSource().getName())){
									modId=aModlist.get(j).getModId();
									modVer=aModlist.get(j).getVersion();
									break;
								}
							}
						}
					}catch (IOException e){
						e.printStackTrace();
					}
					
					//last chance - search the mod in badModlist
					if(modId.equals("---")){
						for (int j = 0; j < bModlist.size(); j++){
							if(listOfFiles[i].getName().contains(bModlist.get(j))){
								modId=bModlist.get(j);
								break;
							}
						}
					}
					
					
					//System.out.println(modId+"   "+modVer+"   "+modFn);
					if(isClient){
						if(!isUnwanted(modId,modFn)){
							localModlist.add(new String[]{modId,modVer,modFn});
						}
					}else{
						if(modId.equals("---")){
							modId=modFn;
							bModlist.add(modFn);
							newBadMod=true;
						}
						localModlist.add(new String[]{modId,modVer,modFn});
					}
				}
			}else{
				scanDir(listOfFiles[i].getPath());
			}
		}
		say("Modfiles found: "+ localModlist.size(),false);
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
					if(localModlist.get(i)[2].compareTo(preparePath(smod[2]))!=0){
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
			res=downloadFile("mods/"+smod[2]);
			newModsCount=newModsCount+res;
		}else if(res==1){
			say("New version! Updating " + smod[0],false);
			File file = new File(mcDir+"mods"+File.separator+localModlist.get(i)[2]);
			file.delete();
			res=downloadFile("mods/"+smod[2]);
			updModsCount=updModsCount+res;
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
		
		say("Welcome to Focssy"+focssy.Focssy.instance.version+"!",true);
		if(!loadrModlist(bModlist,"bmodlist.txt")){
	  		say("Couldn't connect to "+modpackUrl,true);
	  		return 0;
	  	}
		loadrModlist(uModlist,"umodlist.txt");
		say("Searching for installed mods", true);
	  	scanDir(mcDir+"mods");
	  	say("Modfiles found: "+ localModlist.size(),true);

	  	res=checkModpack();
	  	say("Total files downloaded: "+(newModsCount+updModsCount),true);
	  	say("Mods new:     "+newModsCount,true);
	  	say("Mods updated: "+updModsCount,true);
		if(res!=0){
  		syncConfigs();
			say("./------------------------------------------------------------------------\\.",true);
			say("<|WE'VE GOT UPDATE! CLIENT WILL BE STOPPED! PLEASE, RESTART IT MANUALLY!!!|>",true);
			say(".\\------------------------------------------------------------------------/.",true);
			return 1;
		}else{
			say("Something is wrong. Look at your minecraft log and contact your server overlord!",true);
			return 0;
		}
	}
	
	public void createLists(){
		say("Welcome to Focssy"+focssy.Focssy.instance.version+"!",false);
		loadrModlist(bModlist,"bmodlist.txt");
		say("Searching modfiles",false);
		scanDir(mcDir+"mods");
		
		PrintWriter writer;
		try {
			say("Writing modlist.txt",false);
			writer = new PrintWriter("modlist.txt", "UTF-8");
				for (String[] text : localModlist) {
					writer.println(text[0]+"   "+text[1]+"   "+text[2]);
			    }
			writer.close();
			
			say("Writing bmodlist.txt",false);
			if(newBadMod){
				say("New badMods detected! Please, correct file bmodlist.txt manually!",false);
			}
			writer = new PrintWriter("bmodlist.txt", "UTF-8");
				for (String text : bModlist) {
					writer.println(text);
			    }
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
