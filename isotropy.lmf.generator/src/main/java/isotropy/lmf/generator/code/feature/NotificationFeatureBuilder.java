package isotropy.lmf.generator.code.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.generator.util.GenUtils;

public class NotificationFeatureBuilder
{
	private final Group<?> group;

	public NotificationFeatureBuilder(Group<?> group)
	{
		this.group = group;
	}

	public EnumSpec buildSpec(FeatureResolution featureResolution)
	{
		final var feature = featureResolution.feature();
		final var parent = (Group<?>) feature.lmContainer();
		final var local = parent == group;
		final var name = GenUtils.toConstantCase(feature.name());

		if (local)
		{
			return new EnumSpec(name, null);
		}
		else
		{
			final var parentType = ClassName.get("", parent.name());
			final var constructorCall = TypeSpec.anonymousClassBuilder("$T.NotificationFeatures.$N", parentType, name)
												.build();
			return new EnumSpec(name, constructorCall);
		}
	}

	public record EnumSpec(String name, TypeSpec typeSpec)
	{
		public void insert(TypeSpec.Builder enumBuilder)
		{
			if (typeSpec != null) enumBuilder.addEnumConstant(name, typeSpec);
			else enumBuilder.addEnumConstant(name);
		}

		public boolean local()
		{
			return typeSpec == null;
		}
	}
}
