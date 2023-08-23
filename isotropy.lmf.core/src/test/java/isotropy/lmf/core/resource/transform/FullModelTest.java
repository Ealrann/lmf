package isotropy.lmf.core.resource.transform;

import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FullModelTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void loadFullModel()
	{
		final var inputStream = new ByteArrayInputStream(fullModelText.getBytes());
		final var ptree = treeBuilder.read(inputStream);
		final var ptreeToJava = new PTreeToJava();
		final var roots = ptreeToJava.transform(ptree);

		final var root = roots.get(0);
		assertTrue(root instanceof Model);

		final var model = (Model) root;
		assertEquals(model.domain(), "isotrpoy.lmf.core.lang");

		assertEquals(15,
					 model.groups()
						  .size());
		assertEquals(11,
					 model.aliases()
						  .size());
		assertEquals(8,
					 model.units()
						  .size());
		assertEquals(2,
					 model.enums()
						  .size());
	}

	private static final String fullModelText = """
			(Model name=LMCore domain=isotrpoy.lmf.core.lang
			   
				(Group LMObject)
				(Group Named (includes /groups.0) (-att name=name /units.7 mandatory))
				(Group Type  (includes /groups.1))
			   
				(Definition Model (includes group=/groups.1)
					(-contains groups  (reference group=/groups.4)  [0..*])
					(-contains enums   (reference group=/groups.10) [0..*])
					(-contains units   (reference group=/groups.11) [0..*])
					(-contains aliases (reference group=/groups.9)  [0..*]))
			   
				(Definition Group parameters=./generics.0
					(includes group=/groups.2 parameters=../generics.0)
					(includes group=/groups.14 parameters=../generics.0)
					(Generic T extends /groups.0)
					(-att      concrete       /units.2)
					(-contains includes       (reference /groups.13)  [0..*])
					(-contains features       (reference /groups.5)   [0..*])
					(-contains generics       (reference /groups.12)  [0..*])
					(-refers   parameters     (reference /groups.12)  [0..*]))
			   
				(Group Feature (includes /groups.1) (Generic UnaryType) (Generic EffectiveType)
					(-att immutable datatype=/units.2)
					(-att many      datatype=/units.2)
					(-att mandatory datatype=/units.2))
			   
				(Definition Attribute parameters=./generics.0,./generics.1
					(includes /groups.5 parameters=../generics.0,../generics.1)
					(Generic UnaryType) (Generic EffectiveType)
					(-refers datatype (reference /groups.8 parameters=/groups.6/generics.0) [1..1]))
						
				(Definition Relation parameters=./generics.0,./generics.1
					(includes /groups.5 parameters=../generics.0,../generics.1)
					(Generic UnaryType boundType=Extends type=/groups.0) (Generic EffectiveType)
					(-contains reference        (reference group=/groups.13) [1..1])
					(-att      lazy             datatype=/units.2)
					(-att      contains         datatype=/units.2))
			   
				(Group Datatype (includes /groups.2 parameters=../generics.0) (Generic T))
				(Definition Alias (includes /groups.1)
					(-att words /units.7 [0..*]))
				(Definition Enum  (includes /groups.8 parameters=../generics.0) (Generic T)
					(-att literals /units.7 [0..*]))
				(Definition Unit  (includes /groups.8 parameters=../generics.0) (Generic T)
					(-att matcher      /units.0   [0..1])
					(-att defaultValue /units.7   [0..1])
					(-att primitive    /enums.1   [1..1])
					(-att extractor    /units.1   [0..1]))
			   
				(Definition Generic (includes group=/groups.1)(includes /groups.14 parameters=../generics.0) parameters=./generics.0
				    (Generic T boundType=extends type=/groups.0)
					(-refers type      (reference group=/groups.2))
					(-att    boundType datatype=/enums.0))
			   
				(Definition Reference (includes group=/groups.0 parameters=../generics.0)
				    (Generic T)
			    	(-refers   group      [1..1] (reference /groups.14 parameters=../../generics.0))
			    	(-contains parameters [0..*] (reference /groups.14)))
			   
			    (Group Concept)
			   
				(Unit name=matcher   matcher="rgx_match:<(.+?)>")
			    (Unit name=extractor matcher="rgx_match:<(.+?)>")
				(Enum BoundType extends,super)
			   
				(Enum name=Primitive boolean,int,long,float,double,string)
			    (Unit name=boolean matcher="rgx_match:<(true|false)>" defaultValue=false primitive=boolean )
			    (Unit name=int     matcher="rgx_match:<[0-9]+>"       defaultValue=0     primitive=int     )
			    (Unit name=long    matcher="rgx_match:<[0-9]+[Ll]>"   defaultValue=0L    primitive=long    )
			    (Unit name=float   matcher="rgx_match:<[0-9.]+[Ff]>"  defaultValue=0f    primitive=float   )
			    (Unit name=double  matcher="rgx_match:<[0-9.]+>"      defaultValue=0.    primitive=double  )
			    (Unit name=string)
			   
				(Alias Definition words=Group,concrete)
				(Alias +contains  words=Relation,contains,immutable=false)
				(Alias -contains  words=Relation,contains,immutable)
				(Alias +refers    words=Relation,contains=false,immutable=false)
				(Alias -refers    words=Relation,contains=false,immutable)
				(Alias +att       words=Attribute,immutable=false)
				(Alias -att       words=Attribute,immutable)
				(Alias [0..1]     words=mandatory=false,many=false)
				(Alias [1..1]     words=mandatory,many=false)
				(Alias [0..*]     words=mandatory=false,many)
				(Alias [1..*]     words=mandatory,many)
			)
			""";
}
