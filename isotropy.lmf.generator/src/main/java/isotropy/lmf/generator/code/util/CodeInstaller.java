package isotropy.lmf.generator.code.util;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface CodeInstaller<Input>
{
	void install(Input input);

	static <Input> CodeInstaller<Input> compose(CodeInstaller<Input>... installers)
	{
		return new ComposedCodeInstallers<>(List.of(installers));
	}

	static <Input, Built> CodeInstaller<Input> of(CodeBuilder<Input, Built> codeBuilder,
												  BiConsumer<Built, Input> installer,
												  Predicate<Input> acceptFeature)
	{
		return new SingleCodeInstaller<>(codeBuilder, installer, acceptFeature);
	}

	static <Input, Built> CodeInstaller<Input> of(CodeBuilder<Input, Built> codeBuilder,
												  Consumer<Built> installer,
												  Predicate<Input> acceptFeature)
	{
		return new SingleCodeInstaller<>(codeBuilder, (t, f) -> installer.accept(t), acceptFeature);
	}

	static <Input, Built> CodeInstaller<Input> of(CodeBuilder<Input, Built> codeBuilder, Consumer<Built> installer)
	{
		return new SingleCodeInstaller<>(codeBuilder, (t, f) -> installer.accept(t), f -> true);
	}

	final class SingleCodeInstaller<Input, Built> implements CodeInstaller<Input>
	{
		private final CodeBuilder<Input, Built> codeBuilder;
		private final BiConsumer<Built, Input> installer;
		private final Predicate<Input> accpetFeature;

		public SingleCodeInstaller(CodeBuilder<Input, Built> codeBuilder,
								   BiConsumer<Built, Input> installer,
								   Predicate<Input> acceptFeature)
		{
			this.codeBuilder = codeBuilder;
			this.installer = installer;
			this.accpetFeature = acceptFeature;
		}

		@Override
		public void install(Input input)
		{
			if (accpetFeature.test(input))
			{
				final var builtCode = codeBuilder.build(input);
				installer.accept(builtCode, input);
			}
		}
	}

	final class ComposedCodeInstallers<Input> implements CodeInstaller<Input>
	{
		private final List<CodeInstaller<Input>> installers;

		public ComposedCodeInstallers(final List<CodeInstaller<Input>> installers) {this.installers = installers;}

		@Override
		public void install(Input input)
		{
			installers.forEach(f -> f.install(input));
		}
	}
}
