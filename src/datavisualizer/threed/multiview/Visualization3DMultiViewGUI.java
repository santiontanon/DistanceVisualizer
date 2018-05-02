/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer.threed.multiview;

import datavisualizer.DistanceMatrix;
import datavisualizer.InstancePositions;
import datavisualizer.threed.Visualization3D;
import datavisualizer.threed.positioncontrollers.ForceDistanceMatrixVisualization3D;
import datavisualizer.threed.positioncontrollers.InstancePositions3DController;
import java.awt.Color;
import java.awt.Dimension;
import java.util.*;
import javax.swing.*;
import util.Pair;
import datavisualizer.threed.positioncontrollers.Visualization3DPositionController;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 *
 * @author santi
 */
public class Visualization3DMultiViewGUI extends JFrame {

    public static int MAX_VIEWS = 3;
    public static boolean SHOW_KEY = true;

    public static final int W_WIDTH = 1024;
    public static final int W_HEIGHT = 768;

    List<String> names = null;
    List<String> featureNames = null;
    List<String> outputFeatureNames = null;
    Object originalData[][] = null; // if available, this holds the original data as a matrix (they could be "Double" or "String")
    String imagesFolder = null;
    
    List<Pair<String, DistanceMatrix>> matrices = null;
    List<Pair<String, InstancePositions>> positions = null;
    
    List<Pair<String, Visualization3DPositionController>> controllers = null;
    JComboBox controllerSelectionBox = null;
    Checkbox freezeCheckBox = null;

    List<Pair<String, List<String>>> labelSets = null;

    public Visualization3D visualization = null;
    public List<Visualization3DMultiViewGUIView> views = new ArrayList<>();
    JPanel viewsPanel = null;
    
    // this variable is just so that action listeners can access to "this", referring to the 
    // Visualization3DMultiViewGUI, and not to the action listener itself
    Visualization3DMultiViewGUI accessibleThis = null;
    JFrame dataFilterFrame = null;
    JFrame dataToolsFrame = null;
    
    int controllerSelected = -1;

