package com.adamk33n3r.runelite.watchdog.hub;

import com.adamk33n3r.runelite.watchdog.ui.PlaceholderTextField;
import com.adamk33n3r.runelite.watchdog.ui.StretchedStackedLayout;
import com.adamk33n3r.runelite.watchdog.ui.panels.PanelUtils;
import com.adamk33n3r.runelite.watchdog.ui.panels.ScrollablePanel;

import com.google.common.base.Splitter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.MultiplexingPluginPanel;
import net.runelite.client.ui.PluginPanel;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adamk33n3r.runelite.watchdog.ui.panels.AlertPanel.BACK_ICON;
import static com.adamk33n3r.runelite.watchdog.ui.panels.AlertPanel.BACK_ICON_HOVER;

@Slf4j
//@Singleton
public class AlertHubPanel extends PluginPanel {
    private final Provider<MultiplexingPluginPanel> muxer;
    private final AlertHubClient alertHubClient;
//    private final ScrollablePanel filteredAlerts;
    private List<AlertHubItem> alertHubItems = new ArrayList<>();
    private final IconTextField searchBar;
    private final JPanel container;
    private static final Splitter SPLITTER = Splitter.on(" ").trimResults().omitEmptyStrings();

    @Inject
    public AlertHubPanel(Provider<MultiplexingPluginPanel> muxer, AlertHubClient alertHubClient) {
        super(false);
        this.muxer = muxer;
        this.alertHubClient = alertHubClient;

//        this.setLayout(new BorderLayout());
//
//        JPanel topPanel = new JPanel(new BorderLayout());
//        topPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        JButton backButton = PanelUtils.createActionButton(
            BACK_ICON,
            BACK_ICON_HOVER,
            "Back",
            (btn, modifiers) -> this.muxer.get().popState()
        );
        backButton.setPreferredSize(new Dimension(22, 16));
        backButton.setBorder(new EmptyBorder(0, 0, 0, 5));
//        topPanel.add(backButton, BorderLayout.WEST);
//        PlaceholderTextField filterTextField = new PlaceholderTextField();
//        filterTextField.setPlaceholder("Filter");
//        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                updateFilter(filterTextField.getText());
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                updateFilter(filterTextField.getText());
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                updateFilter(filterTextField.getText());
//            }
//        });
//        topPanel.add(filterTextField);
//        this.add(topPanel, BorderLayout.NORTH);
//
//        this.filteredAlerts = new ScrollablePanel(new StretchedStackedLayout(3, 3));
//        this.filteredAlerts.setBorder(new EmptyBorder(0, 10, 0, 10));
//        this.filteredAlerts.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
//        this.filteredAlerts.setScrollableHeight(ScrollablePanel.ScrollableSizeHint.STRETCH);
//        this.filteredAlerts.setScrollableBlockIncrement(ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 10);
//        JScrollPane scroll = new JScrollPane(this.filteredAlerts, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//
//        this.add(scroll, BorderLayout.CENTER);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        this.setBackground(ColorScheme.PROGRESS_ERROR_COLOR);
        this.searchBar = new IconTextField();
        this.searchBar.setIcon(IconTextField.Icon.SEARCH);
        this.searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        this.searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter(searchBar.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter(searchBar.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter(searchBar.getText());
            }
        });

        this.container = new JPanel(new DynamicGridLayout(0, 1, 0, 5));
//        this.container.setMaximumSize(new Dimension(PANEL_WIDTH, 9999));
        this.container.setBackground(ColorScheme.GRAND_EXCHANGE_LIMIT);
        this.container.setBorder(BorderFactory.createEmptyBorder(0, 7, 15, 7));
//        this.container.setAlignmentX(Component.LEFT_ALIGNMENT);

//        JPanel wrapper = new JPanel(new BorderLayout());
//        wrapper.add(this.container, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane(this.container, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(ColorScheme.GRAND_EXCHANGE_ALCH);
        scrollPane.setMaximumSize(new Dimension(PANEL_WIDTH + SCROLLBAR_WIDTH, 9999));

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGap(5)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(backButton, 24, 24, 24)
                .addComponent(searchBar, 24, 24, 24))
            .addGap(10)
            .addComponent(scrollPane)
        );

        layout.setHorizontalGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addGap(7)
                .addComponent(backButton)
                .addGap(3)
                .addComponent(searchBar)
                .addGap(7))
            .addComponent(scrollPane)
        );

//        this.revalidate();

        reloadList();
    }

    public void reloadList() {
        this.container.removeAll();

        try {
            List<AlertManifest> alertManifests = this.alertHubClient.downloadManifest();
            System.out.println(alertManifests.stream().map(AlertManifest::toString).collect(Collectors.joining(", ")));
            this.reloadList(alertManifests);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reloadList(List<AlertManifest> alertManifests) {
        this.alertHubItems = alertManifests.stream().map(AlertHubItem::new).collect(Collectors.toList());
        this.updateFilter(this.searchBar.getText());
    }

    private void updateFilter(String search) {
        this.container.removeAll();
        String upperSearch = search.toUpperCase();
        this.alertHubItems.stream().filter(alertHubItem -> {
//            Alert alert = WatchdogPlugin.getInstance().getAlertManager().getGson().fromJson(alertHubItem.getManifest().getJson(), ALERT_LIST_TYPE);
            AlertManifest manifest = alertHubItem.getManifest();
            return Text.matchesSearchTerms(SPLITTER.split(upperSearch), manifest.getKeywords());
        }).forEach(this.container::add);
        this.revalidate();
        // Idk why I need to repaint sometimes and the PluginListPanel doesn't
        this.repaint();
    }
}