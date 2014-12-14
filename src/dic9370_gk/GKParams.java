/**
 *
 * @author Enridestroy
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dic9370_gk;

import java.util.HashMap;


public class GKParams {
    private HashMap<String, Object> parameters = new HashMap<>();
    
    public void addParam(String name, Object value){
        this.parameters.put(name, value);
    }
    
    public Object getParam(String name){
        return this.parameters.get(name);
    }
    
    public boolean checkParam(String name){
        return this.parameters.containsKey(name);
    }
    
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        for(String o : this.parameters.keySet()){
            Object oo = this.parameters.get(o);
            b.append(o).append("_").append(oo);
        }
        return b.toString();
    }
}
