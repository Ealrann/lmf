package isotropy.lmf.generator.group.feature;

import java.util.List;

public class FeatureInstallers
{
	private final List<FeatureInstaller<?>> installers;

	public FeatureInstallers(final List<FeatureInstaller<?>> installers) {this.installers = installers;}

	public void install(FeatureResolution resolution)
	{
		installers.forEach(f -> f.install(resolution));
	}
}
