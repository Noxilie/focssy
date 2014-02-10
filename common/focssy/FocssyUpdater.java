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
	private ArrayList<String> badModsNames = new ArrayList<String>();
	
    private boolean sayComplete=true;

	public FocssyUpdater(FocssyUpdaterScreen inst){
		scrInst = inst;
		mcDir = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator;
		modpackUrl = Focssy.instance.modpackUrl;
	}
	
	@Override
	public void run(){
		System.out.println("UpdateStatus is " + scrInst.updateStatus);
		update();	
		scrInst.updateStatus = 0;
		System.out.println("UpdateStatus is " + scrInst.updateStatus);
	}
	
  	public void say(String str){
  		if(str.contains("...")){
  			System.out.print("[Focssy] "+str);
  			scrInst.console.add(str);
  			sayComplete=false;
  		}else{
  			if(sayComplete){
  				System.out.println("[Focssy] "+str);
  				scrInst.console.add(str);
  			}else{
  				System.out.println(str);
  				scrInst.console.set(scrInst.console.size()-1,scrInst.console.get(scrInst.console.size()-1)+str);
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
    	say("Downloading "+ fileName +", please wait...");
		try{
			File newFile = new File(mcDir+preparePath(fileName));
			URL adress = new URL(modpackUrl+fileName);
			org.apache.commons.io.FileUtils.copyURLToFile(adress, newFile, 5000, 5000);
		}catch(IOException ex){
			say("ERROR!");
			ex.printStackTrace();
			return 0;
		}
		say("DONE!");
		return 1;
	}
  //---------------------
  
	public boolean loadBadModsNames(){
    	say("Getting bad mods list...");
    	try {
			URL badmods = new URL(modpackUrl+"badmods.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(badmods.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				badModsNames.add(inputLine);
			}
			in.close();
		}catch (IOException e){
			say("ERROR!");
			e.printStackTrace();
			return false;
		}
    	say("DONE!");
    	return true;
	}
    
	public void loadLocalModlist(){
		int completor;
		
		String modId="";
		String modVer;
		String modFn;
		
		say("Scanning mods dir...");
    	File folder = new File(mcDir+"mods");
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && (listOfFiles[i].getName().contains(".jar")||listOfFiles[i].getName().contains(".zip"))) {
//System.out.println("Examining - "+listOfFiles[i].getName());
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
						for (int j = 0; j < badModsNames.size(); j++){
							if(listOfFiles[i].getName().contains(badModsNames.get(j))){
								modId=badModsNames.get(j);
							}
						}
					}
				}catch (IOException e){
					e.printStackTrace();
				}
//System.out.println("adding "+modId+" "+modVer+" to modlist");
				localModlist.add(new String[]{modId,modVer,modFn});
//System.out.println("added "+localModlist.get(localModlist.size()-1)[0]);
			}
		}
		say("DONE!");
		say("Modfiles found: "+ localModlist.size());
	}
    
	public int checkModpack(){
		int res=0;
		say("Checking modpack for updates...");
		try {
			URL modlist = new URL(modpackUrl+"modlist.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(modlist.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				res=checkMod(inputLine);
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
//System.out.println("Searching |"+smod[0]+"| in modlist");
//System.out.println("Size of modlist - "+localModlist.size());
		for(i = 0; i<localModlist.size(); i++){
//System.out.println("Is it "+ i +" |"+localModlist.get(i)[0] + "|?");
			if(localModlist.get(i)[0].compareTo(smod[0])==0){
				if(localModlist.get(i)[1].compareTo("---")==0){
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
			say("New mod! Downloading " + smod[0]);
			res=downloadFile("mods"+File.separator+smod[2]);	
		}else if(res==1){
			say("New version! Updating " + smod[0]);
			File file = new File(mcDir+"mods"+File.separator+preparePath(localModlist.get(i)[2]));
			file.delete();
			res=downloadFile("mods"+File.separator+smod[2]);
		}
		return res;
	}
	
	public int syncConfigs(){
		byte[] buffer = new byte[1024];
		
		if(downloadFile("config.zip")==0){
			return 0;
		}
		
		try{
		ZipInputStream zis = new ZipInputStream(new FileInputStream(mcDir+"config.zip"));
		ZipEntry ze = zis.getNextEntry();
		
		while(ze!=null){
			File newFile = new File(mcDir + ze.getName());
			
			//create all non exists folders
			//else you will hit FileNotFoundException for compressed folder
			//new File(newFile.getParent()).mkdirs();
			
			if(newFile.isDirectory()){
				newFile.mkdirs();
			}else{
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
			if(!loadBadModsNames()){
	  		say("Couldn't connect to "+modpackUrl);
	  		return 0;
	  	} 	
	  	loadLocalModlist();
	  	res=checkModpack();
	  	
		if(res!=0){
  		syncConfigs();
			say("./------------------------------------------------------------------------\\.");
			say("<|WE'VE GOT UPDATE! CLIENT WILL BE STOPPED! PLEASE, RESTART IT MANUALLY!!!|>");
			say(".\\------------------------------------------------------------------------/.");
			return 1;
			//Minecraft.getMinecraft().shutdownMinecraftApplet();
			//System.exit(1);
		}else{
			say("Can't find any new mods in server rep. Contact your server overlord.");
			return 0;
		}	
	}
}
