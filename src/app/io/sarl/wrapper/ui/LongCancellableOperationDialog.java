package io.sarl.wrapper.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class LongCancellableOperationDialog extends JDialog implements ControllerDialog
{
	public LongCancellableOperationDialog(JFrame parent, String title,
											String message, Runnable onCancel)
	{
		super(parent, title);
		this.setLocationRelativeTo(parent);
		this.setSize(300, 150);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);

		JLabel messageLabel = new JLabel(message);
		messageLabel.setAlignmentX(CENTER_ALIGNMENT);
		messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		JProgressBar progressBar = new JProgressBar();
		progressBar.setAlignmentX(CENTER_ALIGNMENT);
		progressBar.setIndeterminate(true);
		
		mainPanel.add(messageLabel);
		mainPanel.add(progressBar);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((ActionEvent e) ->
		{
			if (onCancel != null)
			{
				onCancel.run();
			}
			dispose();
		});
		
		bottomPanel.add(cancelButton);
		
		this.setVisible(true);
	}

	@Override
	public void close()
	{
		dispose();
	}
}
