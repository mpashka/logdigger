package com.iv.logView.ui;

import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

public class JSelector extends JComponent {

    private final JList leftLst;
    private final JList rightLst;

    public JSelector(Collection<String> all, Collection<String> selected) {
        leftLst = new JList();
        rightLst = new JList();
        init(all, selected);
    }

    protected void init(Collection<String> all, Collection<String> selected) {
        double b = 5.0;
        double p = TableLayout.PREFERRED;
        double f = TableLayout.FILL;
        double[] cols = new double[]{b, f, b, p, b, f, b};
        double[] rows = new double[]{b, p, f, p, p, f, b};
        TableLayout layout = new TableLayout(cols, rows);
        setLayout(layout);

        final MouseListener ml = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JList fromLst = (JList) e.getSource();
                    JList toLst = (fromLst == leftLst) ? rightLst : leftLst;
                    moveElements(fromLst, toLst);
                }
            }
        };

        JLabel leftLbl =new JLabel("Hidden:");
        leftLbl.setDisplayedMnemonic('H');
        leftLbl.setLabelFor(leftLst);
        add(leftLbl, "1, 1");
        JLabel rightLbl = new JLabel("Visible:");
        rightLbl.setDisplayedMnemonic('V');
        rightLbl.setLabelFor(rightLst);
        add(rightLbl, "5, 1");

        DefaultListModel lm = new DefaultListModel();
        Collection<String> col = new LinkedList<String>(all);
        col.removeAll(selected);
        for (String str : col) {
            putElement(lm, str);
        }
        leftLst.setModel(lm);
        leftLst.addMouseListener(ml);
        JScrollPane lScroll = new JScrollPane(leftLst);
        add(lScroll, "1, 2, 1, 5");

        add(new JButton(new ToLeftAction()), "3, 3");
        add(new JButton(new ToRightAction()), "3, 4");

        DefaultListModel rm = new DefaultListModel();
        for (String str : selected) {
            putElement(rm, str);
        }
        rightLst.setModel(rm);
        rightLst.addMouseListener(ml);
        JScrollPane rScroll = new JScrollPane(rightLst);
        add(rScroll, "5, 2, 5, 5");

        if (lm.isEmpty()) {
            lScroll.setPreferredSize(rScroll.getPreferredSize());
        } else if (rm.isEmpty()) {
            rScroll.setPreferredSize(lScroll.getPreferredSize());
        }
    }

    public Collection<String> getSelected() {
        Collection<String> result = new LinkedList<String>();
        for (Enumeration en = ((DefaultListModel) rightLst.getModel()).elements(); en.hasMoreElements();) {
            result.add((String) en.nextElement());
        }
        return result;
    }

    private static void putElement(DefaultListModel model, String element) {
        int pos = 0;
        for (; pos < model.size(); pos++) {
            String c = (String) model.getElementAt(pos);
            if (c.compareToIgnoreCase(element) >= 0) break;
        }
        model.insertElementAt(element, pos);
    }

    private void moveElements(JList fromList, JList toList) {
        int[] idx = fromList.getSelectedIndices();
        DefaultListModel fromModel = (DefaultListModel) fromList.getModel();
        DefaultListModel toModel = (DefaultListModel) toList.getModel();
        Arrays.sort(idx);
        for (int i = idx.length - 1; i >= 0; i--) {
            String o = (String) fromModel.getElementAt(idx[i]);
            fromModel.removeElementAt(idx[i]);
            putElement(toModel, o);
        }
    }

    private class ToLeftAction extends AbstractAction {
        public ToLeftAction() {
            putValue(NAME, "<");
//            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            moveElements(rightLst, leftLst);
        }
    }

    private class ToRightAction extends AbstractAction {
        public ToRightAction() {
            putValue(NAME, ">");
//            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            moveElements(leftLst, rightLst);
        }
    }

}
