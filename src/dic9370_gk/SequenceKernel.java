/**
 *
 * @author Enridestroy
 */
package dic9370_gk;

import grph.Grph;
import java.util.List;

public class SequenceKernel extends KernelFunction{
    /**
     * 
     * @param a
     * @param b
     * @return 
     */
    public double compute(List a, List b){
        if(a.size()!=b.size()) return 1.0f;
        
        int l=a.size();
        if(l<1){
            return 1.0f;
        }
        /**
         * choix des noyaux pour les vertex et edges
         */
        VertexKernel kv = new DiracVertexKernel();
        EdgeKernel ke = new DiracEdgeKernel();
        
        double k_v1 = kv.compute(a.get(0), b.get(0));
        
        if(l<2) return k_v1;
        
        double[] p_kv_ke = new double[l-1];
        //System.out.println("l="+l);
        for(int i=1;i<((l+1)/2);i++){
            int iv = 2*i;
            //System.out.println("iv="+iv);
            int ie = iv-1;
            //System.out.println("ie="+ie);
            p_kv_ke[i-1] = kv.compute(a.get(iv), b.get(iv)) * ke.compute(a.get(ie), b.get(ie));
            //System.out.println("----");
        }
        
        double _t = k_v1;
        for(int i=0;i<p_kv_ke.length;i++){
            //System.out.println("kz("+i+")="+p_kv_ke[i]);
            _t*=p_kv_ke[i];
        }
        return _t;
    }
    
    
    @Override
    public double compute(Object a, Object b) {
        return (float)this.compute((List)a, (List)b);
    }


    @Override
    public double[][] computeKernelGramMatrix(Grph[] graphs_to_compare, GKParams params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
