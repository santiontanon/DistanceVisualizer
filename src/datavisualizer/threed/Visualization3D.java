/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 
package datavisualizer.threed;
 
import datavisualizer.DistanceMatrix;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class Visualization3D extends JPanel {
    public static final int COLOR_STEPS = 32;
    public static final int MAX_CASE_SIZE_TO_BE_DRAWN = 64;

    public boolean SHOW_KEY = true;
    public List<String> toAppearInKey = new LinkedList<>();    
    
    public String []names = null;
    
    boolean m_continuous1 = false;
    boolean m_continuous2 = false;
    boolean m_continuous3 = false;

    boolean m_reverse1 = false;
    boolean m_reverse2 = false;
    boolean m_reverse3 = false;

    String []labels1 = null;
    String []labels2 = null;
    String []labels3 = null;
    public HashMap<String,Color> labelColors = new LinkedHashMap<>();

    Double []continuousLabels1 = null;
    Double []continuousLabels2 = null;
    Double []continuousLabels3 = null;
    double minContinuousLabel1 = 0, maxContinuousLabel1= 1;
    double minContinuousLabel2 = 0, maxContinuousLabel2= 1;
    double minContinuousLabel3 = 0, maxContinuousLabel3= 1;
    Color continuousColors[] = null;    // this is used when there is a single label set selected
    Color continuousColors3[][][] = null;   // this is used when more than one label set is selected
    
    public List<Integer> highlightedCases = new ArrayList<>();
 
    public Point3d locations[] = null;
    public HashMap<String,Integer> labelCounts = new LinkedHashMap<>();
    List<Color> availableColors = null;
    Random rand = new Random();
    public int m_base_case_size = 8;
    double cases_min_x = 0,cases_min_y = 0, cases_min_z = 0;
    double cases_max_x = 0,cases_max_y = 0, cases_max_z = 0;
     
    // Focus: these are relative to the center of all the points and their positions
    double center_x = 0;
    double center_y = 0;
    double center_z = 0;
    double scale = 1;
     
    double angle = 0;

    public boolean []instancesToIgnore = null;
    
    public boolean modifiedByMouseListener = false;
    
    public Visualization3D m_parent = null; // the visualization where the actual positions are stored

    
    public Visualization3D(Visualization3D v) {
        SHOW_KEY = v.SHOW_KEY;
        names = v.names;

        m_continuous1 = v.m_continuous1;
        m_continuous2 = v.m_continuous2;
        m_continuous3 = v.m_continuous3;

        m_reverse1 = v.m_reverse1;
        m_reverse2 = v.m_reverse2;
        m_reverse3 = v.m_reverse3;
        
        labels1 = v.labels1;
        labels2 = v.labels2;
        labels3 = v.labels3;
        labelColors.putAll(v.labelColors);

        continuousLabels1 = v.continuousLabels1;
        continuousLabels2 = v.continuousLabels2;
        continuousLabels3 = v.continuousLabels3;
        
        minContinuousLabel1 = v.minContinuousLabel1;
        maxContinuousLabel1 = v.maxContinuousLabel1;
        minContinuousLabel2 = v.minContinuousLabel2;
        maxContinuousLabel2 = v.maxContinuousLabel2;
        minContinuousLabel3 = v.minContinuousLabel3;
        maxContinuousLabel3 = v.maxContinuousLabel3;
        
        continuousColors = v.continuousColors;
        continuousColors3 = v.continuousColors3;
        
        locations = new Point3d[names.length];
        instancesToIgnore = new boolean[v.instancesToIgnore.length];
        for(int i = 0;i<instancesToIgnore.length;i++) {
            instancesToIgnore[i] = v.instancesToIgnore[i];
            locations[i] = new Point3d(v.locations[i].x, v.locations[i].y, v.locations[i].z);
        }
        
        Visualization3DListener ml = new Visualization3DListener(this);
        addMouseListener(ml);
        addMouseMotionListener(ml);
        addMouseWheelListener(ml);
 
        setKeyBindings();
         
        assignColors();
    }


    public Visualization3D(List<String> cl, List<String> ll, List<String> forKey, boolean continuous) {
        String []ll_array = new String [ll.size()];
        for(int i = 0;i<ll.size();i++) ll_array[i] = ll.get(i);
        names = ll_array;
        instancesToIgnore = new boolean[names.length];
        
        locations = new Point3d[names.length];
        for(int i = 0;i<names.length;i++) {
            locations[i] = new Point3d(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
        }
        
        setClasses(cl, null, null, forKey, continuous, false, false, false, false, false);
                
        Visualization3DListener ml = new Visualization3DListener(this);
        addMouseListener(ml);
        addMouseMotionListener(ml);
        addMouseWheelListener(ml);
 
        setKeyBindings();
         
        assignColors();
    }
     
 
    public void setParent(Visualization3D parent) {
        m_parent = parent;
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
     
     
    public static class randomPushAction extends AbstractAction {
        Visualization3D ap = null;
        public randomPushAction(Visualization3D a_ap) {
            ap = a_ap;
        }
        public void actionPerformed(ActionEvent e) {
            Visualization3D ap2 = ap;
            if (ap2.m_parent!=null) {
                ap2 = ap2.m_parent;
            }
            
            // use the "ap2" locations, but still use "ap" for the cases max and min,
            // since those are set when drawing, and the parent might never be drawn
            for(Point3d p:ap2.locations) {
                p.setLocation(p.getX()+0.25*ap.rand.nextDouble()*(ap.cases_max_x-ap.cases_min_x), 
                              p.getY()+0.25*ap.rand.nextDouble()*(ap.cases_max_y-ap.cases_min_y),
                              p.getZ()+0.25*ap.rand.nextDouble()*(ap.cases_max_z-ap.cases_min_z));
            }
        }        
    }
     
     
    public void setKeyBindings() {        
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
                           boolean continuous3,
                           boolean reverse1,
                           boolean reverse2,
                           boolean reverse3) {
        m_continuous1 = continuous1;
        m_continuous2 = continuous2;
        m_continuous3 = continuous3;
        
        m_reverse1 = reverse1;
        m_reverse2 = reverse2;
        m_reverse3 = reverse3;

        if (cl1==null) {
            labels1 = null;
            continuousLabels1 = null;
        } else {
            String []cl_array = new String [cl1.size()];
            for(int i = 0;i<cl1.size();i++) cl_array[i] = cl1.get(i);
            labels1 = cl_array;
            if (m_continuous1) {
                continuousLabels1 = new Double[names.length];
                for(int i = 0;i<names.length;i++) {
                    if (labels1[i] != null) {
                        continuousLabels1[i] = new Double(Double.parseDouble(labels1[i]));
                    } else {
                        continuousLabels1[i] = null;
                    }
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
                continuousLabels2 = new Double[names.length];
                for(int i = 0;i<names.length;i++) {
                    if (labels2[i] != null) {
                        continuousLabels2[i] = new Double(Double.parseDouble(labels2[i]));
                    } else {
                        continuousLabels2[i] = null;
                    }
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
                continuousLabels3 = new Double[names.length];
                for(int i = 0;i<names.length;i++) {
                    if (labels3[i] != null) {
                        continuousLabels3[i] = new Double(Double.parseDouble(labels3[i]));
                    } else {
                        continuousLabels3[i] = null;
                    }
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

        Rectangle r = getBounds();
        drawCaseBase(g, 0,0,r.width,r.height, Color.black, m_base_case_size);
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
                    (m_continuous3 && labels3!=null)) {
                    if (m_reverse1) {
                        g.setColor(continuousColors3[COLOR_STEPS-i][0][0]);
                    } else {
                        g.setColor(continuousColors3[i][0][0]);
                    }
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
                    if (m_reverse2) {
                        g.setColor(continuousColors3[0][COLOR_STEPS-i][0]);
                    } else {
                        g.setColor(continuousColors3[0][i][0]);
                    }
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
                    if (m_reverse3) {
                        g.setColor(continuousColors3[0][0][COLOR_STEPS-i]);
                    } else {
                        g.setColor(continuousColors3[0][0][i]);
                    }
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
            for(Double cl:continuousLabels1) {
                if (cl==null) continue;
                if (cl<minContinuousLabel1) minContinuousLabel1 = cl;
                if (cl>maxContinuousLabel1) maxContinuousLabel1 = cl;
            }
            if (m_continuous2) {
                for(Double cl:continuousLabels2) {
                    if (cl==null) continue;
                    if (cl<minContinuousLabel2) minContinuousLabel2 = cl;
                    if (cl>maxContinuousLabel2) maxContinuousLabel2 = cl;
                }
            }
            if (m_continuous3) {
                for(Double cl:continuousLabels3) {
                    if (cl==null) continue;
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
  
    
    public void drawCaseBase(Graphics g,int sx,int sy,int dx,int dy, Color outlineColor, int base_case_size) {
        boolean first = true;

        cases_min_x = cases_min_y = cases_max_x = cases_max_y = cases_min_z = cases_max_z = 0;
        for(int i = 0;i<names.length;i++) {
            if (instancesToIgnore[i]) continue;
            Point3d l = locations[i];
            if (first) {
                cases_min_x = cases_max_x = l.getX();
                cases_min_y = cases_max_y = l.getY();
                cases_min_z = cases_max_z = l.getZ();
                first = false;
            } else {
                if (l.getX()<cases_min_x) cases_min_x = l.getX();
                if (l.getX()>cases_max_x) cases_max_x = l.getX();
                if (l.getY()<cases_min_y) cases_min_y = l.getY();
                if (l.getY()>cases_max_y) cases_max_y = l.getY();
                if (l.getZ()<cases_min_z) cases_min_z = l.getZ();
                if (l.getZ()>cases_max_z) cases_max_z = l.getZ();
            }
        }

//        System.out.println("[" + cases_min_x + "," + cases_max_x + "] - [" + cases_min_y + "," + cases_max_y + "]");

        // sort by depth (only do it if there is a small set of cases:
        List<Integer> indexes = new ArrayList<Integer>();
        List<Double> depth = new ArrayList<Double>();
        for(int i = 0;i<names.length;i++) {
            indexes.add(i);
            depth.add(caseZ(i,dx,dy));
        }
        Collections.sort(indexes, new Comparator<Integer>() {
            public int compare(Integer a,Integer b) {
                return -Double.compare(depth.get(a), depth.get(b));
            }
        });
        
        // draw the cases:
        for(int i:indexes) {
            if (instancesToIgnore[i]) continue;
            String solution;
            int x = caseX(i,dx,dy, base_case_size);
            int y = caseY(i,dx,dy, base_case_size);
            int case_size = caseSize(i, dx, dy, base_case_size);
            if (case_size>=MAX_CASE_SIZE_TO_BE_DRAWN) continue;
            if (labels1==null) {
                g.setColor(Color.black);
            } else {
                solution = labels1[i];
                if (m_continuous1) {
                    int idx;
                    if (continuousLabels1[i] == null) {
                        idx = 0;
                    } else {
                        idx = (int)(COLOR_STEPS * (continuousLabels1[i] - minContinuousLabel1)/(maxContinuousLabel1 - minContinuousLabel1));
                    }
                    if (m_reverse1) idx = COLOR_STEPS - idx;
                    if (idx<0) idx = 0;
                    if (idx>COLOR_STEPS) idx = COLOR_STEPS;
                    if (m_continuous2 && labels2!=null) {
                        int idx2;
                        if (continuousLabels2[i] == null) {
                            idx2 = 0;
                        } else {
                            idx2 = (int)(COLOR_STEPS * (continuousLabels2[i] - minContinuousLabel2)/(maxContinuousLabel2 - minContinuousLabel2));
                        }
                        if (m_reverse2) idx2 = COLOR_STEPS - idx2;
                        if (idx2<0) idx2 = 0;
                        if (idx2>COLOR_STEPS) idx2 = COLOR_STEPS;
                        if (m_continuous3 && labels3!=null) {
                            int idx3;
                            if (continuousLabels3[i] == null) {
                                idx3 = 0;
                            } else {
                                idx3 = (int)(COLOR_STEPS * (continuousLabels3[i] - minContinuousLabel3)/(maxContinuousLabel3 - minContinuousLabel3));
                            }
                            if (m_reverse3) idx3 = COLOR_STEPS - idx3;
                            if (idx3<0) idx3 = 0;
                            if (idx3>COLOR_STEPS) idx3 = COLOR_STEPS;
                            g.setColor(continuousColors3[idx][idx2][idx3]);
                        } else {
                            g.setColor(continuousColors3[idx][idx2][0]);
                        }
                    } else {
                        if (m_continuous3 && labels3!=null) {
                            int idx3 = (int)(COLOR_STEPS * (continuousLabels3[i] - minContinuousLabel3)/(maxContinuousLabel3 - minContinuousLabel3));
                            if (m_reverse3) idx3 = COLOR_STEPS - idx3;
                            if (idx3<0) idx3 = 0;
                            if (idx3>COLOR_STEPS) idx3 = COLOR_STEPS;
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
            g.setColor(outlineColor);
            g.drawArc(x-(case_size)/2, y-(case_size)/2, case_size, case_size, 0, 360);
        }

        
        if (!highlightedCases.isEmpty()) {
            int hlcx = -1, hlcy = -1;
            for(int highlightedCase: highlightedCases) {
                String name = names[highlightedCase];
                hlcx = caseX(highlightedCase,dx,dy, base_case_size);
                hlcy = caseY(highlightedCase,dx,dy, base_case_size);
                int case_size = caseSize(highlightedCase, dx, dy, base_case_size);
                Color color = null;
                String solution;
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
                g.fillRect(hlcx+base_case_size/2-tdx/2, hlcy+base_case_size+2, tdx, tdy);
                g.setColor(Color.black);
                g.drawRect(hlcx+base_case_size/2-tdx/2, hlcy+base_case_size+2, tdx, tdy);
                int ystart = (int)(hlcy+base_case_size+2)+padding;
                for(int i = 0;i<lines.size();i++) {
                    String line = lines.get(i);
                    Rectangle2D r = bounds.get(i);
                    g.drawString(line, hlcx+base_case_size/2-tdx/2+padding, (int)(ystart + r.getHeight()));
                    ystart+=r.getHeight() + padding;
                }
//                g.drawString(s2, x+case_size/2-tdx/2+padding, (int)(y+case_size+2+r1.getHeight()+r2.getHeight())+padding*2);
            }
        }
    }    
    
    public double maxDimension() {
        return Math.max(cases_max_x-cases_min_x,
                        Math.max(cases_max_y-cases_min_y,cases_max_z-cases_min_z));
    }
    

    public int caseX(int i, int dx, int dy, int base_case_size) {
        Point3d l = locations[i];
        double dim = maxDimension();

        double centerx = (cases_min_x + cases_max_x)/2;
        double x = (l.getX()-centerx)/dim;
        x -= center_x;
        x*=scale;

        /*
        double centery = (cases_min_y + cases_max_y)/2;
        double y = (l.getY()-centery)/dim;
        y -= center_y;
        y*=scale;
        */
        
        double centerz = (cases_min_z + cases_max_z)/2;
        double z = (l.getZ()-centerz)/dim;
        z -= center_z;
        z*=scale;
        
        double rx = x*Math.cos(angle) - z*Math.sin(angle);
        double rz = x*Math.sin(angle) + z*Math.cos(angle);
        
        rx = (rx)/(1+rz);        
        
        double divisor = Math.min(dx,dy)-base_case_size*3;
//        return (int)(dx/2 + ((dx-base_case_size*3)-divisor)/2 + base_case_size + rx*divisor);
        return (int)(dx/2 + rx*divisor);
    }

    public int caseY(int i, int dx, int dy, int base_case_size) {
        Point3d l = locations[i];
        
        double dim = maxDimension();
        
        double centerx = (cases_min_x + cases_max_x)/2;
        double x = (l.getX()-centerx)/dim;
        x -= center_x;
        x*=scale;
        
        double centery = (cases_min_y + cases_max_y)/2;
        double y = (l.getY()-centery)/dim;
        y -= center_y;
        y*=scale;
        
        double centerz = (cases_min_z + cases_max_z)/2;
        double z = (l.getZ()-centerz)/dim;
        z -= center_z;
        z*=scale;
        
        double rz = x*Math.sin(angle) + z*Math.cos(angle);

        y = (y)/(1+rz);
        
        double divisor = Math.min(dx,dy)-base_case_size*3;
//        return (int)(dy/2 + ((dy-base_case_size*3)-divisor)/2 + base_case_size + y*divisor);
        return (int)(dy/2 + y*divisor);
    }
    
     
    public double caseZ(int i, int dx, int dy) {
        Point3d l = locations[i];
        double dim = maxDimension();

        double centerx = (cases_min_x + cases_max_x)/2;
        double x = (l.getX()-centerx)/dim;
        x -= center_x;
        x*=scale;

        /*
        double centery = (cases_min_y + cases_max_y)/2;
        double y = (l.getY()-centery)/dim;
        y -= center_y;
        y*=scale;
        */
        
        double centerz = (cases_min_z + cases_max_z)/2;
        double z = (l.getZ()-centerz)/dim;
        z -= center_z;
        z*=scale;
        
//        double rx = x*Math.cos(angle) - z*Math.sin(angle);
        double rz = x*Math.sin(angle) + z*Math.cos(angle);

        return rz;
    }        
    
    
    public int caseSize(int i, int dx, int dy, int base_case_size) {
        Point3d l = locations[i];
        double dim = maxDimension();

        double centerx = (cases_min_x + cases_max_x)/2;
        double x = (l.getX()-centerx)/dim;
        x -= center_x;
        x*=scale;

        /*
        double centery = (cases_min_y + cases_max_y)/2;
        double y = (l.getY()-centery)/dim;
        y -= center_y;
        y*=scale;
        */
        
        double centerz = (cases_min_z + cases_max_z)/2;
        double z = (l.getZ()-centerz)/dim;
        z -= center_z;
        z*=scale;
        
//        double rx = x*Math.cos(angle) - z*Math.sin(angle);
        double rz = x*Math.sin(angle) + z*Math.cos(angle);

        double cs = scale*(base_case_size)/(0.9+rz);        
        
//        double divisor = Math.min(dx,dy)-case_size*3;
//        return (int)(dx/2 + ((dx-case_size*3)-divisor)/2 + case_size + rx*divisor);
        return (int)cs;
    }        
    
 
    /*
    void saveToSVG(String fileName, int dx,int dy) throws Exception {
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
            Point2D l = locations[i];
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
    */
    
    public double computeDistortionAsPearson(DistanceMatrix m) {
        int l = names.length;

        double average1 = 0;
        double average2 = 0;
        double stddev1 = 0;
        double stddev2 = 0;
        double total = 0;
        
        double dx,dy,dz,d;

        total = 0;
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (instancesToIgnore[j] || Double.isNaN(m.get(i,j))) continue;
                dx = locations[j].x - locations[i].x;
                dy = locations[j].y - locations[i].y;
                dz = locations[j].z - locations[i].z;
                d = Math.sqrt(dx*dx+dy*dz*dz);
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
                dx = locations[j].x - locations[i].x;
                dy = locations[j].y - locations[i].y;
                dz = locations[j].z - locations[i].z;
                d = Math.sqrt(dx*dx+dy*dz*dz);
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
                dx = locations[j].x - locations[i].x;
                dy = locations[j].y - locations[i].y;
                dz = locations[j].z - locations[i].z;
                d = Math.sqrt(dx*dx+dy*dz*dz);
                accum += (d-average1)*(m.get(i,j)-average2);
            }
        }
        return (accum/total) / (stddev1*stddev2);
    }

    
    public double computeDistortionAsAvgError(DistanceMatrix m) {
        double accum = 0;
        double total = 0;
        int l = names.length;
        double dx,dy,dz,d;
        
        double average2 = 0;
        double stddev2 = 0;
        
        total = 0;
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (instancesToIgnore[j] || Double.isNaN(m.get(i,j))) continue;
                dx = locations[j].x - locations[i].x;
                dy = locations[j].y - locations[i].y;
                dz = locations[j].z - locations[i].z;
                d = Math.sqrt(dx*dx+dy*dz*dz);
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
    
    
    public void syncWith(Visualization3D v) {
        for(int i = 0;i<locations.length;i++) {
            locations[i].x = v.locations[i].x;
            locations[i].y = v.locations[i].y;
            locations[i].z = v.locations[i].z;
            instancesToIgnore[i] = v.instancesToIgnore[i];
        }
        
        if (modifiedByMouseListener) {
            v.center_x = center_x;
            v.center_y = center_y;
            v.center_z = center_z;
            v.angle = angle;
            v.scale = scale;
        } else {
            center_x = v.center_x;
            center_y = v.center_y;
            center_z = v.center_z;
            angle = v.angle;
            scale = v.scale;
        }
    }
}