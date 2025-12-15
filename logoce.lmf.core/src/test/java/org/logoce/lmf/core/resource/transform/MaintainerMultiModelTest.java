package org.logoce.lmf.core.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.LmLoader;
import org.logoce.lmf.core.api.model.ModelRegistry;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
				(Definition ActionList
			         (Generic T (extension boundType=extends type=@Action))
			         (-att list [1..1] @JavaList
			             (parameters ../../generics.0)))

			    (JavaWrapper name=JavaList qualifiedClassName=java.util.List)

				(Group Action
					(includes group=#Types@Dummy))

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

		final var loader = new LmLoader(ModelRegistry.empty());
		final var models = assertDoesNotThrow(
			() -> loader.loadModels(List.of(maintainerStream, typesStream, actionStream)));

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

	private static Relation<?, ?, ?, ?> findRelation(final Group<?> group, final String name)
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
