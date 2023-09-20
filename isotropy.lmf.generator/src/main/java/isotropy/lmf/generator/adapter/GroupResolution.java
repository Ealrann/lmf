package isotropy.lmf.generator.adapter;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.generator.util.GroupType;
import org.logoce.adapter.api.Adapter;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.ModelExtender;

@ModelExtender(scope = Group.class)
@Adapter
public final class GroupResolution implements IAdapter
{
	public final String packageName;
	public final GroupType interfaceType;

	private GroupResolution(final Group<?> group)
	{
		this.packageName = ((Model) group.lmContainer()).domain();
		this.interfaceType = GroupType.from(group);
	}
}
