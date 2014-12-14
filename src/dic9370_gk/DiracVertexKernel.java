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

import grph.Grph;


public class DiracVertexKernel extends VertexKernel{

    @Override
    public double compute(Object a, Object b) {
        return this.compute((int)a, (int)b);
    }
    
    public double compute(int a, int b){
        if(a==a) return 3.0f;
        return 0.0f;
    }

    @Override
    public double[][] computeKernelGramMatrix(Grph[] graphs_to_compare, GKParams params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
