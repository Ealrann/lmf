package isotropy.lmf.generator.group.feature;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class FeatureInstaller<T>
{
	private final CodeBuilder<T> codeBuilder;
	private final BiConsumer<T, FeatureResolution> installer;
	private final Predicate<FeatureResolution> accpetFeature;

	public FeatureInstaller(CodeBuilder<T> codeBuilder,
							BiConsumer<T, FeatureResolution> installer,
							Predicate<FeatureResolution> acceptFeature)
	{
		this.codeBuilder = codeBuilder;
		this.installer = installer;
		this.accpetFeature = acceptFeature;
	}

	public FeatureInstaller(CodeBuilder<T> codeBuilder,
							Consumer<T> installer,
							Predicate<FeatureResolution> acceptFeature)
	{
		this(codeBuilder, (t, f) -> installer.accept(t), acceptFeature);
	}

	public FeatureInstaller(CodeBuilder<T> codeBuilder, Consumer<T> installer)
	{
		this(codeBuilder, installer, f -> true);
	}

	public void install(FeatureResolution resolution)
	{
		if (accpetFeature.test(resolution))
		{
			final var builtCode = codeBuilder.build(resolution);
			installer.accept(builtCode, resolution);
		}
	}
}
