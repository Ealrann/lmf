package org.logoce.lmf.generator.model;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.code.model.*;
import org.logoce.lmf.generator.code.util.FieldBuilder;
import org.logoce.lmf.generator.code.util.InterfaceBuilder;
import org.logoce.lmf.generator.code.util.SubInterfaceBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.FormattedJavaWriter;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ModelDefinition
{
	private final InterfaceBuilder<MetaModel> javaWrapperBuilder = new FieldBuilder<>("JavaWrappers",
																					  new JavaWrapperFieldBuilder(),
																				  m -> m.javaWrappers().stream(),
																					  ConstantTypes.JAVA_WRAPPER_ALL_BUILDER);
	private final InterfaceBuilder<MetaModel> aliasBuilder = new FieldBuilder<>("Aliases",
																				new AliasFieldBuilder(),
																			m -> m.aliases().stream(),
																				ConstantTypes.ALIAS_ALL_BUILDER);
	private final InterfaceBuilder<MetaModel> enumBuilder = new FieldBuilder<>("Enums",
																			   new EnumFieldBuilder(),
																		   m -> m.enums().stream(),
																			   ConstantTypes.ENUM_ALL_BUILDER);
	private final InterfaceBuilder<MetaModel> unitBuilder = new FieldBuilder<>("Units",
																			   new UnitFieldBuilder(),
																		   m -> m.units().stream(),
																			   ConstantTypes.UNIT_ALL_BUILDER);
	private final InterfaceBuilder<MetaModel> groupBuilder = new FieldBuilder<>("Groups",
																				new GroupFieldBuilder(),
																				this::streamOrderedGroup,
																				ConstantTypes.GROUP_ALL_BUILDER);
	private final InterfaceBuilder<Group<?>> genericInterfaceBuilder = new FieldBuilder<>(g -> GenUtils.toConstantCase(
																										g.name()),
																						 g -> new GenericFieldBuilder(),
																						 g -> g.generics().stream(),
																						 ConstantTypes.GENERIC_ALL_BUILDER);
	private final InterfaceBuilder<MetaModel> genericBuilder = new SubInterfaceBuilder<>("Generics",
																						genericInterfaceBuilder,
																						m -> this.streamOrderedGroup(m)
																							 .filter(g -> !g.generics()
																											.isEmpty()));
	private final InterfaceBuilder<Group<?>> groupInterfaceBuilder = new FieldBuilder<>(g -> GenUtils.toConstantCase(g.name()),
																						FeaturesFieldBuilder::new,
																						FeatureStreams::distinctFeatures,
																						ConstantTypes.FEATURE_ALL_BUILDER);

	private final InterfaceBuilder<MetaModel> featureInterfacesBuilder = new SubInterfaceBuilder<>("Features",
																								   groupInterfaceBuilder,
																								   this::streamOrderedGroup);

	private final MetaModel model;

	private final GroupTopologyBuilder topology;

	private final InterfaceBuilder<Group<?>> groupFeatureIdInterfaceBuilder = new FieldBuilder<>(g -> GenUtils.toConstantCase(g.name()),
																								 FeatureIdFieldBuilder::new,
																								 FeatureStreams::distinctFeatures,
																								 null);

	private final InterfaceBuilder<MetaModel> featureIdsInterfacesBuilder = new SubInterfaceBuilder<>("FeatureIDs",
																									  groupFeatureIdInterfaceBuilder,
																									  this::streamOrderedGroup);

	public ModelDefinition(MetaModel model)
	{
		this.model = model;
		this.topology = new GroupTopologyBuilder(model);
	}

	private Stream<Group<?>> streamOrderedGroup(MetaModel model)
	{
		return topology.stream();
	}

	public void generate(final File target)
	{
		final var definitionInterface = TypeSpec.interfaceBuilder(model.name() + "ModelDefinition")
												.addModifiers(Modifier.PUBLIC);

		definitionInterface.addType(featureInterfacesBuilder.build(model));
		definitionInterface.addType(featureIdsInterfacesBuilder.build(model));
		definitionInterface.addType(genericBuilder.build(model));
		definitionInterface.addType(groupBuilder.build(model));
		definitionInterface.addType(unitBuilder.build(model));
		definitionInterface.addType(enumBuilder.build(model));
		definitionInterface.addType(aliasBuilder.build(model));
		definitionInterface.addType(javaWrapperBuilder.build(model));

		final var javaFile = JavaFile.builder(TargetPathUtil.packageName(model), definitionInterface.build())
									 .skipJavaLangImports(true)
									 .build();
		FormattedJavaWriter.write(javaFile, target);
	}

	private final class GroupTopologyBuilder
	{
		private final List<Group<?>> topology;

		public GroupTopologyBuilder(MetaModel model)
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
