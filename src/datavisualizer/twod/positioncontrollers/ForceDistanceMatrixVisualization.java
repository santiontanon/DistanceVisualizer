/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datavisualizer.twod.positioncontrollers;

import datavisualizer.DistanceMatrix;
import datavisualizer.SparseDistanceMatrix;
import datavisualizer.twod.Visualization;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author santi
 */
public class ForceDistanceMatrixVisualization implements VisualizationPositionController {
    Visualization visualization = null;

    DistanceMatrix matrix = null;
    DistanceMatrix matrixsq = null;

    double forces_x[] = null;
    double forces_y[] = null;
    
    Random rand = new Random();
        
    public ForceDistanceMatrixVisualization(DistanceMatrix m, Visualization v) {        
        visualization = v;
        matrix = m;
        matrixsq = m.square();
       
        if (visualization.loc_x==null) {
            visualization.loc_x = new double[matrix.names.length];
            visualization.loc_y = new double[matrix.names.length];
            for(int i = 0;i<matrix.names.length;i++) {
                visualization.loc_x[i] = rand.nextDouble();
                visualization.loc_y[i] = rand.nextDouble();
//                System.out.println(locations[i].getX() + ", " + locations[i].getY());
            }
        }        
    }
    
    
    public void setMatrix(DistanceMatrix m) {
        matrix = m;
        matrixsq = m.square();
    }
    
    
    public void updatePositions()
    {
        applyForces();
    }
    

    public double applyForces()
    {
        int i,j,l;
        double f;
        double dx,dy,d;
        double error = 0;
        int l2;
        SparseDistanceMatrix smsq = null;
        if (matrix instanceof SparseDistanceMatrix) smsq = (SparseDistanceMatrix)matrixsq;
        l = visualization.names.length;
        if (forces_x==null) {
            forces_x = new double[l];
            forces_y = new double[l];
        }
        for(i = 0;i<l;i++) {
            if (visualization.instancesToIgnore[i]) continue;
            forces_x[i] = 0.0;
            forces_y[i] = 0.0;
            l2 = 0;
            if (smsq!=null) {
                for(Entry<Integer,Double> e:smsq.getRow(i).entrySet()) {
                    j = e.getKey();
                    if (i!=j) {
                        if (visualization.instancesToIgnore[j]) continue;
                        dx = visualization.loc_x[j]-visualization.loc_x[i];
                        dy = visualization.loc_y[j]-visualization.loc_y[i];
                        d = dx*dx+dy*dy;
                        error+=d;
                        if (d>0.000001) {
                            f = (d - e.getValue())/d; 
                            forces_x[i] += dx*f;
                            forces_y[i] += dy*f;
                            l2++;
                        }
                    }                
                }
            } else {
                for(j=0;j<l;j++) {
                    if (i!=j) {
                        if (visualization.instancesToIgnore[j] || Double.isNaN(matrixsq.get(i,j))) continue;
                        dx = visualization.loc_x[j]-visualization.loc_x[i];
                        dy = visualization.loc_y[j]-visualization.loc_y[i];
                        d = dx*dx+dy*dy;
                        error+=d;
                        if (d>0.000001) {
                            f = (d - matrixsq.get(i,j))/d; 
//                            f = (d - matrixsq.get(i,j));  // Why was I dividing by "d"?
                            forces_x[i] += dx*f;
                            forces_y[i] += dy*f;
                            l2++;
                        }
                    }
                }
            }
            if (l2>0) {
                forces_x[i]/=l2;
                forces_y[i]/=l2;
            }

        }

        double speed = 0.5;
        for(i = 0;i<l;i++) {
            visualization.loc_x[i] += forces_x[i]*speed;
            visualization.loc_y[i] += forces_y[i]*speed;
//            visualization.locations[i].setLocation(visualization.locations[i].getX()+forces_x[i]*speed, visualization.locations[i].getY()+forces_y[i]*speed);
        }
        
        return error;
    }     
}
