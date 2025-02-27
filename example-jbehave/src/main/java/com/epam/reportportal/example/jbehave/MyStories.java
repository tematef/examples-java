package com.epam.reportportal.example.jbehave;

import com.epam.reportportal.example.jbehave.steps.*;
import com.epam.reportportal.jbehave.ReportPortalStepFormat;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;

/**
 * <p>
 * {@link Embeddable} class to run multiple textual stories via JUnit.
 * </p>
 * <p>
 * Stories are specified in classpath and correspondingly the {@link LoadFromClasspath} story loader is configured.
 * </p>
 */
public class MyStories extends JUnitStories {

	public MyStories() {
		configuredEmbedder().embedderControls()
				.doGenerateViewAfterStories(true)
				.doIgnoreFailureInStories(true)
				.doIgnoreFailureInView(true)
				.useThreads(1)
				.useStoryTimeouts("60");
	}

	@Override
	public Configuration configuration() {
		Class<? extends Embeddable> embeddableClass = this.getClass();
		// Start from default ParameterConverters instance
		ParameterConverters parameterConverters = new ParameterConverters();

		TableTransformers tableTransformers = new TableTransformers();
		TableParsers tableParsers = new TableParsers();
		// factory to allow parameter conversion and loading from external resources (used by StoryParser too)
		ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(),
				new LoadFromClasspath(embeddableClass),
				tableParsers,
				tableTransformers
		);
		// add custom converters
		parameterConverters.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
				new ExamplesTableConverter(examplesTableFactory)
		);
		return new MostUsefulConfiguration().useStoryLoader(new LoadFromClasspath(embeddableClass))
				.useStoryParser(new RegexStoryParser(examplesTableFactory))
				.useStoryReporterBuilder(new StoryReporterBuilder().withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
						.withFormats(CONSOLE, HTML, ReportPortalStepFormat.INSTANCE))
				.useParameterConverters(parameterConverters);
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(),
				new LogLevelTest(),
				new ReportAttachmentsTest(),
				new ReportsStepWithDefectTest(),
				new ReportsTestWithParameters(),
				new ApiSteps()
		);
	}

	@Override
	protected List<String> storyPaths() {
		String storyPatternToRun = ofNullable(System.getProperty("story")).filter(s -> !s.isEmpty())
				.map(s -> "**/" + s)
				.orElse("**/*.story");
		List<URL> resourceFolders = new ArrayList<>();
		ofNullable(getClass().getClassLoader().getResource(PropertiesLoader.INNER_PATH)).map(p -> {
			String filePath = CodeLocations.getPathFromURL(p);
			String rootPath = StringUtils.removeEnd(filePath, "/" + PropertiesLoader.INNER_PATH);
			return CodeLocations.codeLocationFromPath(rootPath);
		}).ifPresent(resourceFolders::add);
		resourceFolders.add(codeLocationFromClass(this.getClass()));

		return resourceFolders.stream()
				.flatMap(u -> new StoryFinder().findPaths(u, storyPatternToRun, "**/excluded*.story").stream())
				.distinct()
				.collect(Collectors.toList());
	}
}
