package datavisualizer.twod;

import datavisualizer.twod.Visualization;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class VisualizationListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    Visualization ap;

    boolean dragging = false;
    int button_pressed;
    int last_drag_x = 0;
    int last_drag_y = 0;
    int total_offset_x=0;
    int total_offset_y =0;
    int MinX=0;
    int MaxX=0;
    int MinY=0;
    int MaxY=0;    
    
    public VisualizationListener(Visualization p) {
        ap = p;
    }

    public void mouseMoved(MouseEvent e) {
        Rectangle r = ap.getBounds();
        int l = ap.names.length;
        int x = e.getX();
        int y = e.getY();

        ap.highlightedCases.clear();
        for (int i = 0; i < l; i++) {
            if (ap.instancesToIgnore[i]) continue;
            if (x >= ap.caseX(i,r.width,r.height) && x < ap.caseX(i,r.width,r.height) + ap.case_size
                && y >= ap.caseY(i,r.width,r.height) && y < ap.caseY(i,r.width,r.height) + ap.case_size) {
                // button highlighted
                ap.highlightedCases.add(i);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    	button_pressed = e.getButton();
    }

    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (button_pressed == MouseEvent.BUTTON1){
        	int x = e.getX();
        	int y = e.getY();
        	if (dragging) {
        		double dx = (x - last_drag_x)/(double)ap.getWidth();
        		double dy = (y - last_drag_y)/(double)ap.getHeight();
        		total_offset_x += dx/ap.scale;
        		total_offset_y += dy/ap.scale;
        		ap.center_x = (ap.center_x - dx/ap.scale);
        		ap.center_y = (ap.center_y - dy/ap.scale);
        	}
        	last_drag_x = x;
        	last_drag_y = y;
        	dragging = true;
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {      
        double factor = Math.pow(1.05, e.getPreciseWheelRotation());
//        double centerx = ap.center_x + (ap.getWidth()/2)/ap.scale;
//        double centery = ap.center_y + (ap.getHeight()/2)/ap.scale;
        double newZoom = ap.scale*factor;
//        double centerx2 = ap.center_x + (ap.getWidth()/2)/newZoom;
//        double centery2 = ap.center_y + (ap.getHeight()/2)/newZoom;
        ap.scale = newZoom;
//        ap.center_x = (ap.center_x + (centerx - centerx2));
//        ap.center_y = (ap.center_y + (centery - centery2));
    }
 
}