/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 
package datavisualizer.twod;
 
import datavisualizer.DistanceMatrix;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
 
/**
 *
 * @author santi
 */
public class Visualization extends JPanel {
    public static final int COLOR_STEPS = 32;

    public boolean OUTLINES = true;
    public boolean SHOW_DATA = true;
    public boolean SHOW_KEY = false;
    public List<String> toAppearInKey = new LinkedList<>();    
    
    public String []names = null;

    boolean m_continuous1 = false;
    boolean m_continuous2 = false;
    boolean m_continuous3 = false;
    String []labels1 = null;
    String []labels2 = null;
    String []labels3 = null;
    HashMap<String,Color> labelColors = new LinkedHashMap<>();

    double []continuousLabels1 = null;
    double []continuousLabels2 = null;
    double []continuousLabels3 = null;
    double minContinuousLabel1 = 0, maxContinuousLabel1= 1;
    double minContinuousLabel2 = 0, maxContinuousLabel2= 1;
    double minContinuousLabel3 = 0, maxContinuousLabel3= 1;
    Color continuousColors[] = null;    // this is used when there is a single label set selected
    Color continuousColors3[][][] = null;   // this is used when more than one label set is selected
    
    public List<Integer> highlightedCases = new ArrayList<>();
 
    public double loc_x[] = null;
    public double loc_y[] = null;
    HashMap<String,Integer> labelCounts = new LinkedHashMap<>();
    List<Color> availableColors = null;
    Random rand = new Random();
    public int case_size = 8;
    double cases_min_x = 0,cases_min_y = 0,cases_max_x = 0,cases_max_y = 0;
     
    // Focus: these are relative to the center of all the points and their positions
    double center_x = 0;
    double center_y = 0;
    double scale = 1;
     
    public boolean []instancesToIgnore = null;

    
    public Visualization(List<String> cl, List<String> ll, List<String> forKey, boolean continuous) {
        String []ll_array = new String [ll.size()];
        for(int i = 0;i<ll.size();i++) ll_array[i] = ll.get(i);
        names = ll_array;
        instancesToIgnore = new boolean[names.length];
        
        setClasses(cl, null, null, forKey, continuous, false, false);
                
        VisualizationListener ml = new VisualizationListener(this);
        addMouseListener(ml);
        addMouseMotionListener(ml);
        addMouseWheelListener(ml);
 
        setKeyBindings();
         
        assignColors();
    }
     
 
    public void clearIgnored() {
        instancesToIgnore = new boolean[names.length];
    }
     
    public void ignore(int idx) {
        instancesToIgnore[idx] = true;
    }
     
     
    public String[] getNames() {
        return names;
    }
     
     
    // actions:
    public static class zoomInAction extends AbstractAction {
        Visualization ap = null;
        public zoomInAction(Visualization a_ap) {
            ap = a_ap;
        }
        public void actionPerformed(ActionEvent e) {
            ap.scale*=1.1;
            if (ap.scale>=16) ap.scale = 16;
        }        
    }
    public static class zoomOutAction extends AbstractAction {
        Visualization ap = null;
        public zoomOutAction(Visualization a_ap) {
            ap = a_ap;
        }
        public void actionPerformed(ActionEvent e) {
            ap.scale/=1.1;
            if (ap.scale<=0.5) ap.scale = 0.5;
        }        
    }
    public static class moveAction extends AbstractAction {
        Visualization ap = null;
        double dx=0,dy=0;
        public moveAction(Visualization a_ap, double a_dx, double a_dy) {
            ap = a_ap;
            dx = a_dx;
            dy = a_dy;
        }
        public void actionPerformed(ActionEvent e) {
            ap.center_x+=(dx/ap.scale);
            ap.center_y+=(dy/ap.scale);
            if (ap.center_x<-0.5) ap.center_x = -0.5;
            if (ap.center_x>0.5) ap.center_x = 0.5;
            if (ap.center_y<-0.5) ap.center_y = -0.5;
            if (ap.center_y>0.5) ap.center_y = 0.5;
        }        
    }
    
    
    /* 
    public static class saveToSVGAction extends AbstractAction {
        Visualization ap = null;
        public saveToSVGAction(Visualization a_ap) {
            ap = a_ap;
        }
        public void actionPerformed(ActionEvent e) {
            Rectangle r = ap.getBounds();
            try {
                ap.saveToSVG("Visualization.svg", r.width,r.height);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }        
    }
    */
 
