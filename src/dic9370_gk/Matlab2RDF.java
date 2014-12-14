package dic9370_gk;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Enridestroy
 * Ce morceau de code provient d'un autre projet personnel qui permet de convertir plusieurs formats de données vers RDF dont Matlab
 */
public class Matlab2RDF {
    final int STRUCTURE = 0;
    final int CHAR = 1;
    final int CELL = 2;
    final int DOUBLE = 3;
    
    public static ArrayList<double[][]> ams = new ArrayList<>();
    
    public void convert(){
        //String fileName = "mixoutALL_shifted.mat";
        String fileName = "MUTAG.mat";
        String name;
        
        
        //read array form file
        MatFileReader mfr = null;
        try {
            mfr = new MatFileReader(fileName);
        } catch (IOException ex) {
            Logger.getLogger(Matlab2RDF.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(mfr==null) return;
        
        //on recupere le contenu dans une map
        Map<String, MLArray> content = mfr.getContent();
        //on charge toutes les cles du fichier
        ArrayList<String> allkeys = new ArrayList<>();
        for(String key : content.keySet()){
            System.out.println("here are some keys :"+key);
            allkeys.add(key);
        }
        //on prend la premiere cle
        name = allkeys.get(0);//constantes
        
      
        for(String k : allkeys){

            ArrayList<MLArray> stack = new ArrayList<>();
            //on ajoute l'element cree a la liste des elements a gerer
                        
            ArrayList<ArrayList<String>> metadata = new ArrayList<>();
            //on prend chaque tableau (structure)
            MLArray mlArrayRetrived0 = mfr.getMLArray(k);
            System.out.println("get_class:"+mlArrayRetrived0.getClass());
            stack.add(mlArrayRetrived0);
            
            metadata.add(new ArrayList<String>());
            
            while(stack.size()>0){
                MLArray mlArrayRetrived = stack.get(0);
                
                ArrayList<String> _metadata;
                if(metadata.isEmpty()){
                    _metadata = new ArrayList<>();
                }
                else{
                    _metadata = metadata.get(0);
                }
                MATLAB_ENUM findType = this.findType(mlArrayRetrived);
                switch(findType){
                    case STRUCTURE:                    
                    {
                        int msize = _metadata.size();
                        Object[] wrapper = this.handleStructure(mlArrayRetrived, null, _metadata, null);
                        ArrayList<MLArray> subitems = (ArrayList<MLArray>) wrapper[0];
                        if(subitems.size()>0) {
                            stack.addAll(subitems);
                        }
                        /**
                         * Gestion des metadonees 
                         */
                        if(_metadata.size()>0 && msize < _metadata.size()){                               
                            //on calcule le nombre de lignes en fonction des attributs connus courrants
                             for(int i=0;i<subitems.size();i++){
                                ArrayList<String> aaa = new ArrayList<>();
                                aaa.add(_metadata.get(i % _metadata.size()));
                                metadata.add(aaa);
                            }
                        }

                        }//on va ddecomposer l'object complexe en sous objets
                        break;
                    case CELL:
                    {
                        int msize = _metadata.size();
                        Object[] wrapper = this.handleCell(mlArrayRetrived, null, _metadata, null);
                        ArrayList<MLArray> subitems = (ArrayList<MLArray>) wrapper[0];
                        if(subitems.size()>0) {
                            stack.addAll(subitems);
                        }

                        /**
                         * Gestion des metadonees 
                         */
                        if(_metadata.size()>0 && msize < _metadata.size()){
                            for(int i=0;i<subitems.size();i++){
                                ArrayList<String> aaa = new ArrayList<>();
                                aaa.add(_metadata.get(i % _metadata.size()));
                                metadata.add(aaa);
                            }
                        }
                            
                    }
                    break;
                    case DOUBLE:
                        {
                            int msize = _metadata.size();
                            Object[] wrapper = this.handleDouble(mlArrayRetrived, null, _metadata, null);
                            ArrayList<MLArray> subitems = (ArrayList<MLArray>) wrapper[0];
                            if(subitems.size()>0) {
                                stack.addAll(subitems);
                            }
                             
                            /**
                             * Gestion des metadonees 
                             */
                            if(_metadata.size()>0 && msize < _metadata.size()){
                                for(int i=0;i<subitems.size();i++){
                                    ArrayList<String> aaa = new ArrayList<>();
                                    aaa.add(_metadata.get(i % _metadata.size()));
                                    metadata.add(aaa);
                                }  metadata.add(_metadata);
                            }
                            
                        }   

                        break;
                    case CHAR:
                        {
                            int msize = _metadata.size();
                            Object[] wrapper = this.handleChar(mlArrayRetrived, null, _metadata, null);
                            ArrayList<MLArray> subitems = (ArrayList<MLArray>) wrapper[0];
                            
                            
                            if(subitems.size()>0) {
                                stack.addAll(subitems);
                            }
                            
                            /**
                             * Gestion des metadonees 
                             */
                            if(_metadata.size()>0 && msize < _metadata.size()){
                                for(int i=0;i<subitems.size();i++){
                                    ArrayList<String> aaa = new ArrayList<>();
                                    aaa.add(_metadata.get(i % _metadata.size()));
                                    metadata.add(aaa);
                                }
                            }
                        }

                        break;
                    default:
                        System.out.println("I don't know how to handle that....");
                        break;
                }
                stack.remove(0);
                if(!metadata.isEmpty())
                    metadata.remove(0);
            }  
        }
    }
    

    public MATLAB_ENUM findType(MLArray data){
        if(data.isCell()){
            return MATLAB_ENUM.CELL;
        }
        else if(data.isStruct()){
            return MATLAB_ENUM.STRUCTURE;
        }
        else if(data.isDouble()){
            return MATLAB_ENUM.DOUBLE;
        }
        else if(data.isChar()){
            return MATLAB_ENUM.CHAR;
        }
        else{
            return null;
        }
    }
    
    /**
     * 
     * @param data
     * @param map
     * @param metadata
     * @return 
     */
    public Object[] handleStructure(MLArray data, Object map, ArrayList<String> metadata, Object container){
        ArrayList<MLArray> arrayList = new ArrayList<>();

        //alors on transforme ca explicitement en structure
        MLStructure cell = (MLStructure)data;
        //pour chacun de ces champs, on recupere les noms
        Collection<String> allFields = cell.getFieldNames();
        String[][] props = new String[allFields.size()][3];
        int i=0;
        for(String ss : allFields){
            props[i][0] = ss;//nom
            props[i][1] = "...";//type
            props[i][2] = "...";//commentaires?
            i++;
        }        
        
        for(String l : allFields){
            //on convertit explicitement en champ
            MLArray field = cell.getField(l);
            metadata.add(l);
        }
        /**
         * ca represente tous les champs, a la suite, sans la distinction entre les colonnes.
         * faudrait faire un modulo colonne et creer des lignes de tables.
         * puis traiter ces lignes comme il faut.
         */
        for(MLArray ff : cell.getAllFields()){
            arrayList.add(ff);
        }

        Object[] wrapper = new Object[3];
        wrapper[0] = arrayList;
        return wrapper;
    }
    
    /**
     * 
     * @param data
     * @param map
     * @param metadata
     * @param container
     * @return 
     */
    public Object[] handleDouble(MLArray data, Object map, ArrayList<String> metadata, Object container){
        ArrayList<MLArray> arrayList = new ArrayList<>();
        
        //conversion explicite
        MLDouble arr = ((MLDouble)data);
        double[][] array = arr.getArray();
        int number_of_new_items = 0;
        int rows = 0;

        String s = "";
        if(!metadata.isEmpty())
            s = metadata.get(0);
        //
        
        if(s.equals("am")){
            ams.add(array);
        }

        for(double[] d : array){
            rows++;
            
            for(double dd : d){
                String key = ""+dd;
                number_of_new_items++;
            }
        }
        
        Object[] wrapper = new Object[4];
        wrapper[0] = arrayList;
        wrapper[3] = number_of_new_items;
        return wrapper;
    }
    
    /**
     * @param data
     * @param map
     * @param metadata
     * @param container
     * @return 
     */
    public Object[] handleChar(MLArray data, Object map, ArrayList<String> metadata, Object container){
        ArrayList<MLArray> arrayList = new ArrayList<>();
        ArrayList<HashMap> parents = new ArrayList<>();
        MLChar arr = ((MLChar)data);
        Character[] exportChar = arr.exportChar();
        StringBuilder b = new StringBuilder();
        for(Character c : exportChar){
            b.append(c);
        }

        String key = b.toString();
        
        
        String meta = "";
        if(!metadata.isEmpty())
            meta = metadata.get(0);
        
        Object[] wrapper = new Object[2];
        wrapper[0] = arrayList;
        wrapper[1] = parents;
        return wrapper;
    }
    
   
    /**
     * 
     * @param data
     * @param map
     * @param metadata
     * @return 
     */
    public Object[] handleCell(MLArray data, Object map, ArrayList<String> metadata, Object container){
        ArrayList<MLArray> arrayList = new ArrayList<>();
               
        MLCell arr = ((MLCell)data);
        
        String s = "";
        if(!metadata.isEmpty())
            s = metadata.get(0);
        //on recupere les sous-cases
        ArrayList<MLArray> cells = arr.cells();
        //pour chacune, 
        for(int i = 0; i < cells.size(); i++){                    
            MLArray a = cells.get(i);  
            arrayList.add(a);
        }
       
        Object[] wrapper = new Object[3];
        wrapper[0] = arrayList;
        return wrapper;
    }
    
}
