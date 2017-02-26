/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datavisualizer.threed.positioncontrollers;

import datavisualizer.InstancePositions;
import datavisualizer.threed.Point3d;
import datavisualizer.threed.Visualization3D;

/**
 *
 * @author santi
 */
public class InstancePositions3DController implements Visualization3DPositionController {
    InstancePositions positions = null;
    Visualization3D visualization = null;

    public InstancePositions3DController(InstancePositions p, Visualization3D v) {
        visualization = v;
       
        setPositions(p);
    }

    public void setPositions(InstancePositions p) {
        positions = p;
        updatePositions();
    }
    
    public void updatePositions()
    {
        if (visualization.locations==null) {
            visualization.locations = new Point3d[positions.names.length];
        } 
        for(int i = 0;i<positions.names.length;i++) {
            visualization.locations[i].x = positions.positions[i][0];
            visualization.locations[i].y = positions.positions[i][1];
            visualization.locations[i].z = positions.positions[i][2];
        }
    }
    
}
