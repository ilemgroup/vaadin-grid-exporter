/*
 * Copyright © 2022 XDEV Software (https://xdev.software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.xdev.vaadin.grid_exporter.grid;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.server.StreamResource;

import software.xdev.vaadin.grid_exporter.Translator;
import software.xdev.vaadin.grid_exporter.format.Format;
import software.xdev.vaadin.grid_exporter.format.FormatConfigComponent;
import software.xdev.vaadin.grid_exporter.format.GeneralConfig;
import software.xdev.vaadin.grid_exporter.format.SpecificConfig;
import software.xdev.vaadin.grid_exporter.grid.column.ColumnConfigurationComponent;


@CssImport(GridExporterStyles.LOCATION)
public class GridExportDialog<T> extends Dialog implements AfterNavigationObserver, Translator
{
	private Tab tabConfigure;
	private Tab tabPreview;
	private Tabs tabs;
	private ReportViewerComponent viewerComponent;
	private final GeneralConfig<T> configuration;
	private final GridExportLocalizationConfig localizationConfig;
	private final Grid<T> gridToExport;
	private Format<T, ?> selectedFormat;
	private FormatConfigComponent<?> selectedFormatConfigComponent;
	
	public static <T> GridExportDialog<T> open(final Grid<T> gridToExport)
	{
		final GridExportLocalizationConfig gridExportLocalizationConfig = new GridExportLocalizationConfig();
		return open(
			gridToExport,
			new GeneralConfig(
				gridToExport,
				(Translator)key -> gridExportLocalizationConfig.getTranslation(key, gridToExport)),
			gridExportLocalizationConfig);
	}
	
	public static <T> GridExportDialog<T> open(final Grid<T> gridToExport, final GeneralConfig<T> configuration)
	{
		return open(gridToExport, configuration);
	}
	
	public static <T> GridExportDialog<T> open(
		final Grid<T> gridToExport,
		final GridExportLocalizationConfig localizationConfig)
	{
		return open(gridToExport, new GeneralConfig<>(gridToExport), localizationConfig);
	}
	
	public static <T> GridExportDialog<T> open(
		final Grid<T> gridToExport,
		final GeneralConfig<T> configuration,
		final GridExportLocalizationConfig localizationConfig)
	{
		final GridExportDialog<T> dialog = new GridExportDialog<>(gridToExport, configuration, localizationConfig);
		dialog.open();
		return dialog;
	}
	
	public GridExportDialog(
		final Grid<T> gridToExport,
		final GeneralConfig<T> configuration,
		final GridExportLocalizationConfig localizationConfig)
	{
		super();
		this.gridToExport = gridToExport;
		this.configuration = configuration;
		this.localizationConfig = Objects.requireNonNull(localizationConfig);
		
		this.initUI();
		
	}
	
	@Override
	public String translate(final String key)
	{
		return this.localizationConfig.getTranslation(key, this);
	}
	
	private <E extends SpecificConfig> void export(
		final Format<T, E> format,
		final FormatConfigComponent<?> specificConfigComponent)
	{
		final byte[] exportedGridAsBytes =
			format.export(this.gridToExport, this.configuration, (E)specificConfigComponent.getConfig());
		
		final StreamResource resource = new StreamResource(
			this.configuration.getFileName() + "." + format.getFormatFilenameSuffix(),
			() -> new ByteArrayInputStream(exportedGridAsBytes));
		resource.setContentType(format.getMimeType());
		
		this.viewerComponent.setData(resource, format.getMimeType(), format.isPreviewableInStandardBrowser(), this);
		this.tabs.setSelectedTab(this.tabPreview);
	}
	
	@Override
	public void afterNavigation(final AfterNavigationEvent event)
	{
		// Workaround for https://github.com/vaadin/vaadin-dialog-flow/issues/108
		this.close();
	}
	
	private void initUI()
	{
		final ColumnConfigurationComponent<T> columnConfigurationComponent =
			new ColumnConfigurationComponent<>(this, this.configuration.getColumnConfigurations());
		columnConfigurationComponent.addClassName(GridExporterStyles.PRIMARY_FLEX_CHILD);
		columnConfigurationComponent.addClassName(GridExporterStyles.FLEX_WRAP_CONTAINER);
		columnConfigurationComponent.addClassName(GridExporterStyles.BAR);
		
		this.viewerComponent = new ReportViewerComponent();
		this.viewerComponent.addClassName(GridExporterStyles.MAIN_LAYOUT);
		final VerticalLayout gridcontent = new VerticalLayout();
		gridcontent.setSizeUndefined();
		gridcontent.addClassName(GridExporterStyles.MAIN_LAYOUT);
		
		// HEADER
		this.tabConfigure = new Tab(
			VaadinIcon.COG.create(),
			new Span(this.translate(GridExportLocalizationConfig.CONFIGURE_COLUMNS)));
		this.tabPreview =
			new Tab(VaadinIcon.VIEWPORT.create(), new Span(this.translate(GridExportLocalizationConfig.PREVIEW)));
		
		this.tabs = new Tabs(this.tabConfigure, this.tabPreview);
		this.tabs.addClassName(GridExporterStyles.BAR);
		this.tabs.addSelectedChangeListener(
			event ->
			{
				this.removeAll();
				if(event.getSelectedTab() == this.tabPreview)
				{
					this.add(this.viewerComponent);
				}
				else
				{
					this.add(gridcontent);
				}
			}
		);
		
		final Label lblTitle = new Label(this.translate(GridExportLocalizationConfig.EXPORT_CAPTION));
		lblTitle.addClassName(GridExporterStyles.PRIMARY_FLEX_CHILD);
		final Button btnClose = new Button(VaadinIcon.CLOSE.create());
		final Icon iconGrid = new Icon(VaadinIcon.TABLE);
		final HorizontalLayout titlebar = new HorizontalLayout();
		titlebar.addClassName(GridExporterStyles.BAR);
		titlebar.addClassName(GridExporterStyles.FLEX_WRAP_CONTAINER);
		titlebar.setFlexGrow(1.0, lblTitle);
		titlebar.add(iconGrid, lblTitle, btnClose);
		titlebar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		this.getHeader().add(new VerticalLayout(titlebar, this.tabs));
		
		// FOOTER
		final Button btnCancel = new Button(this.translate(GridExportLocalizationConfig.CANCEL));
		final Button btnExport = new Button(this.translate(GridExportLocalizationConfig.EXPORT));
		btnExport.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btnExport.addClassName(GridExporterStyles.BUTTON);
		btnCancel.addClassName(GridExporterStyles.BUTTON);
		btnExport.addClickListener(event -> this.export(this.selectedFormat, this.selectedFormatConfigComponent));
		btnCancel.addClickListener(event -> this.close());
		btnClose.addClickListener(event -> this.close());
		
		final HorizontalLayout buttonbar = new HorizontalLayout();
		buttonbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		buttonbar.add(btnCancel, btnExport);
		buttonbar.addClassName(GridExporterStyles.BAR);
		
		final Label lblStatus = new Label();
		lblStatus.addClassName(GridExporterStyles.BAR);
		lblStatus.addClassName(GridExporterStyles.STATUS);
		
		final HorizontalLayout bottomlayout = new HorizontalLayout();
		bottomlayout.setSpacing(false);
		bottomlayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		bottomlayout.add(lblStatus, buttonbar);
		bottomlayout.addClassName(GridExporterStyles.BAR);
		this.getFooter().add(bottomlayout);
		
		gridcontent.setPadding(false);
		final FlexLayout specificConfigurationLayout = new FlexLayout();
		specificConfigurationLayout.addClassName(GridExporterStyles.SPECIFIC_CONFIGURATION_CONTAINER);
		final ComboBox<Format<T, ?>> formatComboBox = new ComboBox<>();
		gridcontent.add(columnConfigurationComponent, formatComboBox, specificConfigurationLayout);
		gridcontent.addClassName(GridExporterStyles.BAR);
		
		formatComboBox.setItems(this.configuration.getAvailableFormats());
		formatComboBox.setItemLabelGenerator(item -> item.getFormatNameToDisplay());
		formatComboBox.setLabel(this.translate(GridExportLocalizationConfig.FORMAT));
		formatComboBox.addValueChangeListener(
			event -> {
				specificConfigurationLayout.removeAll();
				this.selectedFormat = event.getValue();
				this.selectedFormatConfigComponent = this.selectedFormat.createConfigurationComponent();
				this.selectedFormatConfigComponent.addClassName(GridExporterStyles.SPECIFIC_CONFIGURATION_CONTAINER);
				specificConfigurationLayout.add(this.selectedFormatConfigComponent);
			}
		);
		formatComboBox.setValue(this.configuration.getPreselectedFormat());
		
		this.addClassName(GridExporterStyles.DIALOG);
		// Simply call the "Selection Changed Listener"
		this.tabs.setSelectedTab(this.tabPreview);
		this.tabs.setSelectedTab(this.tabConfigure);
	}
}
