package logoce.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import logoce.lmf.model.lang.Model;
import org.logoce.lmf.adapter.api.Adapter;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.ModelExtender;

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