    public Visualization3DMultiViewGUI(String name,
            List<Pair<String, DistanceMatrix>> a_matrices,
            List<Pair<String, InstancePositions>> a_positions,
            List<Pair<String, List<String>>> a_labels,
            List<String> a_names, 
            List<String> a_featureNames,
            Object a_originalData[][],
            List<String> a_outputFeatures,  // these features are considered "output", and should not be used to calculate distances, PCA or t-SNE
            String a_imagesFolder, 
            int n_views) {
        super(name);
        accessibleThis = this;
        setPreferredSize(new Dimension(W_WIDTH, W_HEIGHT));
        setSize(W_WIDTH, W_HEIGHT);

        matrices = a_matrices;
        positions = a_positions;

        names = a_names;
        featureNames = a_featureNames;
        outputFeatureNames = a_outputFeatures;
        originalData = a_originalData;
        labelSets = a_labels;

        imagesFolder = a_imagesFolder;
        
        // if the original data is available, use it as labels:
        if (originalData!=null && featureNames!=null) {
            for(int i = 0;i<featureNames.size();i++) {
                String feature = featureNames.get(i);
                List<String> l = new ArrayList<>();
                for(int j = 0;j<originalData.length;j++) {
                    if (originalData[j][i]==null) {
                        l.add(null);
                    } else {
                        l.add("" + originalData[j][i]);
                    }
                }
                labelSets.add(new Pair<>(feature, l));
            }            
        }
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error setting native LAF: " + e);
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        setBackground(Color.WHITE);

        getContentPane().removeAll();
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // create the visualization:
        visualization = new Visualization3D(labelSets.get(0).m_b, names, new ArrayList<>(), Visualization3DMultiViewGUIView.areLabelsContinuous(labelSets.get(0).m_b), imagesFolder);
        visualization.SHOW_KEY = SHOW_KEY;

        // controllers:
        {
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
            controllers = new ArrayList<>();
            for (Pair<String, DistanceMatrix> tmp : matrices) {
                controllers.add(new Pair<String, Visualization3DPositionController>(tmp.m_a, new ForceDistanceMatrixVisualization3D(tmp.m_b, visualization)));
            }
            for (Pair<String, InstancePositions> tmp : positions) {
                controllers.add(new Pair<String, Visualization3DPositionController>(tmp.m_a, new InstancePositions3DController(tmp.m_b, visualization)));
            }
            controllerSelected = 0;
            topPanel.add(new JLabel("Select Position Controller:"));
            String[] controllerNames = new String[controllers.size()];
            for (int i = 0; i < controllers.size(); i++) {
                controllerNames[i] = controllers.get(i).m_a;
            }
            controllerSelectionBox = new JComboBox(controllerNames);
            topPanel.add(controllerSelectionBox);
            
            freezeCheckBox = new Checkbox("Freeze");
            freezeCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    repaint();
                }
            });
            topPanel.add(freezeCheckBox);
            
            JButton addView = new JButton("+1 view");
            addView.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (views.size()<MAX_VIEWS) {
                        Visualization3DMultiViewGUIView v = new Visualization3DMultiViewGUIView(accessibleThis, W_WIDTH / n_views);
                        views.add(v);
                        viewsPanel.add(v);
                        pack();
                    }
                }
            });
            topPanel.add(addView);
            JButton removeView = new JButton("-1 view");
            removeView.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (views.size()>1) {
                        Visualization3DMultiViewGUIView v = views.get(views.size()-1);
                        views.remove(v);
                        viewsPanel.remove(v);
                        pack();
                    }
                }
            });
            topPanel.add(removeView);
            JButton dataFilter = new JButton("Data Filter");
            dataFilter.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (dataFilterFrame==null) {
                        dataFilterFrame = new DataFilterGUI("Data Filter", 480, 400, accessibleThis);
                    }
                    dataFilterFrame.setResizable(true);
                    dataFilterFrame.setVisible(true);
                    dataFilterFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);   
                }
            });
            topPanel.add(dataFilter);
            
            JButton dataTools = new JButton("Data Tools");
            dataTools.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (dataToolsFrame==null) {
                        dataToolsFrame = new DataToolsGUI("Data", 480, 400, accessibleThis);
                    }
                    dataToolsFrame.setResizable(true);
                    dataToolsFrame.setVisible(true);
                    dataToolsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);   
                }
            });
            topPanel.add(dataTools);


            panel.add(topPanel);
        }

        // add views:
        viewsPanel = new JPanel();
        viewsPanel.setLayout(new BoxLayout(viewsPanel, BoxLayout.X_AXIS));
        for (int i = 0; i < n_views; i++) {
            Visualization3DMultiViewGUIView v = new Visualization3DMultiViewGUIView(this, W_WIDTH / n_views);
            views.add(v);
            viewsPanel.add(v);
        }
        panel.add(viewsPanel);

        getContentPane().add(panel);
        pack();
    }


    public void updateLoop() {
        boolean first = true;
        while (true) {
            int sleep_amount = 1;
            if (first) {
                // give time to SWING to redraw the whole thing at launch:
                sleep_amount = 500;
                first = false;
            }
            if (!freezeCheckBox.getState() && controllerSelected >= 0) {
                controllers.get(controllerSelected).m_b.updatePositions();
            } else {
                // if everthing is frozen, then we don't need to redraw that often:
                if (sleep_amount<100) sleep_amount = 100;
            }
            try {
                Thread.sleep(sleep_amount);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (controllerSelectionBox != null) {
                int selected = controllerSelectionBox.getSelectedIndex();
                if (selected != controllerSelected) {
                    controllerSelected = selected;
                }
            }

            for (Visualization3DMultiViewGUIView v : views) {
                v.update();
            }
            visualization.modifiedByMouseListener = false;
        }
    }

}
