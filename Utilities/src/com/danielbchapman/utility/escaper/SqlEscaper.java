/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SqlEscaper.java
 *
 * Created on Sep 13, 2011, 1:11:13 PM
 */
package com.danielbchapman.utility.escaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * 
 * @author dchapman
 */
public class SqlEscaper extends javax.swing.JFrame
{

	/**
	 * @param args
	 *          the command line arguments
	 */
	public static void main(String args[])
	{
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try
		{
			for(javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
					.getInstalledLookAndFeels())
			{
				if("Nimbus".equals(info.getName()))
				{
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch(ClassNotFoundException ex)
		{
			java.util.logging.Logger.getLogger(SqlEscaper.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}
		catch(InstantiationException ex)
		{
			java.util.logging.Logger.getLogger(SqlEscaper.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}
		catch(IllegalAccessException ex)
		{
			java.util.logging.Logger.getLogger(SqlEscaper.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}
		catch(javax.swing.UnsupportedLookAndFeelException ex)
		{
			java.util.logging.Logger.getLogger(SqlEscaper.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				new SqlEscaper().setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButton1;

	private javax.swing.JEditorPane jEditorPane1;

	private javax.swing.JScrollPane jScrollPane1;

	// End of variables declaration//GEN-END:variables
	/** Creates new form SqlEscaper */
	public SqlEscaper()
	{
		initComponents();
		setLocation(200, 200);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT
	 * modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{

		jScrollPane1 = new javax.swing.JScrollPane();
		jEditorPane1 = new javax.swing.JEditorPane();
		jButton1 = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("SQL Escaping Utility");

		jEditorPane1.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
		jScrollPane1.setViewportView(jEditorPane1);

		jButton1.setText("toString();");
		jButton1.addActionListener(new java.awt.event.ActionListener()
		{
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton1ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout
								.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING)
												.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 476,
														Short.MAX_VALUE)).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						layout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 321,
										Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jButton1).addContainerGap()));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton1ActionPerformed
		String text = String.copyValueOf(jEditorPane1.getText().toCharArray());
		BufferedReader reader = new BufferedReader(new StringReader(text));
		String line;
		StringBuilder builder = new StringBuilder();
		try
		{
			for(; (line = reader.readLine()) != null;)
			{
				builder.append("\"");
				line = line.replace("\"", "\\\"").replace("\t", "  ");
				builder.append(line);
				builder.append(" \\n\" +\r\n");
			}

			String newString = builder.toString();
			int index = newString.lastIndexOf("+");
			newString = String.copyValueOf(newString.toCharArray(), 0, index) + ";";
			jEditorPane1.setText(newString);
			System.out.println(newString);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}// GEN-LAST:event_jButton1ActionPerformed
}
