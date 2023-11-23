package org.logoce.lmf.model.resource.transform;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Primitive;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.resource.parsing.PTreeReader;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ExtractMethodRecommender")
public class UnitTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void simpleUnit()
	{
		final var textModel = "(Unit name=boolean " +
							  "      matcher=\"rgx_match:<(true|false)>\" " +
							  "      defaultValue=false " +
							  "      primitive=boolean )";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelBuilder<>();
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Unit<?>);

		final var unit = (Unit<?>) root;
		assertEquals("boolean", unit.name());
		assertEquals(Primitive.Boolean, unit.primitive());
		assertEquals("false", unit.defaultValue());
		assertEquals("rgx_match:<(true|false)>", unit.matcher());
		assertNull(unit.extractor());
	}

	@Test
	public void matcherOnly()
	{
		final var textModel = "(Unit matcher=\"rgx_match:<(true|false)>\") ";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelBuilder<>();
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Unit<?>);

		final var unit = (Unit<?>) root;
		assertEquals("rgx_match:<(true|false)>", unit.matcher());
	}

	@Test
	public void allBaseUnits()
	{
		final var textModel = """
				 (Model Test
					(Unit name=matcher   matcher="rgx_match:<(.+?)>")
					(Unit name=extractor matcher="rgx_match:<(.+?)>")
					(Enum name=BoundType extends,super)
				    
					(Enum name=Primitive boolean,int,long,float,double,string)
					(Unit name=boolean matcher="rgx_match:<(true|false)>" defaultValue=false primitive=boolean )
					(Unit name=int     matcher="rgx_match:<[0-9]+>"       defaultValue=0     primitive=int     )
					(Unit name=long    matcher="rgx_match:<[0-9]+[Ll]>"   defaultValue=0L    primitive=long    )
					(Unit name=float   matcher="rgx_match:<[0-9.]+[Ff]>"  defaultValue=0f    primitive=float   )
					(Unit name=double  matcher="rgx_match:<[0-9.]+>"      defaultValue=0.    primitive=double  )
					(Unit name=string) )
				""";
		final var inputStream = new ByteArrayInputStream(textModel.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PModelBuilder<>();
		final var roots = ptreeToJava.build(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);

		final var model = (Model) root;
		assertEquals(2,
					 model.enums()
						  .size());
		assertEquals(8,
					 model.units()
						  .size());

		final var unitMatcher = model.units()
									 .get(0);
		assertEquals("matcher", unitMatcher.name());
		assertEquals(Primitive.String, unitMatcher.primitive());
		assertNull(unitMatcher.defaultValue());
		assertEquals("rgx_match:<(.+?)>", unitMatcher.matcher());
		assertNull(unitMatcher.extractor());

		final var unitExtractor = model.units()
									   .get(1);
		assertEquals("extractor", unitExtractor.name());
		assertEquals(Primitive.String, unitExtractor.primitive());
		assertNull(unitExtractor.defaultValue());
		assertEquals("rgx_match:<(.+?)>", unitExtractor.matcher());
		assertNull(unitExtractor.extractor());

		final var booleanUnit = model.units()
									 .get(2);
		assertEquals("boolean", booleanUnit.name());
		assertEquals(Primitive.Boolean, booleanUnit.primitive());
		assertEquals("false", booleanUnit.defaultValue());
		assertEquals("rgx_match:<(true|false)>", booleanUnit.matcher());
		assertNull(booleanUnit.extractor());

		final var intUnit = model.units()
								 .get(3);
		assertEquals("int", intUnit.name());
		assertEquals(Primitive.Int, intUnit.primitive());
		assertEquals("0", intUnit.defaultValue());
		assertEquals("rgx_match:<[0-9]+>", intUnit.matcher());
		assertNull(intUnit.extractor());

		final var longUnit = model.units()
								  .get(4);
		assertEquals("long", longUnit.name());
		assertEquals(Primitive.Long, longUnit.primitive());
		assertEquals("0L", longUnit.defaultValue());
		assertEquals("rgx_match:<[0-9]+[Ll]>", longUnit.matcher());
		assertNull(longUnit.extractor());

		final var floatUnit = model.units()
								   .get(5);
		assertEquals("float", floatUnit.name());
		assertEquals(Primitive.Float, floatUnit.primitive());
		assertEquals("0f", floatUnit.defaultValue());
		assertEquals("rgx_match:<[0-9.]+[Ff]>", floatUnit.matcher());
		assertNull(floatUnit.extractor());

		final var doubleUnit = model.units()
									.get(6);
		assertEquals("double", doubleUnit.name());
		assertEquals(Primitive.Double, doubleUnit.primitive());
		assertEquals("0.", doubleUnit.defaultValue());
		assertEquals("rgx_match:<[0-9.]+>", doubleUnit.matcher());
		assertNull(doubleUnit.extractor());

		final var stringUnit = model.units()
									.get(7);
		assertEquals("string", stringUnit.name());
		assertEquals(Primitive.String, stringUnit.primitive());
		assertNull(stringUnit.defaultValue());
		assertNull(stringUnit.matcher());
		assertNull(stringUnit.extractor());
	}
}
