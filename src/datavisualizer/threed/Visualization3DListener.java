package datavisualizer.threed;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Visualization3DListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    Visualization3D ap;

    boolean dragging = false;
    int button_pressed;
    int last_drag_x = 0;
    int last_drag_y = 0;
    int total_offset_x = 0;
    int total_offset_y = 0;

    int last_right_drag_x = 0;  // for rotations
    int last_right_drag_y = 0;  // for rotations

    int MinX = 0;
    int MaxX = 0;
    int MinY = 0;
    int MaxY = 0;

    public Visualization3DListener(Visualization3D p) {
        ap = p;
    }

    public void mouseMoved(MouseEvent e) {
        Rectangle r = ap.getBounds();
        int l = ap.names.length;
        int x = e.getX();
        int y = e.getY();

        ap.highlightedCases.clear();
        for (int i = 0; i < l; i++) {
            if (ap.instancesToIgnore[i]) {
                continue;
            }
            if (x >= ap.caseX(i, r.width, r.height, ap.m_base_case_size) && x < ap.caseX(i, r.width, r.height, ap.m_base_case_size) + ap.m_base_case_size &&
                y >= ap.caseY(i, r.width, r.height, ap.m_base_case_size) && y < ap.caseY(i, r.width, r.height, ap.m_base_case_size) + ap.m_base_case_size) {
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
        if (button_pressed == MouseEvent.BUTTON1) {
            int x = e.getX();
            int y = e.getY();
            if (dragging) {
                double dx = (x - last_drag_x) / (double) ap.getWidth();
                double dy = (y - last_drag_y) / (double) ap.getHeight();
                total_offset_x += dx / ap.scale;
                total_offset_y += dy / ap.scale;
                
                double inc_X = -(dx / ap.scale);
                double inc_Y = -(dy / ap.scale);
                
                ap.center_x += inc_X*Math.cos(-ap.angle) - 0*Math.sin(-ap.angle);
                ap.center_z += inc_X*Math.sin(-ap.angle) + 0*Math.cos(-ap.angle);
                ap.center_y += inc_Y;
                ap.modifiedByMouseListener = true;
            }
            last_drag_x = x;
            last_drag_y = y;
            dragging = true;
        }
        if (button_pressed == MouseEvent.BUTTON3) {
            int x = e.getX();
            int y = e.getY();
            if (dragging) {
                double dx = (x - last_right_drag_x) / (double) ap.getWidth();
                double dy = (y - last_right_drag_y) / (double) ap.getHeight();
                total_offset_x += dx / ap.scale;
                total_offset_y += dy / ap.scale;
                ap.angle += dx;// / ap.scale;
//                ap.center_x = (ap.center_x - dx / ap.scale);
//                ap.center_y = (ap.center_y - dy / ap.scale);
                ap.modifiedByMouseListener = true;
            }
            last_right_drag_x = x;
            last_right_drag_y = y;
            dragging = true;
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        // NOTE: the e.isShiftDown() trick apparently only works on Mac to distinguish V/H scrolling
        if (e.isShiftDown()) {
            // horizontal scrolling:
            ap.angle += e.getPreciseWheelRotation()/20;
            ap.modifiedByMouseListener = true;
        } else {
            // vertical scrolling:
            double factor = Math.pow(1.05, e.getPreciseWheelRotation());
            double newZoom = ap.scale * factor;
            ap.scale = newZoom;
            ap.modifiedByMouseListener = true;            
        }
    }

}
