package focssyScanner;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Mod(modid="focssyscanner", useMetadata=true)
public class FocssyScanner{
    public String version;
    public String mcDir;
    public int modsScanned=0;
    
    public String modId, modVer;
    
    private ArrayList<String[]> localModlist = new ArrayList<String[]>();	//all our modfiles(zip and jar) in mods dir and subdirs
    private ArrayList<String> bModlist = new ArrayList<String>();		//bad mods (without mcmod.info)
    private ArrayList<String> uModlist = new ArrayList<String>();               //unknown mods
    private List<ModContainer> aModlist = Loader.instance().getActiveModList(); //mods currently loaded by minecraft forge
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt){
    	version = evt.getModMetadata().version;
        File dir = new File("");
        mcDir = dir.getAbsolutePath()+File.separator;
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt){
        say("[Focssy] Welcome to FocssyScanner"+version+"!\n");
        //let's our scanner do all the work ^___^
        scanDir(mcDir+"mods");
        PrintWriter writer;
        try {
            //make modlist-file
            writer = new PrintWriter("modlist.txt", "UTF-8");
            for (String[] text : localModlist){
                writer.println(text[0]+"   "+text[1]+"   "+text[2]);
            }
            writer.close();
        
            //make unknownModlist-file if there are any unknown mods
            if(uModlist.size()>0){
                writer = new PrintWriter("unknownMods.txt", "UTF-8");
                for (String text : uModlist){
                    writer.println("---   ---   "+text);
                }
                writer.print(System.lineSeparator()+"-----"+System.lineSeparator()+"all mods:"+System.lineSeparator()+"-----"+System.lineSeparator());
                for (ModContainer mod : aModlist){
                    writer.println(mod.getModId()+"   "+mod.getVersion());
                }
                writer.close();
            }
            
            say("\n--------------\nScanned mods:\t" + modsScanned+"\n");
            say("Corrected:\t" + bModlist.size()+"\n");
            say("Unknown:\t" + uModlist.size()+"\n\n");
            //any bad mods that we should "correct"?
            if(bModlist.size()>0){
                writer = new PrintWriter("order", "UTF-8");
                for (String text : bModlist) {
                    writer.println(text);
                }
                writer.close();
                summonAssasin();
                say("./------------------------------------------------------------------------\\.\n");
		say("<|        CLIENT WILL BE STOPPED! PLEASE, RESTART IT MANUALLY!!!           |>\n");
		say(".\\------------------------------------------------------------------------/.\n");
                cpw.mods.fml.common.FMLCommonHandler.instance().handleExit(0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    /**
    * @param str - string to say
    */
    public void say(String str){
        System.out.print(str);
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
    
    public void scanDir(String dir){
	int completor;
	int modStatus;
        
	String modFn;
	
        //rudiment???
    	File folder = new File(dir);
    	if(!folder.exists()){
    		folder.mkdir();
    	}
	File[] listOfFiles = folder.listFiles();
		
	say("--Scanning "+ dir+"\n");
        for(int i = 0; i < listOfFiles.length; i++){
            if(listOfFiles[i].isFile()){
                if((listOfFiles[i].getName().contains(".jar")||listOfFiles[i].getName().contains(".zip"))) {
                    int mstat, fstat=-2;
                    modsScanned++;
                    modStatus=0;
                    modId="---";
                    modVer="---";
                    modFn=listOfFiles[i].getPath().replace(mcDir+"mods"+File.separator, "");
                    completor=0;
                        
                    try{
                        ZipFile zf = new ZipFile(new File(dir+File.separator+listOfFiles[i].getName()));
                        fstat=getInfo(zf,"focssy.info");
                        if(fstat==0){
                            mstat=getInfo(zf,"mcmod.info");
                            if(mstat<1){
                                modStatus=1;
                                //no mcmod.info, or it's not valid try to search in activeMods
                                for (int j = 0; j < aModlist.size(); j++){
                                    if(listOfFiles[i].getName().equals(aModlist.get(j).getSource().getName())){
                                        if(modId.equals("---"))modId=aModlist.get(j).getModId();
                                        if(modVer.equals("---"))modVer=aModlist.get(j).getVersion();
                                        completor=2;
                                        break;  
                                    }
                                }
                                if(completor!=2){
                                    modStatus=2;
                                }
                            }
                        }else if(fstat==-1){
                            modStatus=2;
                        }
                        zf.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    //System.out.println(modId+"   "+modVer+"   "+modFn);
                    //So, after all, is this mod is bad? Should we correct it?
                    if(modStatus==0){
                        say("---Good mod found : "+modFn+"\n");
                    }else if(modStatus==1){
                        bModlist.add(modFn+";"+modId+";"+modVer);
                        say("-----Mod marked as bad : "+modFn+"\n");
                    }else{
                        //this is unknown mod! reprort it...
                        say("-----ACHTUNG! Unknown mod! : "+modFn);
                        uModlist.add(modFn);
                        if(fstat==0){
                            bModlist.add(modFn+";;");
                            say(" (without focssy.info, corrected)");
                        }
                        say("\n");
                    }
                    //if mod isn't unknown then add it to modlist, and don't add ourselves :)
                    if(modStatus!=2 && !modId.equals("focssyscanner")){
                        localModlist.add(new String[]{modId,modVer,modFn});
                    }
                }
            }else{ //it's a directory, we need to go deeper
                scanDir(listOfFiles[i].getPath());
            }
        }
    }
    
    public void summonAssasin(){
        byte[] buffer = new byte[1024];
        File fa = new File("focssyAssasin.jar");
        try {
            if(!fa.exists()){
                ZipFile zf = new ZipFile(new File(mcDir+"mods"+File.separator+"focssyScanner.jar"));
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
            java.lang.Runtime.getRuntime().exec("java -jar focssyAssasin.jar mark");
		
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}