package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaintainerMultiModelTest
{
	private static final String MAINTAINER_MODEL = """
			(MetaModel domain=org.sheepy.lily.core.model name=Maintainer
				(Group Maintainable
					(Generic T
						(extension boundType=extends type=@Maintainable
							(parameters ../../../generics.0)))
					(+refers name=maintainer [0..1] @Maintainer
						(parameters ../../generics.0)))

				(Group Maintainer
					(Generic T
						(extension boundType=extends type=@Maintainable
							(parameters ../../../generics.0)))
					(+refers name=maintained [0..*] @Maintainable
						(parameters ../../generics.0)))
			)
			""";

	private static final String TYPES_MODEL = """
			(MetaModel domain=org.sheepy.lily.core.model name=Types
				(Group Dummy)
			)
			""";

	private static final String ACTION_MODEL = """
			(MetaModel domain=org.sheepy.lily.core.model name=Action imports=org.sheepy.lily.core.model.Types
				(JavaWrapper name=ActionList qualifiedClassName=java.util.List
					(Generic T
						(extension boundType=extends type=@Action)))

				(Group Action
					(includes group=#Types@LNamedElement))

				(Definition CloseApplicationAction
					(includes group=@Action))
			)
			""";

	@Test
	public void selfBoundGeneric_shouldResolveWithMultiModelLoading()
	{
		final var maintainerStream = toStream(MAINTAINER_MODEL);
		final var typesStream = toStream(TYPES_MODEL);
		final var actionStream = toStream(ACTION_MODEL);

		final var models = ResourceUtil.loadModels(List.of(maintainerStream, typesStream, actionStream),
												   ModelRegistry.empty());

		assertEquals(3, models.size());
		assertTrue(models.stream().allMatch(MetaModel.class::isInstance));

		final var maintainerModel = models.stream()
										  .filter(MetaModel.class::isInstance)
										  .map(MetaModel.class::cast)
										  .filter(m -> "Maintainer".equals(m.name()))
										  .findFirst()
										  .orElseThrow();

		final var maintainable = findGroup(maintainerModel, "Maintainable");
		final var genericT = maintainable.generics().getFirst();

		final var extension = genericT.extension();
		final var extensionParameter = extension.parameters().getFirst();
		assertSame(genericT, extensionParameter.type(), "Generic extension parameter should resolve to its own Generic");

		final var maintainerRelation = findRelation(maintainable, "maintainer");
		final var relationParameter = maintainerRelation.parameters().getFirst();
		assertSame(genericT,
				   relationParameter.type(),
				   "Relation parameter on 'maintainer' should resolve to the group's Generic T");
	}

	private static ByteArrayInputStream toStream(final String text)
	{
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}

	private static Group<?> findGroup(final MetaModel model, final String name)
	{
		return model.groups()
					.stream()
					.filter(g -> name.equals(g.name()))
					.findFirst()
					.orElseThrow();
	}

	private static Relation<?, ?> findRelation(final Group<?> group, final String name)
	{
		return group.features()
					.stream()
					.filter(Relation.class::isInstance)
					.map(Relation.class::cast)
					.filter(r -> name.equals(r.name()))
					.findFirst()
					.orElseThrow();
	}
}
