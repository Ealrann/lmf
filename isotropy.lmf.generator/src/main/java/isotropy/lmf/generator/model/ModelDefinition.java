package isotropy.lmf.generator.model;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.code.model.*;
import isotropy.lmf.generator.code.util.FieldBuilder;
import isotropy.lmf.generator.code.util.InterfaceBuilder;
import isotropy.lmf.generator.code.util.SubInterfaceBuilder;
import isotropy.lmf.generator.util.ConstantTypes;
import isotropy.lmf.generator.util.GenUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ModelDefinition
{
	private final InterfaceBuilder<Model> javaWrapperBuilder = new FieldBuilder<>("JavaWrappers",
																				  new JavaWrapperFieldBuilder(),
																				  m -> m.javaWrappers().stream(),
																				  ConstantTypes.JAVA_WRAPPER_ALL_BUILDER);
	private final InterfaceBuilder<Model> aliasBuilder = new FieldBuilder<>("Aliases",
																			new AliasFieldBuilder(),
																			m -> m.aliases().stream(),
																			ConstantTypes.ALIAS_ALL_BUILDER);
	private final InterfaceBuilder<Model> enumBuilder = new FieldBuilder<>("Enums",
																		   new EnumFieldBuilder(),
																		   m -> m.enums().stream(),
																		   ConstantTypes.ENUM_ALL_BUILDER);
	private final InterfaceBuilder<Model> unitBuilder = new FieldBuilder<>("Units",
																		   new UnitFieldBuilder(),
																		   m -> m.units().stream(),
																		   ConstantTypes.UNIT_ALL_BUILDER);
	private final InterfaceBuilder<Model> groupBuilder = new FieldBuilder<>("Groups",
																			new GroupFieldBuilder(),
																			this::streamOrderedGroup,
																			ConstantTypes.GROUP_ALL_BUILDER);
	private final InterfaceBuilder<Model> genericBuilder = new FieldBuilder<>("Generics",
																			  new GenericFieldBuilder(),
																			  m -> this.streamOrderedGroup(m)
																					   .filter(g -> !g.generics()
																									  .isEmpty()),
																			  null);

	private final InterfaceBuilder<Group<?>> groupInterfaceBuilder = new FieldBuilder<>(g -> GenUtils.toConstantCase(g.name()),
																						FeaturesFieldBuilder::new,
																						ModelUtils::streamAllFeatures,
																						ConstantTypes.FEATURE_ALL_BUILDER);

	private final InterfaceBuilder<Model> featureInterfacesBuilder = new SubInterfaceBuilder<>("Features",
																							   groupInterfaceBuilder,
																							   this::streamOrderedGroup);

	private final Model model;

	private final GroupTopologyBuilder topology;

	public ModelDefinition(Model model)
	{
		this.model = model;
		this.topology = new GroupTopologyBuilder(model);
	}

	private Stream<Group<?>> streamOrderedGroup(Model model)
	{
		return topology.stream();
	}

	public void generate(final File target)
	{
		final var definitionInterface = TypeSpec.interfaceBuilder(model.name() + "Definition")
												.addModifiers(Modifier.PUBLIC);

		definitionInterface.addType(featureInterfacesBuilder.build(model));
		definitionInterface.addType(genericBuilder.build(model));
		definitionInterface.addType(groupBuilder.build(model));
		definitionInterface.addType(unitBuilder.build(model));
		definitionInterface.addType(enumBuilder.build(model));
		definitionInterface.addType(aliasBuilder.build(model));
		definitionInterface.addType(javaWrapperBuilder.build(model));

		final var javaFile = JavaFile.builder(model.domain(), definitionInterface.build()).build();
		try
		{
			javaFile.writeTo(target);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private final class GroupTopologyBuilder
	{
		private final List<Group<?>> topology;

		public GroupTopologyBuilder(Model model)
		{
			final Set<Group<?>> topology = new LinkedHashSet<>();
			model.groups().forEach(g -> push(g, topology));
			this.topology = topology.stream().toList();
		}

		public Stream<Group<?>> stream()
		{
			return topology.stream();
		}

		private void push(Group<?> group, Set<Group<?>> topology)
		{
			for (final var include : group.includes())
			{
				final var concept = include.group();

				if (concept instanceof Group<?> includedGroup && model.groups().contains(includedGroup))
				{
					push(includedGroup, topology);
				}
			}
			//noinspection RedundantCollectionOperation
			if (!topology.contains(group))
			{
				topology.add(group);
			}
		}
	}
}