    public static class randomPushAction extends AbstractAction {
        Visualization ap = null;
        public randomPushAction(Visualization a_ap) {
            ap = a_ap;
        }
        public void actionPerformed(ActionEvent e) {
            ap.randomPush(0.33);
        }        
    }

    public void randomPush(double amount) {
        for(int i = 0;i<loc_x.length;i++) {
            loc_x[i] += amount*rand.nextDouble()*(cases_max_x-cases_min_x); 
            loc_y[i] += amount*rand.nextDouble()*(cases_max_y-cases_min_y);
        }        
    }
     
    public void setKeyBindings() {        
        /*
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0),"saveToSVG");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0),"saveToSVG");
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0),"saveToSVG");
        getActionMap().put("saveToSVG", new saveToSVGAction(this));
        */
        
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_R,0),"reset");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R,0),"reset");
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_R,0),"reset");
        getActionMap().put("reset", new randomPushAction(this));
    }
     
    public void setClasses(List<String> cl1, 
                           List<String> cl2, 
                           List<String> cl3, 
                           List<String> forKey, 
                           boolean continuous1,
                           boolean continuous2,
                           boolean continuous3) {
        m_continuous1 = continuous1;
        m_continuous2 = continuous2;
        m_continuous3 = continuous3;
        if (cl1==null) {
            labels1 = null;
            continuousLabels1 = null;
        } else {
            String []cl_array = new String [cl1.size()];
            for(int i = 0;i<cl1.size();i++) cl_array[i] = cl1.get(i);
            labels1 = cl_array;
            if (m_continuous1) {
                continuousLabels1 = new double[names.length];
                for(int i = 0;i<names.length;i++) {
                    continuousLabels1[i] = Double.parseDouble(labels1[i]);
                }            
            }
        }
        if (cl2==null) {
            labels2 = null;
            continuousLabels2 = null;
        } else {
            String []cl_array = new String [cl2.size()];
            for(int i = 0;i<cl2.size();i++) cl_array[i] = cl2.get(i);
            labels2 = cl_array;
            if (m_continuous2) {
                continuousLabels2 = new double[names.length];
                for(int i = 0;i<names.length;i++) {
                    continuousLabels2[i] = Double.parseDouble(labels2[i]);
                }            
            }
        }
        if (cl3==null) {
            labels3 = null;
            continuousLabels3 = null;
        } else {
            String []cl_array = new String [cl3.size()];
            for(int i = 0;i<cl3.size();i++) cl_array[i] = cl3.get(i);
            labels3 = cl_array;
            if (m_continuous3) {
                continuousLabels3 = new double[names.length];
                for(int i = 0;i<names.length;i++) {
                    continuousLabels3[i] = Double.parseDouble(labels3[i]);
                }            
            }
        }
        
        toAppearInKey.clear();
        toAppearInKey.addAll(forKey);

        assignColors();
    }    
    
    
    public void setKey(List<String> forKey) {
        toAppearInKey.clear();
        toAppearInKey.addAll(forKey);        
    }
         
    public void paint(Graphics g) {
        super.paint(g);
        List<Integer> toHighlight = new LinkedList<Integer>();
        
        
        // Determine agent and training set location:
        Rectangle r = getBounds();
//        g.setColor(Color.white);
//        g.fillRect(0, 0, r.width,r.height);
        drawCaseBase(g, 0,0,r.width,r.height, toHighlight, Color.black);        
        if (SHOW_KEY) drawKey(g);
    }
    
    public void drawKey(Graphics g) {
        int x = 10;
        int y = 10;
        if (m_continuous1) {
            double range1 = maxContinuousLabel1 - minContinuousLabel1;
            for(int i = 0;i<=COLOR_STEPS;i+=COLOR_STEPS/8) {
                double f = i/(double)COLOR_STEPS;
                if ((m_continuous2 && labels2!=null) ||
                    (m_continuous2 && labels2!=null)) {
                    g.setColor(continuousColors3[i][0][0]);
                } else {
                    g.setColor(continuousColors[i]);                        
                }
                g.fillRect(x, y, 30, 15);
                g.setColor(Color.black);
                g.drawRect(x, y, 30, 15);
                g.drawString("" + (minContinuousLabel1 + f*range1), x + 40, y+12);
                y+=20;
            }
            if (m_continuous2 && labels2!=null) {
                x+=200;
                y = 10;
                double range2 = maxContinuousLabel2 - minContinuousLabel2;
                for(int i = 0;i<=COLOR_STEPS;i+=COLOR_STEPS/8) {
                    double f = i/(double)COLOR_STEPS;
                    g.setColor(continuousColors3[0][i][0]);
                    g.fillRect(x, y, 30, 15);
                    g.setColor(Color.black);
                    g.drawRect(x, y, 30, 15);
                    g.drawString("" + (minContinuousLabel2 + f*range2), x + 40, y+12);
                    y+=20;
                }
            }
            if (m_continuous3 && labels3!=null) {
                x+=200;
                y = 10;
                double range3 = maxContinuousLabel3 - minContinuousLabel3;
                for(int i = 0;i<=COLOR_STEPS;i+=COLOR_STEPS/8) {
                    double f = i/(double)COLOR_STEPS;
                    g.setColor(continuousColors3[0][0][i]);
                    g.fillRect(x, y, 30, 15);
                    g.setColor(Color.black);
                    g.drawRect(x, y, 30, 15);
                    g.drawString("" + (minContinuousLabel3 + f*range3), x + 40, y+12);
                    y+=20;
                }
            }
        } else {
            for(String cName:toAppearInKey) {
                Integer count = labelCounts.get(cName);
                if (count!=null) {
                    Color c = labelColors.get(cName);
                    g.setColor(c);
                    g.fillRect(x, y, 30, 15);
                    g.setColor(Color.black);
                    g.drawRect(x, y, 30, 15);
                    g.drawString(cName + "(" + count + ")", x + 40, y+12);
                    y+=20;
                }
            }
        }
    }
 
 
    public void assignColors() {
        labelColors.clear();
        labelCounts.clear();
        if (labels1==null) return;
        
        if (m_continuous1) { 
            // find min and max
            minContinuousLabel1 = Double.MAX_VALUE;
            maxContinuousLabel1 = Double.MIN_VALUE;
            minContinuousLabel2 = Double.MAX_VALUE;
            maxContinuousLabel2 = Double.MIN_VALUE;
            minContinuousLabel3 = Double.MAX_VALUE;
            maxContinuousLabel3 = Double.MIN_VALUE;
            for(double cl:continuousLabels1) {
                if (cl<minContinuousLabel1) minContinuousLabel1 = cl;
                if (cl>maxContinuousLabel1) maxContinuousLabel1 = cl;
            }
            if (m_continuous2) {
                for(double cl:continuousLabels2) {
                    if (cl<minContinuousLabel2) minContinuousLabel2 = cl;
                    if (cl>maxContinuousLabel2) maxContinuousLabel2 = cl;
                }
            }
            if (m_continuous3) {
                for(double cl:continuousLabels3) {
                    if (cl<minContinuousLabel3) minContinuousLabel3 = cl;
                    if (cl>maxContinuousLabel3) maxContinuousLabel3 = cl;
                }
            }

            if (continuousColors3==null) {
                continuousColors = new Color[COLOR_STEPS+1];
                continuousColors3 = new Color[COLOR_STEPS+1][COLOR_STEPS+1][COLOR_STEPS+1];
                for(int i = 0;i<COLOR_STEPS+1;i++) {
                    float f1 = i/(float)COLOR_STEPS;
                    float f1a = Math.max(1-f1*2,0);
                    float f1b = Math.max(f1*2-1,0);
                    
                    continuousColors[i] = new Color(f1a, f1b, 0);
                    for(int j = 0;j<COLOR_STEPS+1;j++) {
                        float f2 = j/(float)COLOR_STEPS;
                        for(int k = 0;k<COLOR_STEPS+1;k++) {
                            float f3 = k/(float)COLOR_STEPS;
                            continuousColors3[i][j][k] = new Color(f1, f2, f3);
                        }
                    }
                }
            }
        } else {
            labelColors.clear();
            labelCounts.clear();
            availableColors = new LinkedList<Color>();
            float intervals[] = {0,1,0.5f,0.25f,0.75f};
            for(int max = 1;max<12;max++) {
                for(int i = 0;i<5 && i<=max;i++) {
                    for(int j = 0;j<5 && (i+j)<=max;j++) {
                        int k = max - (i+j);
    //                    System.out.println(i + " " + j + " " + k);
                        if (k<5) {
                            availableColors.add(new Color(intervals[i], intervals[j], intervals[k]));
    //                        System.out.println("Color: " + intervals[i] + " " + intervals[j] + " " + intervals[k]);
                        }
                    }
                }
            }
            System.out.println(availableColors.size() + " colors generated");

            int i = 0;
            for(String solution:labels1) {
                Color color = labelColors.get(solution);
                if (color==null) {
                    labelCounts.put(solution,1);
                    if (i<availableColors.size()) {
                        color = availableColors.get(i);
                        labelColors.put(solution, color);
                    } else {
                        color = new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
                        labelColors.put(solution, color);
                    }
                    i++;
                } else {
                    labelCounts.put(solution, labelCounts.get(solution)+1);
                }
            }
        }
    }
 
 
    public void drawCaseBase(Graphics g,int sx,int sy,int dx,int dy, List<Integer> toHighlight, Color outlineColor) {
        boolean first = true;
 
        if (names.length>0) {
            double s = Math.sqrt(((dx*dy)/names.length)/50);
            if (s<8) s = 8;
            if (s>16) s = 16;
            case_size = (int)s;
        }
 
        cases_min_x = cases_min_y = cases_max_x = cases_max_y = 0;
        for(int i = 0;i<names.length;i++) {
            if (instancesToIgnore[i]) continue;
            if (first) {
                cases_min_x = cases_max_x = loc_x[i];
                cases_min_y = cases_max_y = loc_y[i];
                first = false;
            } else {
                if (loc_x[i]<cases_min_x) cases_min_x = loc_x[i];
                if (loc_x[i]>cases_max_x) cases_max_x = loc_x[i];
                if (loc_y[i]<cases_min_y) cases_min_y = loc_y[i];
                if (loc_y[i]>cases_max_y) cases_max_y = loc_y[i];
            }
        }
 
//        System.out.println("[" + cases_min_x + "," + cases_max_x + "] - [" + cases_min_y + "," + cases_max_y + "]");
 
        // draw the highlighted cases:
        for(int i:toHighlight) {
            int x = caseX(i,dx,dy);
            int y = caseY(i,dx,dy);
            g.setColor(Color.black);
            g.drawArc(x-(case_size+16)/2, y-(case_size+16)/2, case_size+16, case_size+16, 0, 360);
            g.setColor(Color.lightGray);
            g.fillArc(x-(case_size+16)/2, y-(case_size+16)/2, case_size+16, case_size+16, 0, 360);
        }
         
        // draw the cases:
        String solution = null;
        if (SHOW_DATA) {
            for(int i = 0;i<names.length;i++) {
                if (instancesToIgnore[i]) continue;
                int x = caseX(i,dx,dy);
                int y = caseY(i,dx,dy);
                if (labels1==null) {
                    g.setColor(Color.black);
                } else {
                    solution = labels1[i];
                    if (m_continuous1) {
                        int idx = (int)(COLOR_STEPS * (continuousLabels1[i] - minContinuousLabel1)/(maxContinuousLabel1 - minContinuousLabel1));
                        if (m_continuous2 && labels2!=null) {
                            int idx2 = (int)(COLOR_STEPS * (continuousLabels2[i] - minContinuousLabel2)/(maxContinuousLabel2 - minContinuousLabel2));
                            if (m_continuous3 && labels3!=null) {
                                int idx3 = (int)(COLOR_STEPS * (continuousLabels3[i] - minContinuousLabel3)/(maxContinuousLabel3 - minContinuousLabel3));
                                g.setColor(continuousColors3[idx][idx2][idx3]);
                            } else {
                                g.setColor(continuousColors3[idx][idx2][0]);
                            }
                        } else {
                            if (m_continuous3 && labels3!=null) {
                                int idx3 = (int)(COLOR_STEPS * (continuousLabels3[i] - minContinuousLabel3)/(maxContinuousLabel3 - minContinuousLabel3));
                                g.setColor(continuousColors3[idx][0][idx3]);
                            } else {
                                g.setColor(continuousColors[idx]);
                            }
                        }
                    } else {
                        g.setColor(labelColors.get(solution));
                    }
                }
                g.fillArc(x-(case_size)/2, y-(case_size)/2, case_size, case_size, 0, 360);
                if (OUTLINES) {
                    g.setColor(outlineColor);
                    g.drawArc(x-(case_size)/2, y-(case_size)/2, case_size, case_size, 0, 360);
                }
            }
        }
         
        if (!highlightedCases.isEmpty()) {
            int hlcx = -1, hlcy = -1;
            for(int highlightedCase: highlightedCases) {
                hlcx = caseX(highlightedCase,dx,dy);
                hlcy = caseY(highlightedCase,dx,dy);
                Color color = null;
                if (labels1==null) {
                    solution = null;
                } else {
                    solution = labels1[highlightedCase];
                    color = labelColors.get(solution);
                }
                if (color==null) color = Color.black;
                g.setColor(color);
                g.drawArc(hlcx-(case_size+8)/2, hlcy-(case_size+8)/2, case_size+8, case_size+8, 0, 360);
            }
            
            // draw text box:
            {
                List<String> lines = new ArrayList<>();
                List<Rectangle2D> bounds = new ArrayList<>();
                String s1 = "";
                int idx = 0;
                for(int highlightedCase: highlightedCases) {
                    s1 += names[highlightedCase] + ", ";
                    idx++;
                    if (s1.length()>100) {
                        s1 += "... and " + (highlightedCases.size()-idx) + " others";
                        break;
                    }
                }
                while(s1.length()>100) {
                    lines.add(s1.substring(0,100));
                    s1 = s1.substring(100);
                }
                lines.add(s1);
                HashMap<String, Integer> tmp = new HashMap<>();
                for(int highlightedCase: highlightedCases) {
                    if (labels1==null) {
                    } else {
                        String label = labels1[highlightedCase];
                        if (tmp.get(label)==null) {
                            tmp.put(label, 1);
                        } else {
                            tmp.put(label, tmp.get(label)+1);
                        }
                    }                    
                }
                for(String label:tmp.keySet()) {
                    lines.add(label + ":" + tmp.get(label));
                }
                
                int padding = 4;
                int tdx = 0, tdy = 0;
                for(String line:lines) {
                    Rectangle2D r = g.getFontMetrics().getStringBounds(line, g);
                    bounds.add(r);
                    if (r.getWidth()>tdx) tdx = (int)r.getWidth();
                    tdy += (int)r.getHeight() + padding;
                }
                tdx+=padding*2;
                tdy+=padding*3;
                
 
                g.setColor(Color.lightGray);
                g.fillRect(hlcx+case_size/2-tdx/2, hlcy+case_size+2, tdx, tdy);
                g.setColor(Color.black);
                g.drawRect(hlcx+case_size/2-tdx/2, hlcy+case_size+2, tdx, tdy);
                int ystart = (int)(hlcy+case_size+2)+padding;
                for(int i = 0;i<lines.size();i++) {
                    String line = lines.get(i);
                    Rectangle2D r = bounds.get(i);
                    g.drawString(line, hlcx+case_size/2-tdx/2+padding, (int)(ystart + r.getHeight()));
                    ystart+=r.getHeight() + padding;
                }
//                g.drawString(s2, x+case_size/2-tdx/2+padding, (int)(y+case_size+2+r1.getHeight()+r2.getHeight())+padding*2);
            }
        }
         
 
    }
 
    public int caseX(int i, int dx, int dy) {
        double center = (cases_min_x + cases_max_x)/2;
        double x = (loc_x[i]-center)/Math.max(cases_max_x-cases_min_x,cases_max_y-cases_min_y);
        x -= center_x;
        x*=scale;
        double divisor = Math.min(dx,dy)-case_size*3;
        return (int)(dx/2 + ((dx-case_size*3)-divisor)/2 + case_size + x*divisor);
    }
 
    public int caseY(int i, int dx, int dy) {
        double center = (cases_min_y + cases_max_y)/2;
        double y = (loc_y[i]-center)/Math.max(cases_max_x-cases_min_x,cases_max_y-cases_min_y);
        y -= center_y;
        y*=scale;
        double divisor = Math.min(dx,dy)-case_size*3;
        return (int)(dy/2 + ((dy-case_size*3)-divisor)/2 + case_size + y*divisor);
    }
     
 
    public void saveToSVG(String fileName, int dx,int dy) throws Exception {
        FileWriter fw = new FileWriter(fileName);
        
        if (names.length>0) {
            double s = Math.sqrt(((dx*dy)/names.length)/50);
            if (s<8) s = 8;
            if (s>16) s = 16;
            case_size = (int)s;
        }

        boolean first = true;
        cases_min_x = cases_min_y = cases_max_x = cases_max_y = 0;
        for(int i = 0;i<names.length;i++) {
            if (instancesToIgnore[i]) continue;
            Point2D l = new Point2D.Double(loc_x[i], loc_y[i]);
            if (first) {
                cases_min_x = cases_max_x = l.getX();
                cases_min_y = cases_max_y = l.getY();
                first = false;
            } else {
                if (l.getX()<cases_min_x) cases_min_x = l.getX();
                if (l.getX()>cases_max_x) cases_max_x = l.getX();
                if (l.getY()<cases_min_y) cases_min_y = l.getY();
                if (l.getY()>cases_max_y) cases_max_y = l.getY();
            }
        }
        
        // draw the cases:
        fw.write("<svg xmlns=\"http://www.w3.org/2000/svg\">\n");
        fw.write("<g transform=\"scale(1)\">\n");
        for(int i = 0;i<names.length;i++) {
            if (instancesToIgnore[i]) continue;
            String solution;
            int x = caseX(i,dx,dy);
            int y = caseY(i,dx,dy);
            solution = labels1[i];
            Color color = labelColors.get(solution);
            
            String hexColor = Integer.toHexString(color.getRGB() & 0xffffff);
            if (hexColor.length() < 6) hexColor = "000000".substring(0, 6 - hexColor.length()) + hexColor;
            
            fw.write("<ellipse cx=\"" + x + "\" cy=\"" + y + "\" rx=\"" + case_size/2 + "\" ry=\"" + case_size/2 + "\" fill=\"#" + hexColor + "\" stroke=\"black\" stroke-width=\"1\"/>\n");
        }    
        
        if (SHOW_KEY) {
            int x = 10;
            int y = 10;
            if (m_continuous1) {
                double range = maxContinuousLabel1 - minContinuousLabel1;
                for(int i = 0;i<=COLOR_STEPS;i+=COLOR_STEPS/8) {
                    double f = i/(double)COLOR_STEPS;                    
                    // g.setColor(continuousColors[i]);
                    // g.fillRect(x, y, 30, 15);
                    // g.setColor(Color.black);
                    // g.drawRect(x, y, 30, 15);
                    // g.drawString("" + minContinuousLabel + f*range, x + 40, y+12);
                    y+=20;
                }
            } else {
                for(String cName:toAppearInKey) {
                    Integer count = labelCounts.get(cName);
                    if (count!=null) {
                        Color color = labelColors.get(cName);
                        String hexColor = Integer.toHexString(color.getRGB() & 0xffffff);
                        if (hexColor.length() < 6) hexColor = "000000".substring(0, 6 - hexColor.length()) + hexColor;

                        fw.write("<rect x=\"" + x + "\" y=\"" + y + "\" width=\"30\" height=\"15\" fill=\"#" + hexColor + "\" stroke=\"black\" stroke-width=\"1\" />\n");
                        fw.write("<text x=\"" + (x+40) + "\" y=\"" + (y+12) + "\" style=\"font-size:16\" fill=\"black\">" + cName + "</text>\n");
    //                    g.drawString(cName, x + 40, y+12);
                        y+=20;
                    }
                }
            }
        }
        
        fw.write("</g>\n");        
        fw.write("</svg>\n");
        fw.close();
    }   

    
    public double computeDistortionAsPearson(DistanceMatrix m) {
        int l = names.length;

        double average1 = 0;
        double average2 = 0;
        double stddev1 = 0;
        double stddev2 = 0;
        double total = 0;
        
        double dx,dy,d;

        total = 0;
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (instancesToIgnore[j] || Double.isNaN(m.get(i,j))) continue;
                dx = loc_x[j] - loc_x[i];
                dy = loc_y[j] - loc_y[i];
                d = Math.sqrt(dx*dx+dy*dy);
                average1+=d;
                average2+=m.get(i,j);
                total++;
            }
        }
        average1/=total;
        average2/=total;
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (instancesToIgnore[j] || Double.isNaN(m.get(i,j))) continue;
                dx = loc_x[j] - loc_x[i];
                dy = loc_y[j] - loc_y[i];
                d = Math.sqrt(dx*dx+dy*dy);
                stddev1+= (d - average1)*(d - average1);
                stddev2+= (m.get(i,j) - average2)*(m.get(i,j) - average2);
            }
        }
        stddev1 = Math.sqrt(stddev1/total);
        stddev2 = Math.sqrt(stddev2/total);
        
        double accum = 0;
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (instancesToIgnore[j] || Double.isNaN(m.get(i,j))) continue;
                dx = loc_x[j] - loc_x[i];
                dy = loc_y[j] - loc_y[i];
                d = Math.sqrt(dx*dx+dy*dy);
                accum += (d-average1)*(m.get(i,j)-average2);
            }
        }
        return (accum/total) / (stddev1*stddev2);
    }

    
    public double computeDistortionAsAvgError(DistanceMatrix m) {
        double accum = 0;
        double total = 0;
        int l = names.length;
        double dx,dy,d;
        
        double average2 = 0;
        double stddev2 = 0;
        
        total = 0;
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (instancesToIgnore[j] || Double.isNaN(m.get(i,j))) continue;
                dx = loc_x[j] - loc_x[i];
                dy = loc_y[j] - loc_y[i];
                d = Math.sqrt(dx*dx+dy*dy);
                accum += Math.abs(d - m.get(i,j));
                average2+=m.get(i,j);
                total++;
            }
        }
        average2/=total;
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (instancesToIgnore[j] || Double.isNaN(m.get(i,j))) continue;
                stddev2+= (m.get(i,j) - average2)*(m.get(i,j) - average2);
            }
        }
        stddev2 = Math.sqrt(stddev2/total);        
        
        // return how many standard deviations
        return (accum/stddev2)/total;
    }    
}