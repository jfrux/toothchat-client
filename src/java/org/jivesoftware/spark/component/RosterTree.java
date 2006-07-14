/**
 * $Revision: $
 * $Date: $
 *
 * Copyright (C) 2006 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package org.jivesoftware.spark.component;

import org.jivesoftware.resource.SparkRes;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.spark.SparkManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class RosterTree extends JPanel {
    private final JiveTreeNode rootNode = new JiveTreeNode("Contact List");
    private final Tree rosterTree;
    private final Map addressMap = new HashMap();
    private boolean showUnavailableAgents = true;

    /**
     * Creates a new Roster Tree.
     */
    public RosterTree() {
        rootNode.setAllowsChildren(true);
        rosterTree = new Tree(rootNode);
        rosterTree.setCellRenderer(new JiveTreeCellRenderer());
        buildFromRoster();
        setLayout(new BorderLayout());

        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.white);

        final JScrollPane treeScroller = new JScrollPane(rosterTree);
        treeScroller.setBorder(BorderFactory.createEmptyBorder());
        panel.add(treeScroller, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        add(panel, BorderLayout.CENTER);
        for (int i = 0; i < rosterTree.getRowCount(); i++) {
            rosterTree.expandRow(i);
        }
    }

    private void changePresence(String user, boolean available) {
        final Iterator iter = addressMap.keySet().iterator();
        while (iter.hasNext()) {
            final JiveTreeNode node = (JiveTreeNode)iter.next();
            final String nodeUser = (String)addressMap.get(node);
            if (user.startsWith(nodeUser)) {
                if (!available) {
                    node.setIcon(SparkRes.getImageIcon(SparkRes.CLEAR_BALL_ICON));
                }
                else {
                    node.setIcon(SparkRes.getImageIcon(SparkRes.GREEN_BALL));
                }
            }
        }
    }

    private void buildFromRoster() {
        final XMPPConnection xmppCon = SparkManager.getConnection();
        final Roster roster = xmppCon.getRoster();

        roster.addRosterListener(new RosterListener() {
            public void entriesAdded(Collection addresses) {

            }

            public void entriesUpdated(Collection addresses) {

            }

            public void entriesDeleted(Collection addresses) {

            }

            public void presenceChanged(String user) {
                Presence presence = roster.getPresence(user);
                changePresence(user, presence != null && presence.getMode() == Presence.Mode.AVAILABLE);

            }
        });


        final Iterator iter = roster.getGroups();
        while (iter.hasNext()) {
            final RosterGroup group = (RosterGroup)iter.next();


            final JiveTreeNode groupNode = new JiveTreeNode(group.getName(), true);
            groupNode.setAllowsChildren(true);
            if (group.getEntryCount() > 0) {
                rootNode.add(groupNode);
            }

            Iterator entries = group.getEntries();
            while (entries.hasNext()) {
                final RosterEntry entry = (RosterEntry)entries.next();
                String name = entry.getName();
                if (name == null) {
                    name = entry.getUser();
                }


                final JiveTreeNode entryNode = new JiveTreeNode(name, false);
                final Presence p = roster.getPresence(entry.getUser());
                addressMap.put(entryNode, entry.getUser());
                if (p != null && p.getType() == Presence.Type.AVAILABLE && p.getMode() == Presence.Mode.AVAILABLE) {
                    groupNode.add(entryNode);
                }
                else if ((p == null || p.getType() == Presence.Type.UNAVAILABLE) && showUnavailableAgents) {
                    groupNode.add(entryNode);
                }

                changePresence(entry.getUser(), p != null);
                final DefaultTreeModel model = (DefaultTreeModel)rosterTree.getModel();
                model.nodeStructureChanged(groupNode);
            }
        }
    }

    /**
     * Returns the Tree representation of the Roster Tree.
     *
     * @return the tree representation of the Roster Tree.
     */
    public Tree getRosterTree() {
        return rosterTree;
    }

    /**
     * Returns the selected agent node userobject.
     *
     * @param node the JiveTreeNode.
     * @return the selected agent nodes userobject.
     */
    public String getJID(JiveTreeNode node) {
        return (String)addressMap.get(node);
    }

    public String toString() {
        return "Roster";
    }
}
