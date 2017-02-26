/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer.threed.multiview;

import static datavisualizer.threed.multiview.Visualization3DMultiViewGUIView.areLabelsContinuous;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 *
 * @author santi
 */
public class DataFilterGUI extends JFrame {

    Visualization3DMultiViewGUI m_parent = null;

    JComboBox labelSetSelectionBox;
    JList displayedLabelsBox = null;
    JList hiddenLabelsBox = null;

    List<String> currentDisplayedLabels = new ArrayList<>();
    List<String> currentHiddenLabels = new ArrayList<>();

    public DataFilterGUI(String name, int width, int height, Visualization3DMultiViewGUI parent) {
        super(name);
        setPreferredSize(new Dimension(width, height));
        setSize(new Dimension(width, height));
        m_parent = parent;

        String[] labelNames = new String[m_parent.labelSets.size() + 1];
        labelNames[0] = null;
        for (int i = 0; i < m_parent.labelSets.size(); i++) {
            labelNames[i + 1] = m_parent.labelSets.get(i).m_a;
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        labelSetSelectionBox = new JComboBox(labelNames);
        labelSetSelectionBox.setPreferredSize(new Dimension(width, 32));
        p.add(labelSetSelectionBox);
        labelSetSelectionBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateLabelBoxes();
            }
        });
        add(p);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        displayedLabelsBox = new JList<String>(new String[]{});
        hiddenLabelsBox = new JList<String>(new String[]{});
        displayedLabelsBox.setAlignmentY(TOP_ALIGNMENT);
        hiddenLabelsBox.setAlignmentY(TOP_ALIGNMENT);

        p2.add(displayedLabelsBox);

        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        JButton hide = new JButton("hide");
        hide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selected = displayedLabelsBox.getSelectedIndex();
                if (selected >= 0) {
                    int labelsSelected = labelSetSelectionBox.getSelectedIndex();
                    List<String> labels = m_parent.labelSets.get(labelsSelected - 1).m_b;
                    String label = currentDisplayedLabels.get(selected);
                    
                    for(int i = 0;i<labels.size();i++) {
                        if (labels.get(i).equals(label)) {
                            m_parent.visualization.instancesToIgnore[i] = true;
                        }
                    }
                    updateLabelBoxes();
                }
            }
        });
        JButton display = new JButton("display");
        display.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selected = hiddenLabelsBox.getSelectedIndex();
                if (selected >= 0) {
                    int labelsSelected = labelSetSelectionBox.getSelectedIndex();
                    List<String> labels = m_parent.labelSets.get(labelsSelected - 1).m_b;
                    String label = currentHiddenLabels.get(selected);
                    
                    for(int i = 0;i<labels.size();i++) {
                        if (labels.get(i).equals(label)) {
                            m_parent.visualization.instancesToIgnore[i] = false;
                        }
                    }
                    updateLabelBoxes();
                }
            }
        });
        JButton color = new JButton("color");
        color.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selected = displayedLabelsBox.getSelectedIndex();
                if (selected >= 0) {
                    String label = currentDisplayedLabels.get(selected);
                    changeLabelColor(label);
                }
            }
        });
        p3.add(hide);
        p3.add(display);
        p3.add(color);
        p3.setAlignmentY(TOP_ALIGNMENT);
        p2.add(p3);
        p2.add(hiddenLabelsBox);
        p.add(p2);
    }
    

    public void updateLabelBoxes() {
        int selected = labelSetSelectionBox.getSelectedIndex();
        List<String> labels = m_parent.labelSets.get(selected - 1).m_b;
        boolean currentLabelsContinuous = areLabelsContinuous(labels);
        String currentDisplayedLabelsArray[] = null;
        String currentHiddenLabelsArray[] = null;

        currentDisplayedLabels.clear();
        currentHiddenLabels.clear();
        if (currentLabelsContinuous) {
            currentDisplayedLabelsArray = new String[]{};
            currentHiddenLabelsArray = new String[]{};
        } else {
            HashMap<String, Integer> displayedCounts = new HashMap<>();
            HashMap<String, Integer> hiddenCounts = new HashMap<>();

            // count the number of displayer/hidden instances of each label:
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                if (m_parent.visualization.instancesToIgnore[i]) {
                    Integer c = hiddenCounts.get(label);
                    if (c == null) {
                        c = 0;
                        currentHiddenLabels.add(label);
                    }
                    c++;
                    hiddenCounts.put(label, c);
                } else {
                    Integer c = displayedCounts.get(label);
                    if (c == null) {
                        c = 0;
                        currentDisplayedLabels.add(label);
                    }
                    c++;
                    displayedCounts.put(label, c);
                }
            }
            currentDisplayedLabelsArray = new String[currentDisplayedLabels.size()];
            currentHiddenLabelsArray = new String[currentHiddenLabels.size()];
            for (int i = 0; i < currentDisplayedLabels.size(); i++) {
                currentDisplayedLabelsArray[i] = currentDisplayedLabels.get(i) + "(" + displayedCounts.get(currentDisplayedLabels.get(i)) + ")";
            }
            for (int i = 0; i < currentHiddenLabels.size(); i++) {
                currentHiddenLabelsArray[i] = currentHiddenLabels.get(i) + "(" + hiddenCounts.get(currentHiddenLabels.get(i)) + ")";
            }
        }

        displayedLabelsBox.setListData(currentDisplayedLabelsArray);
        hiddenLabelsBox.setListData(currentHiddenLabelsArray);
        repaint();
    }

    public void changeLabelColor(String label) {
        Color currentColor = null;
        for(Visualization3DMultiViewGUIView v:m_parent.views) {
            currentColor = v.m_visualization.labelColors.get(label);
            if (currentColor != null) break;
        }
        
        if (currentColor != null) {
            JFrame dialogue = new JFrame("Change Color for " + label);
            dialogue.setPreferredSize(new Dimension(100, 140));
            dialogue.setSize(100, 140);
            dialogue.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JTextField rtext = new JTextField("" + currentColor.getRed());
            JTextField gtext = new JTextField("" + currentColor.getGreen());
            JTextField btext = new JTextField("" + currentColor.getBlue());
            panel.add(rtext);
            panel.add(gtext);
            panel.add(btext);
            JButton set = new JButton("set");
            set.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int r = Integer.parseInt(rtext.getText());
                        int g = Integer.parseInt(gtext.getText());
                        int b = Integer.parseInt(btext.getText());

                        for(Visualization3DMultiViewGUIView v:m_parent.views) {
                            if (v.m_visualization.labelColors.get(label)!=null) {
                                v.m_visualization.labelColors.put(label, new Color(r, g, b));
                            }
                        }
                    } catch (Exception ex) {

                    }
                }
            });
            panel.add(set);
            dialogue.add(panel);
            dialogue.setVisible(true);
        }
    }

}
