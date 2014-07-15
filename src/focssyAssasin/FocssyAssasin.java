package focssyAssasin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FocssyAssasin{
    
    public static void main(String[] args) {
        ArrayList<String> victims = new ArrayList<String>();   //our DethNote
        String mcDir;
        String[] details;
        
        File dir = new File("");
	mcDir = dir.getAbsolutePath()+File.separator;
        
        //read deathNote
        try{
            String line = null;
            FileReader fr = new FileReader("order");
            BufferedReader br = new BufferedReader(fr);

            while((line = br.readLine()) != null) {
                victims.add(line);
            }
            br.close();
            fr.close();
        }catch(Exception ex){
            return;
        }
        
        //give minecraft-process some time to close and exit
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            Logger.getLogger(FocssyAssasin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //do all dirty job
        try{
            System.gc();
            for(String victim : victims){
                if(args[0].equals("kill")){
                    File file = new File(mcDir+"mods"+File.separator+victim);
                    System.out.println("[FocssyAssasin] Assasinating victim: "+mcDir+"mods"+File.separator+victim);
                    file.setWritable(true);
                    file.delete();
                }else if(args[0].equals("mark")){
                    details = victim.split(";",3);
                    Map<String, String> env = new HashMap<>(); 
                    env.put("create", "true");
                    Path path = Paths.get(mcDir+"mods"+File.separator+details[0]);
                    URI uri = URI.create("jar:" + path.toUri());
                    try (FileSystem fs = FileSystems.newFileSystem(uri, env)){
                        Path nf = fs.getPath("focssy.info");
                        try (Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                            writer.write("\"modid\": \""+details[1]+"\",\n\"version\": \""+details[2]+"\",");
                            writer.close();
                        }
                        fs.close();
                    }
                }
            }
	}catch(Exception ex){
            ex.printStackTrace();
	}
        
        //clean all evidence
        File file = new File("order");
        file.delete();
        System.exit(0);
    }
}
