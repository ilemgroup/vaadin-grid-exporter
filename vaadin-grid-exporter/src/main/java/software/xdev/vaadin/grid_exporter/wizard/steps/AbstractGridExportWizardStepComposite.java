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
package software.xdev.vaadin.grid_exporter.wizard.steps;

import java.util.Objects;

import com.vaadin.flow.component.Component;

import software.xdev.vaadin.grid_exporter.Translator;
import software.xdev.vaadin.grid_exporter.components.wizard.GridExporterWizardState;
import software.xdev.vaadin.grid_exporter.components.wizard.step.WizardStepComposite;


public abstract class AbstractGridExportWizardStepComposite<C extends Component, T>
	extends WizardStepComposite<C, GridExporterWizardState<T>>
	implements Translator
{
	protected Translator translator;
	
	protected AbstractGridExportWizardStepComposite(final Translator translator)
	{
		this.translator = Objects.requireNonNull(translator);
	}
	
	@Override
	public String translate(final String key)
	{
		return this.translator.translate(key);
	}
}
