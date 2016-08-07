/*
 * Copyright 2006 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressDialog
  extends JDialog
  implements ProgressListener
{
  private JPanel detailsComponent = new JPanel();
  private JProgressBar bar = new JProgressBar(0, 100);
  private boolean cancelled;

  public ProgressDialog(Frame owner, String title)
    throws HeadlessException
  {
    super(owner, title, true);
    bar.setStringPainted(true);

    Box outerBox = new Box(BoxLayout.Y_AXIS);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setAlignmentY(CENTER_ALIGNMENT);
    cancelButton.setAlignmentX(CENTER_ALIGNMENT);
    cancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancelled = true;
        setVisible(false);
        dispose();
      }
    });

    outerBox.add(detailsComponent);
    outerBox.add(bar);
    outerBox.add(Box.createVerticalGlue());
    outerBox.add(cancelButton);
    add(outerBox);

    centerDialog(owner);
  }

  public void addDetailsPanel(JComponent component)
  {
    detailsComponent.add(component);
    centerDialog(getOwner());
  }
  
  private void centerDialog(Window owner)
  {
    pack();
    setLocation(owner.getX() + owner.getWidth() / 2 - getWidth() / 2,
        owner.getY() + owner.getHeight() / 2 - getHeight() / 2);
  }

  public void progress(int current, int total)
  {
    bar.setValue(100 * current / total);
    bar.setString(Integer.toString(current) + " / " + Integer.toString(total));
    if (bar.getValue() == 100)
    {
      setVisible(false);
      dispose();
    }
  }

  public boolean isCancelled()
  {
    return cancelled;
  }
}
