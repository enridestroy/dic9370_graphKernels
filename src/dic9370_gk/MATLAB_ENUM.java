/**
 *
 * @author Enridestroy
 * Ce morceau de code provient d'un autre projet personnel qui permet de convertir plusieurs formats de données vers RDF dont Matlab
 */
package dic9370_gk;

import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;


public enum MATLAB_ENUM {
    STRUCTURE(MLStructure.class.hashCode()),
    CELL(MLCell.class.hashCode()),
    DOUBLE(MLDouble.class.hashCode()),
    CHAR(MLChar.class.hashCode());
    
    private int value;
    
    public int v(){
        return this.value;
    }
    
    private MATLAB_ENUM(int value) {
        this.value = value;
    }
    
}
