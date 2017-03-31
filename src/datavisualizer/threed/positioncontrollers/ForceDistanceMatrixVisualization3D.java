/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datavisualizer.threed.positioncontrollers;

import datavisualizer.DistanceMatrix;
import datavisualizer.SparseDistanceMatrix;
import datavisualizer.threed.Visualization3D;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author santi
 */
public class ForceDistanceMatrixVisualization3D implements Visualization3DPositionController {
    Visualization3D visualization = null;

    DistanceMatrix matrix = null;
    DistanceMatrix matrixsq = null;

    double forces_x[] = null;
    double forces_y[] = null;
    double forces_z[] = null;
    
    Random rand = new Random();
        
    public ForceDistanceMatrixVisualization3D(DistanceMatrix m, Visualization3D v) {        
        visualization = v;
        matrix = m;
        matrixsq = m.square();       
    }
    
    
    public DistanceMatrix getMatrix()
    {
        return matrix;
    }
    
    
    public void setMatrix(DistanceMatrix m) {
        matrix = m;
        matrixsq = m.square();
    }
    
    
    public void updatePositions()
    {
        applyForces();
    }
    

    public void applyForces()
    {
        int i,j,l;
        double f;
        double dx,dy,dz,d;
        int l2;
        SparseDistanceMatrix smsq = null;
        if (matrix instanceof SparseDistanceMatrix) smsq = (SparseDistanceMatrix)matrixsq;
        l = visualization.names.length;
        if (forces_x==null) {
            forces_x = new double[l];
            forces_y = new double[l];
            forces_z = new double[l];
        }
        for(i = 0;i<l;i++) {
            if (visualization.instancesToIgnore[i]) continue;
            forces_x[i] = 0.0;
            forces_y[i] = 0.0;
            forces_z[i] = 0.0;
            l2 = 0;
            if (smsq!=null) {
                for(Entry<Integer,Double> e:smsq.getRow(i).entrySet()) {
                    j = e.getKey();
                    if (i!=j) {
                        if (visualization.instancesToIgnore[j]) continue;
                        dx = visualization.locations[j].x-visualization.locations[i].x;
                        dy = visualization.locations[j].y-visualization.locations[i].y;
                        dz = visualization.locations[j].z-visualization.locations[i].z;
                        d = dx*dx+dy*dy+dz*dz;
                        if (d>0.000001) {
                            f = (d - e.getValue())/d; 
                            forces_x[i] += dx*f;
                            forces_y[i] += dy*f;
                            forces_z[i] += dz*f;
                            l2++;
                        }
                    }                
                }
            } else {
                for(j=0;j<l;j++) {
                    if (i!=j) {
                        if (visualization.instancesToIgnore[j] || Double.isNaN(matrixsq.get(i,j))) continue;
                        dx = visualization.locations[j].x-visualization.locations[i].x;
                        dy = visualization.locations[j].y-visualization.locations[i].y;
                        dz = visualization.locations[j].z-visualization.locations[i].z;
                        d = dx*dx+dy*dy+dz*dz;
                        if (d>0.000001) {
                            f = (d - matrixsq.get(i,j))/d; 
                            forces_x[i] += dx*f;
                            forces_y[i] += dy*f;
                            forces_z[i] += dz*f;
                            l2++;
                        }
                    }
                }
            }
            if (l2>0) {
                forces_x[i]/=l2;
                forces_y[i]/=l2;
                forces_z[i]/=l2;
            }

        }

        double speed = 0.5;
        for(i = 0;i<l;i++) {
            visualization.locations[i].x += forces_x[i]*speed;
            visualization.locations[i].y += forces_y[i]*speed;
            visualization.locations[i].z += forces_z[i]*speed;
        }

    }     
}
