/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datavisualizer.twod.positioncontrollers;

import datavisualizer.InstancePositions;
import datavisualizer.twod.Visualization;
import java.awt.geom.Point2D;

/**
 *
 * @author santi
 */
public class InstancePositionsVisualization implements VisualizationPositionController {
    InstancePositions positions = null;
    Visualization visualization = null;

    public InstancePositionsVisualization(InstancePositions p, Visualization v) {
        visualization = v;
       
        setPositions(p);
    }

    public void setPositions(InstancePositions p) {
        positions = p;
        updatePositions();
    }
    
    public void updatePositions()
    {
        if (visualization.loc_x==null) {
            visualization.loc_x = new double[positions.names.length];
            visualization.loc_y = new double[positions.names.length];
        } 
        for(int i = 0;i<positions.names.length;i++) {
            visualization.loc_x[i] = positions.positions[i][0];
            visualization.loc_y[i] = positions.positions[i][1];
        }
    }
    
}
