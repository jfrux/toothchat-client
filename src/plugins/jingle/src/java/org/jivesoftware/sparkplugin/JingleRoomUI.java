/**
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-2005 Jive Software. All rights reserved.
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package org.jivesoftware.sparkplugin;

import jvolume.JVolume;
import jvolume.JavaMixerHelper;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.spark.ChatManager;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.component.RolloverButton;
import org.jivesoftware.spark.component.tabbedPane.SparkTab;
import org.jivesoftware.spark.ui.ChatRoom;
import org.jivesoftware.smack.XMPPException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * The UI for calls with Roster members.
 *
 * @author Derek DeMoro
 */
public class JingleRoomUI extends JPanel {

    private JLabel connectedLabel;
    private String phoneNumber;
    private JLabel phoneLabel;
    private PreviousConversationPanel historyPanel;

    private boolean onHold;
    private boolean muted;

    private RosterMemberCallButton muteButton;
    private RosterMemberCallButton holdButton;
    private RosterMemberCallButton transferButton;

    private RolloverButton hangUpButton;

    private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy h:mm a");

    private static String CONNECTED = "Connected";


    protected final Color greenColor = new Color(91, 175, 41);
    protected final Color orangeColor = new Color(229, 139, 11);
    protected final Color blueColor = new Color(64, 103, 162);
    protected final Color redColor = new Color(211, 0, 0);

    private boolean callWasTransferred;

    private ChatRoom chatRoom;

    private JingleSession session;

    public JingleRoomUI(JingleSession session, ChatRoom chatRoom) {
        this.session = session;
        this.chatRoom = chatRoom;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createLineBorder(Color.lightGray));

