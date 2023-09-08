package isotropy.lmf.core.resource.transform;

import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainmentTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleEnumInModel()
	{
		final var textModel = "(Model name=Containment" +
							  "(Enum name=EColor literals=red,green,blue)" +
							  "(Group name=Toto))";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var model = (Model) roots.get(0);
		final var color = model.enums()
							   .get(0);
		final var group = model.groups()
							   .get(0);

		assertEquals(model, color.lmContainer());
		assertEquals(LMCoreDefinition.Features.MODEL.ENUMS, color.lmContainingFeature());

		assertEquals(model, group.lmContainer());
		assertEquals(LMCoreDefinition.Features.MODEL.GROUPS, group.lmContainingFeature());
	}
}
