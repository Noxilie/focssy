package focssy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.zip.ZipInputStream;

@Mod(modid="focssy", useMetadata=true)
public class Focssy{
    public String modpackUrl;
    public String version;
    public String mcDir;
    
    private int newModsCount;
    private int updModsCount;
	
    private ArrayList<String> modsToDelete = new ArrayList<String>();           //all outdated modfiles that should be deleted
    private ArrayList<String[]> localModlist = new ArrayList<String[]>();       //all modfiles(zip and jar) in mods dir and subdirs
    private ArrayList<String> uModlist = new ArrayList<String>();               //mods that no longer in modpack and should be deleted
    
    //hell yeah, these are global temp variables :p
    String modId,modVer;
	
    public Focssy(){
        File dir = new File("");
        mcDir = dir.getAbsolutePath()+File.separator;
                
        newModsCount=0;
        updModsCount=0;
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
        say("[Focssy] Welcome to Focssy"+version+"!\n");
        if(clientRun()==1){
            summonAssasin();
            cpw.mods.fml.common.FMLCommonHandler.instance().handleExit(0);
        }
    }

    //we can't delete modfile while it's not properly closed and that wouldn't happen untill minecraft will exit
    //so we need to run external program to delete outdated files after minecraft finish running
    public void summonAssasin(){
        byte[] buffer = new byte[1024];
        File fa = new File("focssyAssasin.jar");
        try {
            if(!fa.exists()){
                ZipFile zf = new ZipFile(new File(mcDir+"mods"+File.separator+"focssy.jar"));
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
        java.lang.Runtime.getRuntime().exec("java -jar focssyAssasin.jar kill");
		
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
    /**
    * @param str - string to say
    */
    public void say(String str){
        System.out.print(str);
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
            say("ERROR!\n");
            ex.printStackTrace();
            return 0;
        }
        say("DONE!\n");
        return 1;
    }

    private boolean isUnwanted(String modId, String modFn){
        for (String v : uModlist){
            if(modId.equals(v)){
                modsToDelete.add(modFn);
                    return true;
            }
        }
        return false;
    }
    
    public boolean loadModlist(ArrayList<String> target, String name){
        try {
            URL remoteFile = new URL(modpackUrl+name);
            BufferedReader in = new BufferedReader(new InputStreamReader(remoteFile.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                target.add(inputLine);
            }
            in.close();
        }catch (IOException e){
            say("ERROR!\n");
            e.printStackTrace();
            return false;
        }
    	return true;
    }
    
    public int getInfo(ZipFile zf, String entryName) throws IOException{
        int completor=0;
        String buf;
        
        ZipEntry ze = zf.getEntry(entryName);
        if(ze==null){
            return 0;
        }else{
            BufferedReader in = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                if(inputLine.contains("\"modid\"")){
                    buf=inputLine.substring(inputLine.indexOf('\"',inputLine.indexOf(':'))+1,inputLine.lastIndexOf('\"'));
                    if(buf.length()>0){
                        modId=buf;
                        completor++;
                    }
                }else if(inputLine.contains("\"version\"")){
                    buf=inputLine.substring(inputLine.indexOf('\"',inputLine.indexOf(':'))+1,inputLine.lastIndexOf('\"'));
                    if(buf.length()>0){
                        modVer=buf;
                        completor++;
                    }
                }
                if(completor==2){
                    break;	
                }
            }
            in.close();
            if(completor==2){
                return 1;
            }else{
                return -1;
            }
        }
    }
    
    public void scanDir(String dir, ArrayList<String[]> target){
	String modFn;
        boolean isUnknown;
        int res;
		
    	File folder = new File(dir);
    	if(!folder.exists()){
    		folder.mkdir();
    	}
	File[] listOfFiles = folder.listFiles();
		
	say("Scanning "+ dir+"\n");
	for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()){
                if((listOfFiles[i].getName().contains(".jar")||listOfFiles[i].getName().contains(".zip"))) {
                    isUnknown=false;
                    modId="---";
                    modVer="---";
                    modFn=listOfFiles[i].getPath().replace(mcDir+"mods"+File.separator, "");
                    
                    try{
                        ZipFile zf = new ZipFile(new File(dir+File.separator+listOfFiles[i].getName()));
                        res=getInfo(zf,"focssy.info");
                        if(res<1){
                            res=getInfo(zf,"mcmod.info");
                            if(res<1){
                                isUnknown=true;
                            }
                        }
                        zf.close();
                        
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    if(!isUnwanted(modId,modFn) && !isUnknown){
                        localModlist.add(new String[]{modId,modVer,modFn});
                    }
                }
            }else{ //it's a directory
                scanDir(listOfFiles[i].getPath(), target);
            }
        }
    }
    
	public int checkModpack(){
		int res=0;
		say("Sinchronizing mods, please wait.\n");
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
			if(localModlist.get(i)[0].compareTo(smod[0])==0){ //found it in local modlist
				//check filename
				if(localModlist.get(i)[2].compareTo(preparePath(smod[2]))!=0){
					//we have a new filename here, new version perhaps
					res=1;
				}else{
					//ok, let's check version number
					if(localModlist.get(i)[1].compareTo(smod[1])!=0){
						res=1;
					}else{
						res=0;
					}
				}
				break;
			}
		}
				
		if(res==-1){
			say("New mod! Downloading " + smod[0]+"\n");
			res=downloadFile("mods/"+smod[2]);
			newModsCount=newModsCount+res;
		}else if(res==1){
			say("New version! Updating " + smod[0]+"\n");
			//mark for deletion at the end
			if(!localModlist.get(i)[2].equals(smod[2])){
				modsToDelete.add(localModlist.get(i)[2]);
			}
			res=downloadFile("mods/"+smod[2]);
			updModsCount=updModsCount+res;
		}
		return res;
	}
	
	public int syncConfigs(){
		say("Synchronizing mods configs\n");
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
	
	public int clientRun(){
		int res;
		
                if(!loadModlist(uModlist,"umodlist.txt")){
                    return 0;
                }
                
		say("Searching for installed mods\n");
	  	scanDir(mcDir+"mods", localModlist);
	  	say("Modfiles found: "+ localModlist.size()+"\n");

	  	res=checkModpack();
	  	say("Total files downloaded:\t"+(newModsCount+updModsCount)+"\n");
	  	say("New:\t\t\t"+newModsCount+"\n");
	  	say("Updated:\t\t\t"+updModsCount+"\n");
	  	
		if(res!=0){
			syncConfigs();
			
			if(modsToDelete.size()>0){
				PrintWriter writer;
				try {
					writer = new PrintWriter("order", "UTF-8");
						for (String text : modsToDelete) {
							writer.println(text);
					    }
					writer.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			
			say("./------------------------------------------------------------------------\\.\n");
			say("<|WE'VE GOT UPDATE! CLIENT WILL BE STOPPED! PLEASE, RESTART IT MANUALLY!!!|>\n");
			say(".\\------------------------------------------------------------------------/.\n");
			return 1;
		}else{
			say("Can't find any new mods\n");
			return 0;
		}
	}
}