        // Build Top Layer
        final JPanel topPanel = buildTopPanel();
        add(topPanel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        // Build Control Panel
        final JPanel controlPanel = buildControlPanel();
        add(controlPanel, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        // Add Previous Conversation
        historyPanel = new PreviousConversationPanel();
        historyPanel.addPreviousConversations("");
        add(historyPanel, new GridBagConstraints(1, 8, 1, 1, 0.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 100));

        // Setup default settings
        setupDefaults();
    }


    /**
     * Builds the information block.
     *
     * @return the UI representing the Information Block.
     */
    private JPanel buildTopPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        // Add phone label
        phoneLabel = new JLabel();
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 13));
        phoneLabel.setForeground(new Color(64, 103, 162));
        panel.add(phoneLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 2, 2, 2), 0, 0));

        // Add Connected Label
        connectedLabel = new JLabel(CONNECTED);
        connectedLabel.setFont(new Font("Arial", Font.BOLD, 13));
        connectedLabel.setHorizontalTextPosition(JLabel.CENTER);
        connectedLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(connectedLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        return panel;
    }

    /**
     * Builds the Control Panel.
     *
     * @return the control panel.
     */
    private JPanel buildControlPanel() {
        // Add Control Panel
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);

        // Initialize Mixer.
        final JavaMixerHelper javaMixerHelper = new JavaMixerHelper();

        // Add Input Volume To Control Panel
        final ControlPanel inputPanel = new ControlPanel(new GridBagLayout());
        JVolume inputVolume = new JVolume(javaMixerHelper.getMicInputJavaMixer());
        inputVolume.setOrientation(JSlider.VERTICAL);

        final JLabel inputIcon = new JLabel(JinglePhoneRes.getImageIcon("SPEAKER_IMAGE"));
        inputPanel.add(inputVolume, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(2, 2, 2, 2), 0, 0));
        inputPanel.add(inputIcon, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        // Add Output Volume To Control Panel
        final ControlPanel outputPanel = new ControlPanel(new GridBagLayout());
        JVolume outputVolume = new JVolume(javaMixerHelper.getMasterPlaybackJavaMixer());
        outputVolume.setOrientation(JSlider.VERTICAL);

        final JLabel outputIcon = new JLabel(JinglePhoneRes.getImageIcon("MICROPHONE_IMAGE"));
        outputPanel.add(outputVolume, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(2, 2, 2, 2), 0, 0));
        outputPanel.add(outputIcon, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        // Build ControlPanel List
        final ControlPanel controlPanel = new ControlPanel(new GridBagLayout());
        final JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setBackground(new Color(219, 228, 238));

        muteButton = new RosterMemberCallButton(JinglePhoneRes.getImageIcon("MUTE_IMAGE").getImage(), "Mute");
        muteButton.setToolTipText("Mute this call.");
        controlPanel.add(muteButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        controlPanel.add(sep, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        holdButton = new RosterMemberCallButton(JinglePhoneRes.getImageIcon("ON_HOLD_IMAGE").getImage(), "Hold");
        holdButton.setToolTipText("Place this call on hold.");
        controlPanel.add(holdButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));


        final JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
        sep2.setBackground(new Color(219, 228, 238));
        controlPanel.add(sep2, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        transferButton = new RosterMemberCallButton(JinglePhoneRes.getImageIcon("TRANSFER_IMAGE").getImage(), "Transfer");
        transferButton.setToolTipText("Transfer this call.");
        controlPanel.add(transferButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // Add Components to Main Panel
        mainPanel.add(inputPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.2, GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(2, 1, 2, 1), 0, 0));
        mainPanel.add(outputPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.2, GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(2, 1, 2, 1), 0, 0));
        mainPanel.add(controlPanel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 1, 2, 1), 0, 0));

        // Add End Call button
        hangUpButton = new RolloverButton("     End Call", JinglePhoneRes.getImageIcon("HANG_UP_PHONE_77x24_IMAGE"));
        hangUpButton.setHorizontalTextPosition(JLabel.CENTER);
        hangUpButton.setFont(new Font("Dialog", Font.BOLD, 11));
        hangUpButton.setForeground(new Color(153, 32, 10));
        hangUpButton.setMargin(new Insets(0, 0, 0, 0));
        mainPanel.add(hangUpButton, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.8, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));


        return mainPanel;
    }


    public void setupDefaults() {
        holdButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                toggleHold();
            }
        });

        muteButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                toggleMute();
            }

        });

        transferButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
            }
        });


        hangUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                hangUpButton.setEnabled(false);
                try {
                    session.terminate();
                }
                catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Called when a new call is established.
     */
    private void callStarted() {
        // Show History
        historyPanel.removeAll();
        historyPanel.addPreviousConversations(phoneNumber);

        hangUpButton.setEnabled(true);
        muteButton.setEnabled(true);
        holdButton.setEnabled(true);
        transferButton.setEnabled(true);
        setStatus(CONNECTED, false);

        // Add notification to ChatRoom if one exists.
        if (chatRoom != null) {
            final SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
            String time = formatter.format(new Date());

            chatRoom.getTranscriptWindow().insertNotificationMessage("Call started at " + time, ChatManager.NOTIFICATION_COLOR);
        }
    }

    /**
     * Called when the call is ended. This does basic container cleanup.
     */
    public void callEnded() {
        if (!callWasTransferred) {
            historyPanel.callEnded();
            setStatus("Call Ended", redColor);
        }

        hangUpButton.setEnabled(false);
        hangUpButton.setOpaque(false);

        muteButton.setEnabled(false);
        muteButton.setOpaque(false);

        holdButton.setEnabled(false);
        holdButton.setOpaque(false);

        transferButton.setEnabled(false);
        setStatus("Call Ended", redColor);

        // Add notification to ChatRoom if one exists.
        if (chatRoom != null) {
            final SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
            String time = formatter.format(new Date());

            chatRoom.getTranscriptWindow().insertNotificationMessage("Call ended at " + time, ChatManager.NOTIFICATION_COLOR);
        }

        // If this is a standalone phone call with no associated ChatRoom
        // gray out title and show off-phone icon.
        int index = SparkManager.getChatManager().getChatContainer().indexOfComponent(chatRoom);
        SparkTab tab = SparkManager.getChatManager().getChatContainer().getTabAt(index);
        if (chatRoom == null) {
            setTabIcon(JinglePhoneRes.getImageIcon("HANG_UP_PHONE_16x16_IMAGE"));
            if (tab != null) {
                tab.getTitleLabel().setForeground(Color.gray);
            }
        }
        else {
            if (tab != null) {
                tab.setTabDefaultAllowed(true);
                tab.setIcon(tab.getDefaultIcon());
            }
        }
    }

    private void setStatus(String status, boolean alert) {
        if (alert) {
            connectedLabel.setForeground(orangeColor);
        }
        else {
            connectedLabel.setForeground(greenColor);
        }
        connectedLabel.setText(status);
    }

    private void setStatus(String status, Color color) {
        connectedLabel.setForeground(color);
        connectedLabel.setText(status);
    }


    private void toggleMute() {
        if (onHold) {
            toggleHold();
        }

        if (muted) {
            muted = false;
            muteButton.setToolTipText("Mute");
            muteButton.setButtonSelected(false);
            setStatus(CONNECTED, false);
            useDefaultIconOnTab();
        }
        else {
            muted = true;
            muteButton.setToolTipText("Unmute");
            muteButton.setButtonSelected(true);
            setStatus("Muted", true);
            setTabIcon(JinglePhoneRes.getImageIcon("MUTE_IMAGE"));
        }

        muteButton.invalidate();
        muteButton.validate();
        muteButton.repaint();
    }


    private void toggleHold() {
        if (muted) {
            toggleMute();
        }

        if (onHold) {
            onHold = false;
            holdButton.setToolTipText("Hold");
            holdButton.setButtonSelected(false);
            setStatus(CONNECTED, false);
            useDefaultIconOnTab();
        }
        else {
            onHold = true;
            holdButton.setToolTipText("Unhold");
            holdButton.setButtonSelected(true);
            setStatus("On Hold", true);
            setTabIcon(JinglePhoneRes.getImageIcon("ON_HOLD_IMAGE"));
        }

    }

    public void actionPerformed(ActionEvent e) {

    }


    public void paintComponent(Graphics g) {
        BufferedImage cache = new BufferedImage(2, getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = cache.createGraphics();

        GradientPaint paint = new GradientPaint(0, 0, new Color(241, 245, 250), 0, getHeight(), new Color(244, 250, 255), true);

        g2d.setPaint(paint);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();

        g.drawImage(cache, 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * Changes the tab icon associated with this room.
     *
     * @param icon the icon to display.
     */
    private void setTabIcon(Icon icon) {
        SparkTab tab = getSparkTab();
        if (tab != null) {
            tab.setIcon(icon);
        }
    }

    /**
     * Use the default icon of the tab.
     */
    private void useDefaultIconOnTab() {
        SparkTab tab = getSparkTab();
        if (tab != null) {
            tab.setIcon(JinglePhoneRes.getImageIcon("RECEIVER2_IMAGE"));
        }
    }

    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.width = 0;
        return dim;
    }

    public SparkTab getSparkTab() {
        int index = SparkManager.getChatManager().getChatContainer().indexOfComponent(chatRoom);
        return SparkManager.getChatManager().getChatContainer().getTabAt(index);
    }


}