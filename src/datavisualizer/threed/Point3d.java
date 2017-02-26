/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer.threed;

import datavisualizer.threed.*;

/**
 *
 * @author santi
 */
public class Point3d {
    public double x,y,z;
    
    public Point3d(double a_x, double a_y, double a_z) {
        x = a_x;
        y = a_y;
        z = a_z;
    }
    
    public void setLocation(double a_x, double a_y, double a_z) {
        x = a_x;
        y = a_y;
        z = a_z;
    }
    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
