package isotropy.lmf.generator.group;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.util.GroupType;

import java.io.File;
import java.util.List;

public record GroupGenerationContext(Group<?> group,
									 File interfaceDirectory,
									 String packageName,
									 List<FeatureResolution> featureResolutions,
									 GroupType interfaceType)
{
	public record Builder(File directory)
	{
		public GroupGenerationContext build(Group<?> group)
		{
			final var model = (Model) group.lmContainer();
			final var packageName = model.domain();

			final var includes = group.includes();
			final var refInclude = includes.isEmpty() ? null : includes.get(0);
			final var types = GroupType.from(refInclude, group);
			final var featureResolutions = ModelUtils.streamAllFeatures(group).map(FeatureResolution::from).toList();

			return new GroupGenerationContext(group, directory, packageName, featureResolutions, types);
		}
	}
}
