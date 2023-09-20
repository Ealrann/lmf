package isotropy.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import isotropy.lmf.core.lang.Model;
import org.logoce.adapter.api.Adapter;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.ModelExtender;

@ModelExtender(scope = Model.class)
@Adapter
public final class ModelResolution implements IAdapter
{
	public final ClassName modelDefinition;

	private ModelResolution(final Model model)
	{
		this.modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
	}
}
