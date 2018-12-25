/*
 * Copyright (C) 2015-2017 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.phoenicis.javafx.views.mainwindow.apps;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.lang.StringUtils;
import org.phoenicis.javafx.collections.ExpandedList;
import org.phoenicis.javafx.collections.MappedList;
import org.phoenicis.javafx.components.common.widgets.control.CombinedListWidget;
import org.phoenicis.javafx.settings.JavaFxSettingsManager;
import org.phoenicis.javafx.views.common.ThemeManager;
import org.phoenicis.javafx.components.common.widgets.utils.ListWidgetElement;
import org.phoenicis.javafx.views.mainwindow.ui.MainWindowView;
import org.phoenicis.repository.dto.ApplicationDTO;
import org.phoenicis.repository.dto.CategoryDTO;
import org.phoenicis.repository.dto.ScriptDTO;
import org.phoenicis.tools.ToolsConfiguration;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.phoenicis.configuration.localisation.Localisation.tr;

/**
 * This view contains the available applications list.
 * This component is made up of up to three components:
 * <p>
 * <ul>
 * <li>A sidebar containing the categories of the applications and tools to filter the applications</li>
 * <li>A list widget showing the available applications to the user</li>
 * <li>An optional details view that contains the information and available scripts for a selected application</li>
 * </ul>
 */
public class ApplicationsView extends MainWindowView<ApplicationsSidebar> {
    private final ApplicationFilter filter;
    private final JavaFxSettingsManager javaFxSettingsManager;

    private final ObservableList<CategoryDTO> categories;

    private final CombinedListWidget<ApplicationDTO> availableApps;

    private Consumer<ScriptDTO> onSelectScript;

    /**
     * Constructor
     *
     * @param themeManager The theme manager
     * @param javaFxSettingsManager The javafx settings manager
     * @param toolsConfiguration The tools configuration
     */
    public ApplicationsView(ThemeManager themeManager, JavaFxSettingsManager javaFxSettingsManager,
            ToolsConfiguration toolsConfiguration) {
        super(tr("Apps"), themeManager);

        this.javaFxSettingsManager = javaFxSettingsManager;

        this.categories = FXCollections.observableArrayList();

        this.filter = new ApplicationFilter(toolsConfiguration.operatingSystemFetcher(),
                (filterText, application) -> {
                    if (StringUtils.isNotEmpty(filterText)) {
                        return FuzzySearch.partialRatio(application.getName().toLowerCase(),
                                filterText) > this.javaFxSettingsManager.getFuzzySearchRatio();
                    } else {
                        return true;
                    }
                });

        this.filter.filterCategoryProperty().addListener(invalidation -> closeDetailsView());

        this.availableApps = createApplicationListWidget();

        setSidebar(createApplicationsSidebar(this.availableApps));
    }

    private CombinedListWidget<ApplicationDTO> createApplicationListWidget() {
        /*
         * initialising the application lists by:
         * 1. sorting the applications by their name
         * 2. filtering them
         */
        final FilteredList<ApplicationDTO> filteredApplications = new ExpandedList<>(
                this.categories.filtered(category -> category.getType() == CategoryDTO.CategoryType.INSTALLERS),
                CategoryDTO::getApplications)
                        .sorted(Comparator.comparing(ApplicationDTO::getName))
                        .filtered(this.filter::filter);

        filteredApplications.predicateProperty().bind(
                Bindings.createObjectBinding(() -> this.filter::filter,
                        this.filter.filterTextProperty(), this.filter.filterCategoryProperty(),
                        this.filter.containAllOSCompatibleApplicationsProperty(),
                        this.filter.containCommercialApplicationsProperty(),
                        this.filter.containRequiresPatchApplicationsProperty(),
                        this.filter.containTestingApplicationsProperty()));

        final ObservableList<ListWidgetElement<ApplicationDTO>> listWidgetEntries = new MappedList<>(
                filteredApplications,
                ListWidgetElement::create);

        final CombinedListWidget<ApplicationDTO> listWidget = new CombinedListWidget<>(listWidgetEntries);

        listWidget.selectedElementProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showAppDetails(newValue.getItem(), this.javaFxSettingsManager);
            }
        });

        return listWidget;
    }

    private ApplicationsSidebar createApplicationsSidebar(CombinedListWidget<ApplicationDTO> availableApps) {
        /*
         * initialize the category lists by:
         * 1. filtering by installer categories
         * 2. sorting the remaining categories by their name
         */
        final SortedList<CategoryDTO> sortedCategories = this.categories
                .filtered(category -> category.getType() == CategoryDTO.CategoryType.INSTALLERS)
                .sorted(Comparator.comparing(CategoryDTO::getName));

        return new ApplicationsSidebar(this.filter, this.javaFxSettingsManager, sortedCategories,
                availableApps);
    }

    /**
     * Populate with a list of categories containing apps, and then scripts
     *
     * @param categories CategoryDTO
     */
    public void populate(List<CategoryDTO> categories) {
        List<CategoryDTO> filteredCategories = categories.stream()
                .filter(category -> !category.getApplications().isEmpty()).collect(Collectors.toList());

        Platform.runLater(() -> {
            this.categories.setAll(filteredCategories);

            setCenter(this.availableApps);
            closeDetailsView();
        });
    }

    /**
     * Sets the callback, which is called when a script has been selected
     *
     * @param onSelectScript The callback, which is called when a script has been selected
     */
    public void setOnSelectScript(Consumer<ScriptDTO> onSelectScript) {
        this.onSelectScript = onSelectScript;
    }

    public void setOnRetryButtonClicked(EventHandler<? super MouseEvent> event) {
        getFailurePanel().getRetryButton().setOnMouseClicked(event);
    }

    /**
     * Displays the application details view on the right side for a given application.
     *
     * @param application The application, whose details should be shown
     * @param javaFxSettingsManager The javafx settings manager
     */
    private void showAppDetails(ApplicationDTO application, JavaFxSettingsManager javaFxSettingsManager) {
        final ApplicationPanel applicationPanel = new ApplicationPanel(application, this.filter, this.themeManager,
                javaFxSettingsManager);

        applicationPanel.setOnScriptInstall(this::installScript);
        applicationPanel.setOnClose(this::closeDetailsView);
        applicationPanel.prefWidthProperty().bind(this.getTabPane().widthProperty().divide(3));

        showDetailsView(applicationPanel);
    }

    /**
     * Starts the installation process for a given script
     *
     * @param scriptDTO The script to be installed
     */
    private void installScript(ScriptDTO scriptDTO) {
        this.onSelectScript.accept(scriptDTO);
    }
}
